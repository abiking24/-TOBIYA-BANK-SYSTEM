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

public class BillPaymentController implements Navigable {

    @FXML private ComboBox<Account> accountCombo;
    @FXML private ComboBox<String> billerCombo;
    @FXML private TextField referenceField;
    @FXML private TextField amountField;
    @FXML private Label referenceLabel;

    private CustomerDashboardController parentController;

    @FXML
    public void initialize() {
        billerCombo.setItems(FXCollections.observableArrayList("Ethio Telecom", "EEU (Electric)", "AAWSA (Water)"));
        billerCombo.getSelectionModel().selectFirst();
        loadAccounts();
    }

    private void loadAccounts() {
        try {
            String userId = SessionManager.getInstance().getCurrentUser().getUserId();
            List<Account> accounts = RMIClient.getInstance().getBankService().getCustomerAccounts(userId);
            accountCombo.setItems(FXCollections.observableArrayList(accounts));
            accountCombo.setConverter(new StringConverter<Account>() {
                @Override public String toString(Account a) { return a == null ? "" : a.getAccountNumber(); }
                @Override public Account fromString(String s) { return null; }
            });
            if (!accounts.isEmpty()) accountCombo.getSelectionModel().selectFirst();
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load accounts.");
        }
    }

    @FXML
    private void handlePayBill() {
        Account from = accountCombo.getValue();
        if (from == null) return;

        if (!ValidationUtil.isPositiveDouble(amountField.getText())) {
            AlertUtil.showError("Error", "Invalid amount.");
            return;
        }
        double amount = Double.parseDouble(amountField.getText());

        try {
            boolean success = RMIClient.getInstance().getBankService().payBill(
                    from.getAccountId(),
                    billerCombo.getValue(),
                    referenceField.getText(),
                    amount
            );
            if (success) {
                AlertUtil.showSuccess("Success", "Bill paid successfully.");
                amountField.clear(); referenceField.clear();
            } else {
                AlertUtil.showError("Error", "Payment failed.");
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", "Payment error: " + e.getMessage());
        }
    }

    @Override public void setParentController(Object controller) { this.parentController = (CustomerDashboardController) controller; }
}