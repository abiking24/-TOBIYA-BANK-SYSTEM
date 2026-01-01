package com.example.banksystem3.client.controller.customer;

import com.example.banksystem3.client.controller.Navigable;
import com.example.banksystem3.client.rmi.RMIClient;
import com.example.banksystem3.client.session.SessionManager;
import com.example.banksystem3.client.utils.AlertUtil;
import com.example.banksystem3.shared.Account;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class BalanceController implements Navigable {

    @FXML private VBox accountsContainer;
    private CustomerDashboardController parentController;

    @FXML
    public void initialize() {
        loadAccounts();
    }

    private void loadAccounts() {
        accountsContainer.getChildren().clear();
        try {
            String userId = SessionManager.getInstance().getCurrentUser().getUserId();
            List<Account> accounts = RMIClient.getInstance().getBankService().getCustomerAccounts(userId);

            if (accounts.isEmpty()) {
                Label noAcc = new Label("No accounts found.");
                accountsContainer.getChildren().add(noAcc);
                return;
            }

            for (Account acc : accounts) {
                accountsContainer.getChildren().add(createAccountCard(acc));
            }

        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load accounts: " + e.getMessage());
        }
    }

    private VBox createAccountCard(Account acc) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");

        Label typeLabel = new Label(acc.getType().toString());
        typeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        typeLabel.setStyle("-fx-text-fill: #0056B3;");

        Label numLabel = new Label("Acct: " + acc.getAccountNumber());
        numLabel.setStyle("-fx-text-fill: #666;");

        Label balLabel = new Label(String.format("ETB %.2f", acc.getBalance()));
        balLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        balLabel.setStyle("-fx-text-fill: #27ae60;");

        card.getChildren().addAll(typeLabel, numLabel, balLabel);
        return card;
    }

    @FXML private void handleRefresh() { loadAccounts(); }
    @FXML private void handleClose() { 
        // Logic to return to main view if needed, or just stay
    }

    @Override
    public void setParentController(Object controller) {
        if (controller instanceof CustomerDashboardController) {
            this.parentController = (CustomerDashboardController) controller;
        }
    }
}