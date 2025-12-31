package com.example.banksystem3.shared;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Account implements Serializable {
    private String accountId;
    private String customerId;
    private String accountNumber;
    private String accountHolderName;
    private AccountType type;
    private double balance;
    private String status;
    private String createdAt;
    private LocalDateTime updatedAt;

    public enum AccountType {
        SAVINGS("Savings Account", 100.0, 0.5),
        WADIAH("Wadiah Account", 0.0, 0.0);

        private final String displayName;
        private final double minimumBalance;
        private final double interestRate;

        AccountType(String displayName, double minimumBalance, double interestRate) {
            this.displayName = displayName;
            this.minimumBalance = minimumBalance;
            this.interestRate = interestRate;
        }

        public String getDisplayName() { return displayName; }
        public double getMinimumBalance() { return minimumBalance; }
        public double getInterestRate() { return interestRate; }

        @Override
        public String toString() { return displayName; }
    }

    public Account() {
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);
        this.updatedAt = LocalDateTime.now();
    }

    public Account(String accountId, String customerId, String accountNumber,
                   AccountType type, double balance, String createdAt) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.balance = balance;
        this.status = "ACTIVE";
        this.createdAt = createdAt;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) {
        this.accountId = accountId;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
        this.updatedAt = LocalDateTime.now();
    }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
        this.updatedAt = LocalDateTime.now();
    }

    public AccountType getType() { return type; }
    public void setType(AccountType type) {
        this.type = type;
        this.updatedAt = LocalDateTime.now();
    }

    public double getBalance() { return balance; }
    public void setBalance(double balance) {
        this.balance = balance;
        this.updatedAt = LocalDateTime.now();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Business Methods
    public boolean deposit(double amount) {
        if (amount <= 0 || !"ACTIVE".equals(status)) return false;
        this.balance += amount;
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || !"ACTIVE".equals(status) || amount > balance) return false;
        this.balance -= amount;
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %.2f ETB",
                accountNumber, accountHolderName, balance);
    }
}