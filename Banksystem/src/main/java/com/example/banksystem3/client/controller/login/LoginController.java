package com.example.banksystem3.client.controller.login; // Corrected package declaration

import com.example.banksystem3.client.rmi.RMIClient;
import com.example.banksystem3.client.session.SessionManager;
import com.example.banksystem3.client.utils.ErrorHandler;
import com.example.banksystem3.client.utils.ViewManager;
import com.example.banksystem3.client.utils.AlertUtil;
import com.example.banksystem3.shared.User;
import com.example.banksystem3.shared.Role; // Import Role enum
import javafx.fxml.FXML;
import javafx.animation.PauseTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.input.KeyCode; // Import KeyCode
import javafx.scene.layout.StackPane;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import java.util.Locale;
import java.util.ResourceBundle;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger; // Import Logger

public class LoginController {
    private static final Logger logger = Logger.getLogger(LoginController.class.getName()); // Add Logger

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField; // Visible text field for showing password
    @FXML private ToggleButton showPasswordToggle; // Toggle button for eye icon
    @FXML private Label statusLabel;
    @FXML private Button loginButton;
    @FXML private Button exitButton;

    private ResourceBundle bundle;
    private Locale currentLocale;

    @FXML
    public void initialize() {
        logger.info("LoginController initialized."); // Added log

        // Initialize with English by default
        loadLanguage(new Locale("en"));

        // Auto-connect to localhost
        if (RMIClient.getInstance().isConnected()) {
            statusLabel.setText("✅ Connected to server");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
        } else {
            statusLabel.setText("❌ Server not available. Please start the server first.");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }

        // Set up Enter key listener
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
        
        // Sync password fields
        if (passwordTextField != null) {
            passwordTextField.setManaged(false);
            passwordTextField.setVisible(false);
            
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        }
    }

    @FXML
    private void handleTogglePassword() {
        if (showPasswordToggle.isSelected()) {
            // Show password (switch to TextField)
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            showPasswordToggle.setText("🙈"); // Change icon to hide
        } else {
            // Hide password (switch to PasswordField)
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            showPasswordToggle.setText("👁"); // Change icon to show
        }
    }

    @FXML
    private void handleForgotPassword() {
        AlertUtil.showInfo("Forgot Password", "Please contact the administrator to reset your password.");
    }

