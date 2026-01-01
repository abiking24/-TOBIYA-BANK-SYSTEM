package com.example.banksystem3.client.controller.customer;

import com.example.banksystem3.client.controller.Navigable;
import com.example.banksystem3.client.rmi.RMIClient;
import com.example.banksystem3.client.session.SessionManager;
import com.example.banksystem3.client.utils.AlertUtil;
import com.example.banksystem3.client.utils.ValidationUtil;
import com.example.banksystem3.shared.Account;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.List;

public class TransferController implements Navigable {

    @FXML private ComboBox<Account> fromAccountCombo;
    @FXML private ComboBox<String> transferTypeCombo;
    @FXML private TextField toAccountField;
    @FXML private TextField amountField;
    @FXML private TextArea descriptionField;
    @FXML private Label currentBalanceLabel;
    @FXML private Label recipientNameLabel;
    @FXML private Button transferButton;

    private CustomerDashboardController parentController;

    @FXML
    public void initialize() {
        transferTypeCombo.setItems(FXCollections.observableArrayList("Internal Transfer", "Other Bank"));
        transferTypeCombo.getSelectionModel().selectFirst();
        
        loadAccounts();
        
        fromAccountCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentBalanceLabel.setText(String.format("Current Balance: ETB %.2f", newVal.getBalance()));
            }
        });
    }

    private void loadAccounts() {
        try {
            String userId = SessionManager.getInstance().getCurrentUser().getUserId();
            List<Account> accounts = RMIClient.getInstance().getBankService().getCustomerAccounts(userId);
            fromAccountCombo.setItems(FXCollections.observableArrayList(accounts));
            fromAccountCombo.setConverter(new StringConverter<Account>() {
                @Override public String toString(Account a) { return a == null ? "" : a.getAccountNumber(); }
                @Override public Account fromString(String s) { return null; }
            });
            if (!accounts.isEmpty()) fromAccountCombo.getSelectionModel().selectFirst();
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load accounts.");
        }
    }

    @FXML
    private void handleVerifyRecipient() {
        // Mock verification logic
        String accNum = toAccountField.getText();
        if (accNum.length() >= 4) {
            recipientNameLabel.setText("Verified User");
            transferButton.setDisable(false);
        } else {
            recipientNameLabel.setText("Invalid Account");
            transferButton.setDisable(true);
        }
    }

    @FXML
    private void handleTransfer() {
        Account from = fromAccountCombo.getValue();
        if (from == null) return;

        if (!ValidationUtil.isPositiveDouble(amountField.getText())) {
            AlertUtil.showError("Error", "Invalid amount.");
            return;
        }
        double amount = Double.parseDouble(amountField.getText());

        try {
            boolean success = RMIClient.getInstance().getBankService().transfer(
                    from.getAccountId(),
                    toAccountField.getText(),
                    amount,
                    descriptionField.getText()
            );
            if (success) {
                AlertUtil.showSuccess("Success", "Transfer completed.");
                handleClear();
            } else {
                AlertUtil.showError("Error", "Transfer failed.");
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", "Transfer error: " + e.getMessage());
        }
    }

    @FXML private void handleClear() {
        amountField.clear(); toAccountField.clear(); descriptionField.clear();
    }
    @FXML private void handleBack() { /* Navigate back */ }

    @Override public void setParentController(Object controller) {
        if (controller instanceof CustomerDashboardController) this.parentController = (CustomerDashboardController) controller;
    }
}