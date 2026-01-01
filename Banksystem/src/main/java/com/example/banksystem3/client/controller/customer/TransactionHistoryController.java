package com.example.banksystem3.client.controller.customer;

import com.example.banksystem3.client.rmi.RMIClient;
import com.example.banksystem3.client.session.SessionManager;
import com.example.banksystem3.client.utils.AlertUtil;
import com.example.banksystem3.client.utils.CurrencyUtil;
import com.example.banksystem3.shared.Account;
import com.example.banksystem3.shared.Transaction;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.rmi.RemoteException;
import java.util.List;

public class TransactionHistoryController {

    @FXML private ComboBox<Account> accountCombo;
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, String> colDescription;
    @FXML private TableColumn<Transaction, String> colAmount;
    @FXML private TableColumn<Transaction, String> colBalance;

    @FXML
    public void initialize() {
        setupTable();
        loadAccounts();

        accountCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadTransactions(newVal.getAccountId());
            }
        });
    }

    private void setupTable() {
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedTimestamp()));
        colType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().getDisplayName()));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        colAmount.setCellValueFactory(cellData -> {
            double amount = cellData.getValue().getAmount();
            String prefix = "";
            // Add + or - based on transaction type
            switch (cellData.getValue().getType()) {
                case DEPOSIT: case TRANSFER_RECEIVED: case INTEREST:
                    prefix = "+ "; break;
                case WITHDRAWAL: case TRANSFER_SENT:
                    prefix = "- "; break;
            }
            return new SimpleStringProperty(prefix + CurrencyUtil.format(amount));
        });

        colBalance.setCellValueFactory(cellData -> new SimpleStringProperty(CurrencyUtil.format(cellData.getValue().getBalanceAfter())));
    }

    private void loadAccounts() {
        try {
            String userId = SessionManager.getInstance().getCurrentUser().getUserId();
            List<Account> accounts = RMIClient.getInstance().getBankService().getCustomerAccounts(userId);
            accountCombo.setItems(FXCollections.observableArrayList(accounts));
            
            accountCombo.setConverter(new StringConverter<Account>() {
                @Override public String toString(Account a) { return a == null ? "" : a.getAccountNumber() + " (" + a.getType() + ")"; }
                @Override public Account fromString(String s) { return null; }
            });

            if (!accounts.isEmpty()) {
                accountCombo.getSelectionModel().selectFirst();
            }
        } catch (RemoteException e) {
            AlertUtil.showError("Error", "Failed to load accounts.");
        }
    }

    private void loadTransactions(String accountId) {
        try {
            List<Transaction> transactions = RMIClient.getInstance().getBankService().getTransactionHistory(accountId);
            // Sort by date descending (newest first)
            transactions.sort((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()));
            transactionTable.setItems(FXCollections.observableArrayList(transactions));
        } catch (RemoteException e) {
            AlertUtil.showError("Error", "Failed to load transactions.");
        }
    }

    @FXML
    private void handleRefresh() {
        if (accountCombo.getValue() != null) {
            loadTransactions(accountCombo.getValue().getAccountId());
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) transactionTable.getScene().getWindow()).close();
    }
}