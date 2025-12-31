package com.example.banksystem3.client.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class ViewManager {
    private static Stage primaryStage;

    // View constants
    public static final String LOGIN = "/com/example/banksystem3/view/login/Login.fxml";
    public static final String ADMIN_DASHBOARD = "/com/example/banksystem3/view/admin/AdminDashboard.fxml";
    public static final String CUSTOMER_DASHBOARD = "/com/example/banksystem3/view/customer/CustomerDashboard.fxml";
    public static final String MANAGE_CUSTOMERS = "/com/example/banksystem3/view/admin/ManageUsers.fxml";
    public static final String MANAGE_ACCOUNTS = "/com/example/banksystem3/view/admin/ManageAccounts.fxml";
    public static final String MANAGE_USERS = "/com/example/banksystem3/view/admin/ManageUsers.fxml";
    public static final String REGISTER_USER = "/com/example/banksystem3/view/admin/RegisterUser.fxml";
    public static final String VIEW_LOGS = "/com/example/banksystem3/view/admin/ViewLogs.fxml";
    public static final String EXCHANGE_RATES = "/com/example/banksystem3/view/admin/ExchangeRates.fxml";
    public static final String ADMIN_DASHBOARD_VIEW = "/com/example/banksystem3/view/admin/DashboardView.fxml";
    public static final String BALANCE = "/com/example/banksystem3/view/customer/Balance.fxml";
    public static final String TRANSACTION_HISTORY = "/com/example/banksystem3/view/customer/TransactionHistory.fxml";
    public static final String TRANSFER = "/com/example/banksystem3/view/customer/Transfer.fxml";
    public static final String BILL_PAYMENT = "/com/example/banksystem3/view/customer/BillPayment.fxml";

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static Parent loadFXML(String fxmlPath) throws IOException {
        URL fxmlUrl = ViewManager.class.getResource(fxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("Cannot find FXML file at path: " + fxmlPath);
        }
        return FXMLLoader.load(Objects.requireNonNull(fxmlUrl));
    }

    public static void switchScene(String fxmlPath, String title) throws IOException {
        Parent root = loadFXML(fxmlPath);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
}