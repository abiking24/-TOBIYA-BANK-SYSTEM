package com.example.banksystem3.client.controller.admin;

import com.example.banksystem3.client.rmi.RMIClient;
import com.example.banksystem3.client.utils.AlertUtil;
import com.example.banksystem3.shared.Account;
import com.example.banksystem3.shared.BankService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.rmi.RemoteException;

public class WithdrawController {

    @FXML
    private TextField accountNumberField;
    @FXML
    private Label accountHolderLabel;
    @FXML
    private TextField amountField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private Button verifyButton;
    @FXML
    private Button withdrawButton;
    @FXML
    private Button clearButton;

    private Account targetAccount;

    @FXML
    private void handleVerifyAccount() {
        String accountNumber = accountNumberField.getText().trim();
        if (accountNumber.isEmpty()) {
            AlertUtil.showError("Error", "Account number cannot be empty.");
            return;
        }

        try {
            BankService bankService = RMIClient.getInstance().getBankService();
            targetAccount = bankService.getAccountByNumber(accountNumber);

            if (targetAccount != null) {
                accountHolderLabel.setText(targetAccount.getAccountHolderName());
                withdrawButton.setDisable(false);
            } else {
                AlertUtil.showError("Error", "Account not found.");
                accountHolderLabel.setText("Not verified");
                withdrawButton.setDisable(true);
            }
        } catch (RemoteException e) {
            AlertUtil.showError("Error", "An error occurred while verifying the account.");
        }
    }

    @FXML
    private void handleWithdraw() {
        if (targetAccount == null) {
            AlertUtil.showError("Error", "Please verify an account first.");
            return;
        }

        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            AlertUtil.showError("Error", "Amount cannot be empty.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            String description = descriptionField.getText().trim();

            BankService bankService = RMIClient.getInstance().getBankService();
            boolean success = bankService.withdraw(targetAccount.getAccountId(), amount, description);

            if (success) {
                AlertUtil.showInfo("Success", "Withdrawal successful.");
                handleClear();
            } else {
                AlertUtil.showError("Error", "Withdrawal failed. Check account balance.");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Error", "Invalid amount format.");
        } catch (RemoteException e) {
            AlertUtil.showError("Error", "An error occurred during the withdrawal.");
        }
    }

    @FXML
    private void handleClear() {
        accountNumberField.clear();
        accountHolderLabel.setText("Not verified");
        amountField.clear();
        descriptionField.clear();
        withdrawButton.setDisable(true);
        targetAccount = null;
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) accountNumberField.getScene().getWindow();
        stage.close();
    }
}