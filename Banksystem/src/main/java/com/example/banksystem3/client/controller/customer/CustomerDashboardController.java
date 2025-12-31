package com.example.banksystem3.client.controller.customer;

import com.example.banksystem3.client.session.SessionManager;
import com.example.banksystem3.client.utils.AlertUtil;
import com.example.banksystem3.client.rmi.RMIClient;
import com.example.banksystem3.client.utils.CurrencyUtil;
import com.example.banksystem3.client.utils.ViewManager;
import com.example.banksystem3.shared.Account;
import com.example.banksystem3.shared.User;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.logging.Level;
import java.util.List;
import java.util.logging.Logger;
import javafx.stage.Modality;

public class CustomerDashboardController {

    private static final Logger logger = Logger.getLogger(CustomerDashboardController.class.getName());

    @FXML
    private Label welcomeLabel;
    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            // DEFENSIVE CODING: Check for null user and null full name to prevent crashes.
            if (currentUser.getFullName() != null && !currentUser.getFullName().isEmpty()) {
                // Display the user's first name for a personal touch.
                String firstName = currentUser.getFullName().split(" ")[0];
                welcomeLabel.setText("Welcome, " + firstName + "!");
            } else {
                // Fallback message if user data is incomplete.
                welcomeLabel.setText("Welcome, Customer!");
                logger.warning("Could not retrieve full name for the current user.");
            }
        }

        logger.info("CustomerDashboardController initialized.");
        
        // Load default view (Balance/Home)
        handleBalance();
    }

    @FXML
    private void handleBalance() {
        loadView(ViewManager.BALANCE);
    }

    @FXML
    private void handleTransactionHistory() {
        loadView(ViewManager.TRANSACTION_HISTORY);
    }

    @FXML
    private void handleTransfer() {
        loadView(ViewManager.TRANSFER);
    }

    @FXML
    private void handleBillPayment() {
        loadView(ViewManager.BILL_PAYMENT);
    }

    @FXML
    private void handleLogout() {
        if (AlertUtil.showConfirmation("Logout", "Are you sure you want to logout?")) {
            SessionManager.getInstance().logout();
            navigateToLogin();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = ViewManager.loadFXML(fxmlPath);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "Could not load the requested view.");
            logger.log(Level.SEVERE, "Failed to load view: " + fxmlPath, e);
        }
    }

    private void navigateToLogin() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            Parent root = ViewManager.loadFXML(ViewManager.LOGIN);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("🏦 Banking System - Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "Failed to return to the login screen.");
            logger.log(Level.SEVERE, "Failed to navigate to login screen", e);
        }
    }
}