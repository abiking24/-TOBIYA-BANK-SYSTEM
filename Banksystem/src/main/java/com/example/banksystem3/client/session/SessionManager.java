package com.example.banksystem3.client.session;

import com.example.banksystem3.shared.User;
import com.example.banksystem3.shared.Customer;
import com.example.banksystem3.shared.Account;
import com.example.banksystem3.shared.Role;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private Customer currentCustomer;
    private Account selectedAccount;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
        this.selectedAccount = null;
    }

    public void logout() {
        this.currentUser = null;
        this.currentCustomer = null;
        this.selectedAccount = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Customer getCurrentCustomer() {
        return currentCustomer;
    }

    public void setCurrentCustomer(Customer customer) {
        this.currentCustomer = customer;
    }

    public Account getSelectedAccount() {
        return selectedAccount;
    }

    public void setSelectedAccount(Account account) {
        this.selectedAccount = account;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    public String getUsername() {
        return currentUser != null ? currentUser.getUsername() : "Guest";
    }

    public String getUserId() {
        return currentUser != null ? currentUser.getUserId() : null;
    }
}