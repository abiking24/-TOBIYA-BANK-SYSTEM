package com.example.banksystem3.client.controller.customer;

import com.example.banksystem3.client.rmi.RMIClient;
import com.example.banksystem3.client.session.SessionManager;
import com.example.banksystem3.client.utils.AlertUtil;
import com.example.banksystem3.client.utils.CurrencyUtil;
import com.example.banksystem3.shared.Account;
import com.example.banksystem3.shared.Customer; // Import Customer
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.RemoteException;
import java.net.URL; // Added import for URL
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger; // Import Logger

public class TransferController {

    private static final Logger logger = Logger.getLogger(TransferController.class.getName()); // Add Logger

    @FXML private ComboBox<Account> fromAccountCombo;
    @FXML private ComboBox<String> transferTypeCombo;
    @FXML private ComboBox<String> bankCombo;
    @FXML private ComboBox<String> walletCombo;
    @FXML private TextField toAccountField;
    @FXML private TextField amountField;
    @FXML private TextArea descriptionField;
    @FXML private Label currentBalanceLabel;
    @FXML private Label recipientNameLabel;
    @FXML private Button verifyButton;
    @FXML private Button transferButton;
    @FXML private Button clearButton;

    private ObservableList<Account> accountList;
    private Account recipientAccount; // To store the verified recipient account (Internal only)
    private String recipientName; // To store verified recipient name (Internal & External)

