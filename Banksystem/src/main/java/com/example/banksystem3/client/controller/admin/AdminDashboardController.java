package com.example.banksystem3.client.controller.admin;

import com.example.banksystem3.client.controller.Navigable;
import com.example.banksystem3.client.rmi.RMIClient;
import com.example.banksystem3.client.session.SessionManager;
import com.example.banksystem3.client.utils.AlertUtil;
import com.example.banksystem3.client.utils.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminDashboardController implements Initializable {
    private static final Logger logger = Logger.getLogger(AdminDashboardController.class.getName());

    @FXML private Label welcomeLabel;
    @FXML private StackPane contentPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("AdminDashboardController initialized.");

        if (!RMIClient.getInstance().isConnected()) {
            AlertUtil.showError("Connection Error", "Failed to connect to RMI server.");
        }

        String username = SessionManager.getInstance().getUsername();
        if (username != null && !username.isEmpty()) {
            welcomeLabel.setText("Welcome, Admin " + username + "!");
        } else {
            welcomeLabel.setText("Welcome, Administrator!");
        }

        try {
            loadDefaultDashboard();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load initial dashboard view", e);
            javafx.application.Platform.runLater(() -> 
                AlertUtil.showError("Dashboard Load Error", "Failed to load initial dashboard view: " + e.getMessage())
            );
        }
    }

    @FXML
    private void handleManageCustomers() {
        try {
            loadView(ViewManager.MANAGE_CUSTOMERS);
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "Failed to load Manage Customers: " + e.getMessage());
            logger.log(Level.SEVERE, "Failed to load ManageCustomer.fxml", e);
        }
    }

    @FXML
    private void handleManageAccounts() {
        try {
            loadView(ViewManager.MANAGE_ACCOUNTS);
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "Failed to load Manage Accounts: " + e.getMessage());
            logger.log(Level.SEVERE, "Failed to load ManageAccount.fxml", e);
        }
    }

    @FXML
    private void handleRegisterUser() {
        try {
            loadView(ViewManager.REGISTER_USER);
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "Failed to load Register User: " + e.getMessage());
            logger.log(Level.SEVERE, "Failed to load RegisterUser.fxml", e);
        }
    }

    @FXML
    private void handleViewLogs() {
        try {
            loadView(ViewManager.VIEW_LOGS);
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "Failed to load View Logs: " + e.getMessage());
            logger.log(Level.SEVERE, "Failed to load ViewLogs.fxml", e);
        }
    }

    @FXML
    private void handleExchangeRates() {
        try {
            loadView(ViewManager.EXCHANGE_RATES);
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "Failed to load Exchange Rates: " + e.getMessage());
            logger.log(Level.SEVERE, "Failed to load ExchangeRates.fxml", e);
        }
    }

    @FXML
    private void handleDashboard() {
        try {
            loadDefaultDashboard();
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "Failed to load Dashboard: " + e.getMessage());
            logger.log(Level.SEVERE, "Failed to load DashboardView.fxml", e);
        }
    }

    @FXML
    private void handleLogout() {
        if (AlertUtil.showConfirmation("Logout", "Are you sure you want to logout?")) {
            SessionManager.getInstance().logout();
            navigateToLogin();
        }
    }

    @FXML
    private void handleExit() {
        if (AlertUtil.showConfirmation("Exit", "Are you sure you want to exit?")) {
            System.exit(0);
        }
    }

    @FXML
    private void handleReports() {
        AlertUtil.showInfo("Reports", "This feature is coming soon!");
    }

    @FXML
    private void handleSettings() {
        AlertUtil.showInfo("Settings", "This feature is coming soon!");
    }

    private void loadDefaultDashboard() throws IOException {
        loadView(ViewManager.ADMIN_DASHBOARD_VIEW);
    }

    private void loadView(String fxmlPath) throws IOException {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("Cannot find FXML file at path: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();

            if (loader.getController() instanceof Navigable) {
                ((Navigable) loader.getController()).setParentController(this);
            }

            contentPane.getChildren().setAll(view);
            logger.info("Successfully loaded view: " + fxmlPath);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load view: " + fxmlPath, e);
            throw e;
        }
    }

    private void navigateToLogin() {
        try {
            ViewManager.switchScene(ViewManager.LOGIN, "🏦 Banking System - Login");
            logger.info("Navigated to login screen successfully");
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "Failed to load login screen: " + e.getMessage());
            logger.log(Level.SEVERE, "Failed to navigate to login", e);
        }
    }

    public void showDefaultDashboardView() {
        try {
            loadDefaultDashboard();
        } catch (IOException e) {
             AlertUtil.showError("Navigation Error", "Failed to load Dashboard: " + e.getMessage());
        }
    }
}