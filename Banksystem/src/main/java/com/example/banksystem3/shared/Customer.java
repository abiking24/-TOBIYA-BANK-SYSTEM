package com.example.banksystem3.shared;

import java.io.Serializable;

public class Customer implements Serializable {
    private String customerId;
    private String name;
    private String phone;
    private String address;
    private String dob; // Added date of birth field

    public Customer() {}

    public Customer(String customerId, String name, String phone, String address, String dob) {
        this.customerId = customerId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.dob = dob; // Initialize dob
    }

    // Getters and setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    @Override
    public String toString() {
        return name + " (" + customerId + ")";
    }
}