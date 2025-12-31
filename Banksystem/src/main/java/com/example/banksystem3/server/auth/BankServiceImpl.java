package com.example.banksystem3.server.auth;

import com.example.banksystem3.shared.*;
import com.example.banksystem3.server.utils.LogUtil;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BankServiceImpl extends UnicastRemoteObject implements BankService {
    private final AuthService authService;
    private final Map<String, Customer> customers;
    private final Map<String, Account> accounts;
    private final Map<String, List<Transaction>> transactions;
    private final Map<String, List<String>> auditTrails;
    private final List<String> systemLogs;
    private int accountCounter = 999999;
    private int customerCounter = 999;

    public BankServiceImpl() throws RemoteException {
        super();
        this.authService = new AuthService();
        this.customers = new ConcurrentHashMap<>();
        this.accounts = new ConcurrentHashMap<>();
        this.transactions = new ConcurrentHashMap<>();
        this.auditTrails = new ConcurrentHashMap<>();
        this.systemLogs = Collections.synchronizedList(new ArrayList<>());

        initializeSampleData();
        log("INFO", "Bank Server initialized successfully");
    }

    private void initializeSampleData() {
        String[] firstNames = {
            "Abebe", "Kebede", "Almaz", "Tigist", "Mohammed", "Sara", "Dawit", 
            "Yared", "Hanna", "Solomon", "Mulu", "Girma", "Aster", "Belay", "Chala",
            "Tadesse", "Alemu", "Bekele", "Yilma", "Ahmed"
        };
        String[] lastNames = {
            "Tesfaye", "Demeke", "Assefa", "Negash", "Worku", "Mekonnen", "Haile", 
            "Desta", "Fikru", "Lemma", "Amare", "Kassahun", "Berhanu", "Wolde", "Tefera",
            "Getachew", "Bogale", "Admasu", "Shiferaw", "Zewdu"
        };

        for (int i = 0; i < 20; i++) {
            try {
                String firstName = firstNames[i % firstNames.length];
                String lastName = lastNames[i % lastNames.length];
                String fullName = firstName + " " + lastName;
                String username = firstName.toLowerCase() + (i + 1);
                String password = "password123";
                // Ethiopian phone format example: +251 911 234567
                String phone = "+251911" + String.format("%06d", i * 123 + 100000);
                String address = "Addis Ababa, Bole, House " + (i + 100);
                String dob = "19" + (80 + (i % 20)) + "-01-01";
                String nationalId = null; // Optional field left null

                String userId = String.valueOf(2000 + i);
                
                // Create User with required fields and optional nationalId
                User user = new User(userId, username, password, fullName, Role.CUSTOMER);
                user.setPhone(phone);
                user.setAddress(address);
                user.setDateOfBirth(dob);
                
                // Create Account
                String accountId = String.valueOf(3000 + i);
                String accountNumber = generateAccountNumber();
                String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                Account account = new Account(accountId, userId, accountNumber, Account.AccountType.SAVINGS, 5000.0 + (i * 1000), currentDate);
                
                // Register using the admin-like registration method
                registerUser(user, account);
                
            } catch (Exception e) {
                log("ERROR", "Failed to initialize sample user " + i + ": " + e.getMessage());
            }
        }
    }

    private void addSampleTransactions() {
        // Sample data removed
    }

    private void log(String level, String message) {
        String logEntry = LogUtil.formatLog(level, message);
        systemLogs.add(logEntry);
        System.out.println(logEntry);
    }

    private void audit(String userId, String action) {
        String auditEntry = LogUtil.formatInfo(userId + " - " + action);
        auditTrails.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(auditEntry);
    }

    // Authentication Methods
    @Override
    public User login(String username, String password) throws RemoteException {
        User user = authService.authenticate(username, password);
        if (user != null) {
            log("INFO", "User logged in: " + username);
            audit(user.getUserId(), "Login successful");
            return user;
        }
        log("WARN", "Failed login attempt: " + username);
        return null;
    }

    @Override
    public boolean logout(String userId) throws RemoteException {
        boolean success = authService.logout(userId);
        if (success) {
            log("INFO", "User logged out: " + userId);
            audit(userId, "Logout");
        }
        return success;
    }

    @Override
    public boolean changePassword(String userId, String oldPassword, String newPassword) throws RemoteException {
        User user = authService.getUserById(userId);
        if (user != null) {
            return authService.changePassword(user.getUsername(), oldPassword, newPassword);
        }
        log("WARN", "Password change failed: User ID not found: " + userId);
        return false;
    }

    @Override
    public boolean registerUser(User user, Account account) throws RemoteException {
        // --- ROBUST REGISTRATION LOGIC ---
        // This logic ensures that if account creation fails, the user is not left orphaned.

        // 1. Add the user first.
        if (!authService.addUser(user)) {
            log("WARN", "Registration failed: Username '" + user.getUsername() + "' already exists.");
            return false; // User already exists
        }

        // 2. If an account is provided (for a CUSTOMER), create it.
        if (account != null) {
            // Also create a corresponding Customer object from the User details
            Customer newCustomer = new Customer(
                    user.getUserId(),
                    user.getFullName(),
                    user.getPhone(),
                    user.getAddress(),
                    user.getDateOfBirth()
            );
            customers.put(newCustomer.getCustomerId(), newCustomer);

            account.setCustomerId(user.getUserId());
            // Always ensure system-generated 7-digit numeric account number
            if (account.getAccountNumber() == null
                    || account.getAccountNumber().isBlank()
                    || "TEMP_ACC_NUM".equalsIgnoreCase(account.getAccountNumber())
                    || !account.getAccountNumber().matches("\\d{7}")) {
                account.setAccountNumber(generateAccountNumber());
            }
            if (!createAccount(account)) {
                authService.removeUser(user.getUsername()); // Rollback user creation
                customers.remove(newCustomer.getCustomerId()); // Rollback customer creation
                log("ERROR", "Registration failed: Could not create account for user '" + user.getUsername() + "'. User creation rolled back.");
                return false; // Account creation failed
            }
            log("INFO", "Registered new user '" + user.getUsername() + "' with account " + account.getAccountNumber());
            audit(user.getUserId(), "User and initial account created.");
        } else {
            // For ADMIN or other roles that don't need an account.
            log("INFO", "Registered new user '" + user.getUsername() + "' without an account.");
            audit(user.getUserId(), "Admin user created.");
        }
        return true;
    }

    @Override
    public boolean registerUser(User user) throws RemoteException {
        if (authService.addUser(user)) {
            log("INFO", "Registered new user without an account: " + user.getUsername());
            audit(user.getUserId(), "User registered without an account.");
            return true;
        }
        return false;
    }

    // Customer Management
    @Override
    public boolean addCustomer(Customer customer, User user) throws RemoteException {
        if (customers.containsKey(customer.getCustomerId())) {
            return false;
        }

        customers.put(customer.getCustomerId(), customer);

        if (user != null) {
            authService.addUser(user);
        }

        log("INFO", "Customer added: " + customer.getName() + " (" + customer.getCustomerId() + ")");
        audit(user != null ? user.getUserId() : "SYSTEM", "Added customer: " + customer.getCustomerId());
        return true;
    }

    @Override
    public boolean updateCustomer(Customer customer) throws RemoteException {
        if (!customers.containsKey(customer.getCustomerId())) {
            return false;
        }
        customers.put(customer.getCustomerId(), customer);
        log("INFO", "Customer updated: " + customer.getCustomerId());

        // Also update the corresponding User object to maintain data consistency
        User userToUpdate = authService.getUserById(customer.getCustomerId());
        if (userToUpdate != null) {
            userToUpdate.setFullName(customer.getName());
            userToUpdate.setPhone(customer.getPhone());
            userToUpdate.setAddress(customer.getAddress());
            userToUpdate.setDateOfBirth(customer.getDob());
            log("INFO", "Synced user details for: " + userToUpdate.getUsername());
        } else {
            log("WARN", "Could not find corresponding User to sync for customer ID: " + customer.getCustomerId());
        }

        return true;
    }

    @Override
    public boolean deleteCustomer(String customerId) throws RemoteException {
        if (!customers.containsKey(customerId)) {
            return false;
        }

        // Check if customer has active accounts
        boolean hasActiveAccounts = accounts.values().stream()
                .anyMatch(account -> account.getCustomerId().equals(customerId) &&
                        "ACTIVE".equals(account.getStatus()));

        if (hasActiveAccounts) {
            log("WARN", "Cannot delete customer with active accounts: " + customerId);
            return false;
        }

        customers.remove(customerId);
        log("INFO", "Customer deleted: " + customerId);
        return true;
    }

    @Override
    public List<Customer> getAllCustomers() throws RemoteException {
        return new ArrayList<>(customers.values());
    }

    @Override
    public Customer getCustomerById(String customerId) throws RemoteException {
        return customers.get(customerId);
    }

    @Override
    public List<Customer> searchCustomers(String keyword) throws RemoteException {
        String lowerKeyword = keyword.toLowerCase();
        return customers.values().stream()
                .filter(customer ->
                        customer.getName().toLowerCase().contains(lowerKeyword) ||
                                customer.getPhone().contains(keyword) ||
                                customer.getCustomerId().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    // Account Management
    @Override
    public boolean createAccount(Account account) throws RemoteException {
        if (accounts.containsKey(account.getAccountId())) {
            return false;
        }

        // Validate that the customer for this account already exists.
        if (!customers.containsKey(account.getCustomerId())) {
            log("ERROR", "Account creation failed: Customer with ID '" + account.getCustomerId() + "' does not exist.");
            return false; // Cannot create an account for a non-existent customer.
        }

        accounts.put(account.getAccountId(), account);
        transactions.put(account.getAccountId(), Collections.synchronizedList(new ArrayList<>()));

        log("INFO", "Account created: " + account.getAccountNumber() +
                " for customer: " + account.getCustomerId());
        return true;
    }

    @Override
    public boolean updateAccount(Account account) throws RemoteException {
        if (!accounts.containsKey(account.getAccountId())) {
            return false;
        }
        accounts.put(account.getAccountId(), account);
        log("INFO", "Account updated: " + account.getAccountNumber());
        return true;
    }

    @Override
    public boolean closeAccount(String accountId) throws RemoteException {
        Account account = accounts.get(accountId);
        if (account == null) {
            return false;
        }

        account.setStatus("CLOSED");
        log("INFO", "Account closed: " + account.getAccountNumber());
        return true;
    }

    @Override
    public List<Account> getAllAccounts() throws RemoteException {
        return new ArrayList<>(accounts.values());
    }

    @Override
    public Account getAccountById(String accountId) throws RemoteException {
        return accounts.get(accountId);
    }

    @Override
    public Account getAccountByNumber(String accountNumber) throws RemoteException {
        return accounts.values().stream()
                .filter(account -> account.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Account> getCustomerAccounts(String customerId) throws RemoteException {
        return accounts.values().stream()
                .filter(account -> account.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

    // Transaction Operations
    @Override
    public boolean deposit(String accountId, double amount, String description) throws RemoteException {
        Account account = accounts.get(accountId);
        if (account == null || amount <= 0) {
            return false;
        }

        double oldBalance = account.getBalance();
        double newBalance = oldBalance + amount;
        account.setBalance(newBalance);

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                accountId,
                Transaction.TransactionType.DEPOSIT,
                amount,
                description,
                newBalance
        );
        transactions.get(accountId).add(transaction);

        log("INFO", String.format("Deposit: %.2f ETB to account %s", amount, account.getAccountNumber()));
        return true;
    }

    @Override
    public boolean withdraw(String accountId, double amount, String description) throws RemoteException {
        Account account = accounts.get(accountId);
        if (account == null || amount <= 0) {
            return false;
        }

        double minimumBalance = account.getType().getMinimumBalance();
        double oldBalance = account.getBalance();

        // Check sufficient funds including minimum balance requirement
        if (oldBalance - amount < minimumBalance) {
            log("WARN", String.format("Withdrawal failed: Insufficient funds for account %s. Required min: %.2f ETB",
                    account.getAccountNumber(), minimumBalance));
            return false;
        }

        double newBalance = oldBalance - amount;
        account.setBalance(newBalance);

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                accountId,
                Transaction.TransactionType.WITHDRAWAL,
                amount,
                description,
                newBalance
        );
        transactions.get(accountId).add(transaction);

        log("INFO", String.format("Withdrawal: %.2f ETB from account %s", amount, account.getAccountNumber()));
        return true;
    }

    @Override
    public boolean payBill(String accountId, String billerName, String billId, double amount) throws RemoteException {
        Account account = accounts.get(accountId);
        if (account == null || amount <= 0) {
            return false;
        }

        double minimumBalance = account.getType().getMinimumBalance();
        double oldBalance = account.getBalance();

        // Check sufficient funds including minimum balance requirement
        if (oldBalance - amount < minimumBalance) {
            log("WARN", String.format("Bill Payment failed: Insufficient funds for account %s. Required min: %.2f ETB",
                    account.getAccountNumber(), minimumBalance));
            return false;
        }

        double newBalance = oldBalance - amount;
        account.setBalance(newBalance);

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                accountId,
                Transaction.TransactionType.WITHDRAWAL, // Using WITHDRAWAL as base type for payments
                amount,
                "Bill Payment: " + billerName + " (" + billId + ")",
                newBalance
        );
        transactions.get(accountId).add(transaction);

        log("INFO", String.format("Bill Payment: %.2f ETB to %s (%s) from account %s", amount, billerName, billId, account.getAccountNumber()));
        return true;
    }

    @Override
    public boolean transfer(String fromAccountId, String toAccountNumber, double amount, String description) throws RemoteException {
        Account fromAccount = accounts.get(fromAccountId);
        Account toAccount = getAccountByNumber(toAccountNumber);

        if (fromAccount == null || toAccount == null || amount <= 0) {
            return false;
        }

        double minimumBalance = fromAccount.getType().getMinimumBalance();
        double fromOldBalance = fromAccount.getBalance();

        // Check sufficient funds including minimum balance
        if (fromOldBalance - amount < minimumBalance) {
            log("WARN", String.format("Transfer failed: Insufficient funds in account %s",
                    fromAccount.getAccountNumber()));
            return false;
        }

        // Perform transfer
        double fromNewBalance = fromOldBalance - amount;
        double toNewBalance = toAccount.getBalance() + amount;

        fromAccount.setBalance(fromNewBalance);
        toAccount.setBalance(toNewBalance);

        // Record transactions for both accounts
        Transaction fromTransaction = new Transaction(
                UUID.randomUUID().toString(),
                fromAccountId,
                Transaction.TransactionType.TRANSFER_SENT,
                amount,
                description + " to " + toAccount.getAccountNumber(),
                fromNewBalance
        );

        Transaction toTransaction = new Transaction(
                UUID.randomUUID().toString(),
                toAccount.getAccountId(),
                Transaction.TransactionType.TRANSFER_RECEIVED,
                amount,
                description + " from " + fromAccount.getAccountNumber(),
                toNewBalance
        );

        transactions.get(fromAccountId).add(fromTransaction);
        transactions.get(toAccount.getAccountId()).add(toTransaction);

        log("INFO", String.format("Transfer: %.2f ETB from %s to %s",
                amount, fromAccount.getAccountNumber(), toAccount.getAccountNumber()));
        return true;
    }

    @Override
    public double getBalance(String accountId) throws RemoteException {
        Account account = accounts.get(accountId);
        return account != null ? account.getBalance() : 0.0;
    }

    @Override
    public List<Transaction> getTransactionHistory(String accountId) throws RemoteException {
        List<Transaction> accountTransactions = transactions.get(accountId);
        if (accountTransactions == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(accountTransactions);
    }

    @Override
    public List<Transaction> getRecentTransactions(String accountId, int limit) throws RemoteException {
        List<Transaction> accountTransactions = transactions.get(accountId);
        if (accountTransactions == null) {
            return new ArrayList<>();
        }

        return accountTransactions.stream()
                .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> getAllTransactions() throws RemoteException {
        List<Transaction> allTransactions = new ArrayList<>();
        for (List<Transaction> accountTransactions : transactions.values()) {
            allTransactions.addAll(accountTransactions);
        }
        return allTransactions;
    }

    // User Management
    @Override
    public List<User> getAllUsers() throws RemoteException {
        List<User> users = authService.getAllUsers();
        // Enrich customer users with their primary account details for display
        for (User user : users) {
            if (user.isCustomer()) {
                accounts.values().stream()
                        .filter(acc -> acc.getCustomerId().equals(user.getUserId()))
                        .findFirst() // Find the first account associated with the user
                        .ifPresent(account -> {
                            user.setAccountType(account.getType().getDisplayName());
                            user.setBalance(account.getBalance());
                            // The other user details (address, etc.) are already on the User object
                        });
            }
        }
        return users;
    }

    // Reports & Logs
    @Override
    public List<String> getSystemLogs() throws RemoteException {
        return new ArrayList<>(systemLogs);
    }

    @Override
    public List<String> getAuditTrail(String userId) throws RemoteException {
        return new ArrayList<>(auditTrails.getOrDefault(userId, new ArrayList<>()));
    }

    // Utility Methods
    @Override
    public String generateAccountNumber() throws RemoteException {
        // Start with 1000, followed by 11 random digits
        StringBuilder sb = new StringBuilder("1000");
        Random random = new Random();
        for (int i = 0; i < 11; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    @Override
    public String generateCustomerId() throws RemoteException {
        // Numeric-only, exactly 4 digits
        return String.format("%04d", ++customerCounter);
    }

    @Override
    public List<Transaction> getTransactions(String accountId) throws RemoteException {
        return getTransactionHistory(accountId);
    }

    @Override
    public String verifyExternalUser(String bank, String accountNumber) throws RemoteException {
        // 1. Check for specific hardcoded test cases first
        if (bank.equals("Commercial Bank of Ethiopia") && accountNumber.equals("1000257056329")) {
            return "Abriham";
        }
        if (accountNumber.equals("10001")) return "John Doe";
        if (accountNumber.equals("10002")) return "Jane Smith";

        // 2. "Smart Simulation" for ANY other input
        // If the account number is reasonably long (e.g., > 5 digits), generate a name.
        if (accountNumber != null && accountNumber.length() > 5) {
            return generateMockName(accountNumber);
        }

        return "Unknown User";
    }

    @Override
    public boolean buyCurrency(String userId, String currencyCode, double amount) throws RemoteException {
        log("INFO", "User " + userId + " bought " + amount + " " + currencyCode);
        audit(userId, "Bought " + amount + " " + currencyCode);
        return true;
    }

    @Override
    public boolean sellCurrency(String userId, String currencyCode, double amount) throws RemoteException {
        log("INFO", "User " + userId + " sold " + amount + " " + currencyCode);
        audit(userId, "Sold " + amount + " " + currencyCode);
        return true;
    }

    // Helper method to generate a deterministic name based on the account number
    private String generateMockName(String seed) {
        String[] firstNames = {
            "Abebe", "Kebede", "Almaz", "Tigist", "Mohammed", "Sara", "Dawit", 
            "Yared", "Hanna", "Solomon", "Mulu", "Girma", "Aster", "Belay", "Chala"
        };
        String[] lastNames = {
            "Tadesse", "Alemu", "Bekele", "Yilma", "Ahmed", "Tesfaye", "Demeke", 
            "Assefa", "Negash", "Worku", "Mekonnen", "Haile", "Desta", "Fikru"
        };

        // Use the hash code of the account number to pick a consistent name
        int hash = Math.abs(seed.hashCode());
        String first = firstNames[hash % firstNames.length];
        String last = lastNames[(hash / firstNames.length) % lastNames.length];

        return first + " " + last;
    }
}