    @FXML
    private void handleChangePassword() {
        // Create the custom dialog.
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Change Your Password");

        // Set the button types.
        ButtonType changeButtonType = new ButtonType("Change", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);

        // Create the labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Old Password");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Old Password:"), 0, 1);
        grid.add(oldPasswordField, 1, 1);
        grid.add(new Label("New Password:"), 0, 2);
        grid.add(newPasswordField, 1, 2);
        grid.add(new Label("Confirm Password:"), 0, 3);
        grid.add(confirmPasswordField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        javafx.application.Platform.runLater(usernameField::requestFocus);

        // Add event filter to handle validation and logic without closing dialog on error
        final Button btChange = (Button) dialog.getDialogPane().lookupButton(changeButtonType);
        btChange.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String user = usernameField.getText().trim();
            String oldPass = oldPasswordField.getText().trim();
            String newPass = newPasswordField.getText().trim();
            String confirmPass = confirmPasswordField.getText().trim();

            if (user.isEmpty() || oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                AlertUtil.showError("Validation Error", "All fields are required.");
                event.consume();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                AlertUtil.showError("Validation Error", "New passwords do not match.");
                event.consume();
                return;
            }
            
            if (!RMIClient.getInstance().isConnected()) {
                 AlertUtil.showError("Connection Error", "Not connected to server.");
                 event.consume();
                 return;
            }

            try {
                // Verify old password by attempting to login
                User authenticatedUser = RMIClient.getInstance().getBankService().login(user, oldPass);
                
                if (authenticatedUser == null) {
                    AlertUtil.showError("Authentication Failed", "Invalid username or old password.");
                    event.consume();
                    return;
                }

                // Change password using the specific service method
                boolean success = RMIClient.getInstance().getBankService().changePassword(authenticatedUser.getUserId(), oldPass, newPass);
                
                if (success) {
                    AlertUtil.showInfo("Success", "Password changed successfully.");
                } else {
                    AlertUtil.showError("Error", "Failed to change password. Please try again.");
                }

            } catch (Exception e) {
                ErrorHandler.handleException("Error", "An error occurred while changing password.", e);
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleLogin() {
        logger.info("Login attempt initiated."); // Added log
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim(); // Always get from passwordField as they are bound

        if (username.isEmpty() || password.isEmpty()) {
            AlertUtil.showError("Login Error", "Please enter username and password");
            logger.warning("Login failed: Username or password empty."); // Added log
            return;
        }

        if (!RMIClient.getInstance().isConnected()) {
            AlertUtil.showError("Connection Error", "Not connected to server. Please start the server first.");
            logger.severe("Login failed: Not connected to RMI server."); // Added log
            return;
        }

        try {
            User user = RMIClient.getInstance().getBankService().login(username, password);

            if (user != null) {
                SessionManager.getInstance().login(user);
                logger.info("Login successful for user: " + user.getUsername() + " with role: " + user.getRole()); // Added log
                statusLabel.setText("✅ Login successful! Redirecting...");
                statusLabel.setStyle("-fx-text-fill: #27ae60;");

                // BEST PRACTICE: Use a PauseTransition for a clean UI delay instead of a manual thread.
                PauseTransition delay = new PauseTransition(Duration.millis(500));
                delay.setOnFinished(event -> {
                    try {
                        navigateToDashboard();
                    } catch (IOException e) {
                        ErrorHandler.handleException("Navigation Failed", "Could not open the dashboard.", e);
                    }
                });
                delay.play();

            } else {
                statusLabel.setText("❌ Invalid username or password");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                passwordField.clear();
                passwordField.requestFocus();
                logger.warning("Login failed: Invalid username or password for user: " + username); // Added log
            }

        } catch (Exception e) {
            ErrorHandler.handleException("Login Error", "An unexpected error occurred during login.", e);
            statusLabel.setText("❌ Login failed due to a server error.");
        }
    }

    @FXML
    private void handleExit() {
        logger.info("Exit button clicked."); // Added log
        if (AlertUtil.showConfirmation("Exit", "Are you sure you want to exit?")) {
            System.exit(0);
        }
    }

    private void navigateToDashboard() throws IOException {
        logger.info("Navigating to dashboard."); // Added log
        String fxmlPath;
        String title;

        if (SessionManager.getInstance().isAdmin()) {
            fxmlPath = ViewManager.ADMIN_DASHBOARD;
            title = "Banking System - Admin Dashboard";
            logger.info("User is Admin. Loading Admin Dashboard from: " + fxmlPath); // Added log
        } else {
            fxmlPath = ViewManager.CUSTOMER_DASHBOARD;
            title = "Banking System - Customer Dashboard";
            logger.info("User is Customer. Loading Customer Dashboard from: " + fxmlPath); // Added log
        }

        ViewManager.switchScene(fxmlPath, title);
        logger.info("Dashboard stage shown with title: " + title); // Added log
    }

    /**
     * Handles the quick login button for the admin user.
     * Sets predefined credentials and triggers the login process.
     */
    @FXML
    private void handleQuickAdminLogin() {
        // usernameField.setText("admin"); // Commented out to disable automatic input
        // passwordField.setText("admin123"); // Commented out to disable automatic input
    }

    @FXML
    private void handleQuickCustomerLogin() {
        // usernameField.setText("john"); // Commented out to disable automatic input
        // passwordField.setText("john123"); // Commented out to disable automatic input
    }

    private void loadLanguage(Locale locale) {
        currentLocale = locale;
        try {
            bundle = ResourceBundle.getBundle("com.example.banksystem3.i18n.messages", locale);
            updateUI();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load language bundle", e);
        }
    }

    private void updateUI() {
        if (bundle == null) return;

        // Update UI elements with text from bundle
        loginButton.setText(bundle.getString("login.button"));
        exitButton.setText(bundle.getString("login.exit"));
        usernameField.setPromptText(bundle.getString("login.username"));
        passwordField.setPromptText(bundle.getString("login.password"));
    }

    @FXML
    private void switchToEnglish() {
        loadLanguage(new Locale("en"));
    }

    @FXML
    private void switchToAmharic() {
        loadLanguage(new Locale("am"));
    }
}