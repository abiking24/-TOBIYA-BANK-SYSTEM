package com.example.banksystem3.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BankService extends Remote {
    // Authentication
    User login(String username, String password) throws RemoteException;
    boolean logout(String userId) throws RemoteException;

    // Customer Management
    boolean addCustomer(Customer customer, User user) throws RemoteException;
    boolean updateCustomer(Customer customer) throws RemoteException;
    boolean deleteCustomer(String customerId) throws RemoteException;
    List<Customer> getAllCustomers() throws RemoteException;
    Customer getCustomerById(String customerId) throws RemoteException;

    // Account Management
    boolean createAccount(Account account) throws RemoteException;
    boolean updateAccount(Account account) throws RemoteException;
    boolean closeAccount(String accountId) throws RemoteException;
    List<Account> getAllAccounts() throws RemoteException;
    Account getAccountById(String accountId) throws RemoteException;
    List<Account> getCustomerAccounts(String customerId) throws RemoteException;
    Account getAccountByNumber(String accountNumber) throws RemoteException;

    // Transaction Operations
    boolean deposit(String accountId, double amount, String description) throws RemoteException;
    boolean withdraw(String accountId, double amount, String description) throws RemoteException;
    boolean transfer(String fromAccountId, String toAccountNumber, double amount, String description) throws RemoteException;
    double getBalance(String accountId) throws RemoteException;
    boolean payBill(String accountId, String billerName, String billId, double amount) throws RemoteException;
    List<Transaction> getTransactionHistory(String accountId) throws RemoteException;
    List<Transaction> getRecentTransactions(String accountId, int limit) throws RemoteException;
    List<Transaction> getAllTransactions() throws RemoteException;

    // User Management
    List<User> getAllUsers() throws RemoteException;
    boolean changePassword(String userId, String oldPassword, String newPassword) throws RemoteException;
    boolean registerUser(User user, Account account) throws RemoteException;
    boolean registerUser(User user) throws RemoteException;


    // Reports & Logs
    List<String> getSystemLogs() throws RemoteException;
    List<String> getAuditTrail(String userId) throws RemoteException;

    // Utility Methods
    String generateAccountNumber() throws RemoteException;
    String generateCustomerId() throws RemoteException;
    List<Customer> searchCustomers(String keyword) throws RemoteException;

    List<Transaction> getTransactions(String accountId) throws RemoteException;

    String verifyExternalUser(String bank, String accountNumber) throws RemoteException;

    // Currency Exchange
    boolean buyCurrency(String userId, String currencyCode, double amount) throws RemoteException;
    boolean sellCurrency(String userId, String currencyCode, double amount) throws RemoteException;
}