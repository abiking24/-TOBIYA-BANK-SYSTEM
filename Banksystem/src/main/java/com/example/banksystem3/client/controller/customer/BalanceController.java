package com.example.banksystem3.client.controller.customer;

import com.example.banksystem3.client.rmi.RMIClient;
import com.example.banksystem3.client.session.SessionManager;
import com.example.banksystem3.client.utils.AlertUtil;
import com.example.banksystem3.client.utils.CurrencyUtil;
import com.example.banksystem3.shared.Account;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.rmi.RemoteException;
import java.util.List;

public class BalanceController {

    @FXML private VBox accountsContainer;

    @FXML
    public void initialize() {
        loadBalances();
    }

    @FXML
    private void handleRefresh() {
        loadBalances();
    }

    private void loadBalances() {
        accountsContainer.getChildren().clear();
        try {
            String userId = SessionManager.getInstance().getCurrentUser().getUserId();
            List<Account> accounts = RMIClient.getInstance().getBankService().getCustomerAccounts(userId);

            if (accounts.isEmpty()) {
                accountsContainer.getChildren().add(new Label("No accounts found."));
                return;
            }

            for (Account acc : accounts) {
                VBox card = new VBox(5);
                card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");

                Text type = new Text(acc.getType().toString());
                type.setStyle("-fx-font-size: 14px; -fx-fill: #7f8c8d; -fx-font-weight: bold;");

                Text number = new Text("Account: " + acc.getAccountNumber());
                number.setStyle("-fx-font-size: 12px; -fx-fill: #95a5a6;");

                Text balance = new Text(CurrencyUtil.format(acc.getBalance()));
                balance.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-fill: #27ae60;");

                card.getChildren().addAll(type, number, balance);
                accountsContainer.getChildren().add(card);
            }

        } catch (RemoteException e) {
            AlertUtil.showError("Error", "Failed to load balances.");
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) accountsContainer.getScene().getWindow()).close();
    }
}