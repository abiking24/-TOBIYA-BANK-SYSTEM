package com.example.banksystem3.shared;

import java.io.Serializable;
import java.time.LocalDate;

public class User implements Serializable {
    private String userId;
    private String fullName;
    private String nationalId;
    private String phone;
    private String email;
    private String dateOfBirth;
    private String gender;
    private String address;
    private String city;
    private String state;
    private String country;
    private String streetAddress;
    private String accountType;
    private Double balance;
    private String username;
    private String password;
    private Role role; // Changed to Role enum
    private String status;
    private String registrationDate;
    private String customerId; // Ensure this field exists if getCustomerId is needed

    public User() {
        this.registrationDate = LocalDate.now().toString();
        this.status = "ACTIVE";
        this.nationalId = "";
    }
    
    /**
     * Primary constructor for creating a new User.
     */
    public User(String userId, String username, String password, String fullName, Role role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.status = "ACTIVE";
        this.registrationDate = LocalDate.now().toString();
    }


    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) {
        this.nationalId = nationalId != null ? nationalId : "";
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; } // Changed to return Role enum
    public void setRole(Role role) { this.role = role; } // Changed to accept Role enum

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    // Added getCustomerId and setCustomerId
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }


    public boolean isActive() { return "ACTIVE".equals(status); }
    public boolean isCustomer() { return this.role == Role.CUSTOMER; } // Compare with Role enum
    public boolean isAdmin() { return this.role == Role.ADMIN; } // Compare with Role enum

    @Override
    public String toString() {
        return String.format("%s - %s (%s)", userId, fullName, role);
    }
}