    @FXML
    public void initialize() {
        logger.info("TransferController initialized."); // Added log
        if (!RMIClient.getInstance().isConnected()) {
            AlertUtil.showError("Connection Error", "Not connected to the server. Please restart the client.");
            // Disable form if not connected
            setFormDisable(true);
            return;
        }

        accountList = FXCollections.observableArrayList();
        fromAccountCombo.setItems(accountList);
        loadCustomerAccounts();

        // Populate transfer type combo box
        transferTypeCombo.setItems(FXCollections.observableArrayList(
                "Bank Transfer",
                "Wallet Transfer"
        ));

        // Populate bank combo box
        bankCombo.setItems(FXCollections.observableArrayList(
                "Tobia Bank", // Internal Bank
                "Awash Bank",
                "Bank of Abyssinia",
                "Commercial Bank of Ethiopia",
                "Dashen Bank",
                "Hibret Bank",
                "Wegagen Bank",
                "Zemen Bank",
                "Other"
        ));

        // Populate wallet combo box
        walletCombo.setItems(FXCollections.observableArrayList(
                "Telebirr",
                "Safaricom M-PESA"
        ));

        // Set up transfer type selection listener
        transferTypeCombo.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        if (newSelection.equals("Bank Transfer")) {
                            bankCombo.setVisible(true);
                            walletCombo.setVisible(false);
                        } else if (newSelection.equals("Wallet Transfer")) {
                            bankCombo.setVisible(false);
                            walletCombo.setVisible(true);
                        }
                    }
                });

        // Set up account selection listener
        fromAccountCombo.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        updateBalanceDisplay(newSelection);
                    } else {
                        currentBalanceLabel.setText("Current Balance: --");
                    }
                    // Reset recipient info if source account changes
                    resetRecipientInfo();
                });

        // Initialize recipient info
        recipientNameLabel.setText("Not verified");
        transferButton.setDisable(true); // Disable transfer until recipient is verified
    }

    private void resetRecipientInfo() {
        recipientAccount = null;
        recipientName = null;
        recipientNameLabel.setText("Not verified");
        transferButton.setDisable(true);
    }

    private void loadCustomerAccounts() {
        try {            String customerId = SessionManager.getInstance().getCurrentUser().getUserId();
            logger.info("Loading accounts for customer ID: " + customerId); // Added log

            if (customerId != null) {
                List<Account> accounts = RMIClient.getInstance().getBankService().getCustomerAccounts(customerId);
                accountList.setAll(accounts);

                if (!accounts.isEmpty()) {
                    fromAccountCombo.getSelectionModel().select(0);
                    logger.info("Loaded " + accounts.size() + " accounts."); // Added log
                } else {
                    logger.info("No accounts found for customer ID: " + customerId); // Added log
                    AlertUtil.showInfo("No Accounts", "No accounts found for your profile.");
                }
            } else {
                logger.warning("Customer ID is null in session."); // Added log
                AlertUtil.showError("Session Error", "Customer ID not found in session.");
            }
        } catch (RemoteException e) {
            AlertUtil.showError("Server Error", "Failed to load accounts from the server.");
            logger.log(Level.SEVERE, "Failed to load customer accounts via RMI", e);
        } catch (Exception e) { // Catch any other unexpected errors
            AlertUtil.showError("Error", "Failed to load accounts: " + e.getMessage());
            logger.log(Level.SEVERE, "Unexpected error loading customer accounts", e);
        }
    }

    private void updateBalanceDisplay(Account account) {
        if (account == null) {
            currentBalanceLabel.setText("Current Balance: --");
            return;
        }
        try {
            double balance = RMIClient.getInstance().getBankService().getBalance(account.getAccountId());
            currentBalanceLabel.setText("Current Balance: " + CurrencyUtil.format(balance));
            logger.info("Updated balance display for account " + account.getAccountNumber() + ": $" + balance); // Added log
        } catch (Exception e) {
            currentBalanceLabel.setText("Balance: Error");
            logger.log(Level.SEVERE, "Failed to get balance for account: " + account.getAccountId(), e); // Added log
        }
    }

    @FXML
    private void handleVerifyRecipient() {
        logger.info("Verifying recipient initiated."); // Added log
        String accountNumber = toAccountField.getText().trim();
        String transferType = transferTypeCombo.getValue();

        if (transferType == null) {
            AlertUtil.showError("Validation Error", "Please select a transfer type.");
            return;
        }

        if (accountNumber.isEmpty()) {
            AlertUtil.showError("Validation Error", "Please enter recipient account/phone number.");
            return;
        }

        Account fromAccount = fromAccountCombo.getValue();
        if (fromAccount == null) {
            AlertUtil.showError("Validation Error", "Please select your source account first.");
            return;
        }

        try {
            // Logic for Internal Transfer (Tobia Bank)
            if ("Bank Transfer".equals(transferType) && "Tobia Bank".equals(bankCombo.getValue())) {
                recipientAccount = RMIClient.getInstance().getBankService().getAccountByNumber(accountNumber);

                if (recipientAccount == null) {
                    recipientNameLabel.setText("Account not found");
                    transferButton.setDisable(true);
                    AlertUtil.showError("Verification Failed", "Account number not found in Tobia Bank.");
                    return;
                }

                if (recipientAccount.getAccountId().equals(fromAccount.getAccountId())) {
                    recipientNameLabel.setText("Cannot transfer to same account");
                    transferButton.setDisable(true);
                    AlertUtil.showError("Validation Error", "Cannot transfer to the same account.");
                    return;
                }

                Customer recipientCustomer = RMIClient.getInstance().getBankService().getCustomerById(recipientAccount.getCustomerId());
                if (recipientCustomer != null) {
                    recipientName = recipientCustomer.getName();
                    recipientNameLabel.setText("Recipient: " + recipientName);
                    transferButton.setDisable(false);
                    AlertUtil.showInfo("Verified", "Internal Account verified: " + recipientName);
                } else {
                    recipientNameLabel.setText("Recipient: Unknown");
                    transferButton.setDisable(true);
                }

            } 
            // Logic for External Transfer (Other Banks or Wallets)
            else {
                String provider = "Bank Transfer".equals(transferType) ? bankCombo.getValue() : walletCombo.getValue();
                if (provider == null) {
                    AlertUtil.showError("Validation Error", "Please select a bank or wallet provider.");
                    return;
                }

                // Simulate external verification
                String verifiedName = RMIClient.getInstance().getBankService().verifyExternalUser(provider, accountNumber);
                
                if (!"Unknown User".equals(verifiedName)) {
                    recipientName = verifiedName;
                    recipientNameLabel.setText("Recipient: " + recipientName);
                    transferButton.setDisable(false);
                    recipientAccount = null; // No internal account object for external transfers
                    AlertUtil.showInfo("Verified", "External User verified: " + recipientName);
                } else {
                    recipientNameLabel.setText("User not found");
                    transferButton.setDisable(true);
                    AlertUtil.showError("Verification Failed", "User not found in " + provider);
                }
            }

        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to verify account: " + e.getMessage());
            logger.log(Level.SEVERE, "Failed to verify recipient", e);
        }
    }

    @FXML
    private void handleTransfer() {
        logger.info("Transfer initiated."); // Added log
        Account fromAccount = fromAccountCombo.getValue();
        if (fromAccount == null) {
            AlertUtil.showError("Validation Error", "Please select source account.");
            return;
        }

        if (recipientName == null) {
            AlertUtil.showError("Validation Error", "Please verify recipient first.");
            return;
        }

        if (!validateAmount(fromAccount)) {
            return;
        }

        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            String description = descriptionField.getText().trim();
            if (description.isEmpty()) description = "Transfer";

            boolean success;
            
            // Internal Transfer
            if (recipientAccount != null) {
                 success = RMIClient.getInstance().getBankService().transfer(
                        fromAccount.getAccountId(),
                        recipientAccount.getAccountNumber(),
                        amount, description);
            } 
            // External Transfer (Simulated via Withdrawal for now as backend only supports internal transfer logic fully)
            else {
                // For external transfers, we simply withdraw from the sender and log it as a transfer
                // In a real system, this would call an external API
                String provider = "Bank Transfer".equals(transferTypeCombo.getValue()) ? bankCombo.getValue() : walletCombo.getValue();
                String externalDesc = description + " to " + provider + " (" + toAccountField.getText() + ")";
                
                success = RMIClient.getInstance().getBankService().withdraw(
                        fromAccount.getAccountId(), 
                        amount, 
                        externalDesc);
            }

            if (success) {
                AlertUtil.showInfo("Success", "Transfer of " + CurrencyUtil.format(amount) + " to " + recipientName + " was successful!");
                clearForm();
                updateBalanceDisplay(fromAccount);
            } else {
                AlertUtil.showError("Error", "Transfer failed. Please check your balance.");
            }

        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to process transfer: " + e.getMessage());
            logger.log(Level.SEVERE, "Exception during transfer process.", e);
        }
    }

    @FXML
    private void handleClear() {
        logger.info("Clear button clicked."); // Added log
        clearForm();
    }

    @FXML
    private void handleBack() throws IOException {
        // The most robust way to "go back" is to simply close the current view's container.
        Stage stage = (Stage) fromAccountCombo.getScene().getWindow();
        stage.close();
    }

    private boolean validateAmount(Account account) {
        String amountText = amountField.getText().trim();

        if (amountText.isEmpty()) {
            AlertUtil.showError("Validation Error", "Please enter transfer amount.");
            return false;
        }

        try {
            double amount = Double.parseDouble(amountText);
            double minimumBalance = account.getType().getMinimumBalance();

            if (amount <= 0) {
                AlertUtil.showError("Validation Error", "Transfer amount must be greater than 0.");
                return false;
            }

            try {
                double currentBalance = RMIClient.getInstance().getBankService().getBalance(account.getAccountId());

                if (amount > currentBalance - minimumBalance) {
                    AlertUtil.showError("Validation Error",
                            "Insufficient funds. You can transfer up to " + CurrencyUtil.format(currentBalance - minimumBalance));
                    return false;
                }

                if (amount > 10000) { // Transfer limit for demo
                    AlertUtil.showError("Validation Error", "Maximum transfer amount is 10,000 ETB per transaction.");
                    return false;
                }

                return true;

            } catch (Exception e) {
                AlertUtil.showError("Error", "Unable to verify account balance.");
                logger.log(Level.SEVERE, "Error verifying account balance during amount validation.", e); // Added log
                return false;
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Validation Error", "Please enter a valid amount.");
            logger.warning("Invalid amount format entered: " + amountText); // Added log
            return false;
        }
    }

    private void clearForm() {
        logger.info("Clearing transfer form."); // Added log
        fromAccountCombo.getSelectionModel().clearSelection();
        transferTypeCombo.getSelectionModel().clearSelection();
        bankCombo.getSelectionModel().clearSelection();
        walletCombo.getSelectionModel().clearSelection();
        toAccountField.clear();
        amountField.clear();
        descriptionField.clear();
        currentBalanceLabel.setText("Current Balance: --");
        resetRecipientInfo();
        toAccountField.requestFocus();
    }

    private void setFormDisable(boolean disabled) {
        fromAccountCombo.setDisable(disabled);
        transferTypeCombo.setDisable(disabled);
        bankCombo.setDisable(disabled);
        walletCombo.setDisable(disabled);
        toAccountField.setDisable(disabled);
        amountField.setDisable(disabled);
        descriptionField.setDisable(disabled);
        verifyButton.setDisable(disabled);
        transferButton.setDisable(disabled);
        clearButton.setDisable(disabled);
    }
}