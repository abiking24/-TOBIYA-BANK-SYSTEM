package com.example.banksystem3.server.auth;

import com.example.banksystem3.shared.BankService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            System.out.println("Starting Bank Server...");

            // Set RMI server hostname (optional, for remote connections)
            System.setProperty("java.rmi.server.hostname", "localhost");

            // Create and export the RMI service
            BankService bankService = new com.example.banksystem3.server.auth.BankServiceImpl();

            // Create RMI registry on default port 1099
            Registry registry = LocateRegistry.createRegistry(1099);

            // Bind the service to the registry
            registry.rebind("BankService", bankService);

            System.out.println("=====================================");
            System.out.println("Bank Server Started Successfully!");
            System.out.println("Service Name: BankService");
            System.out.println("Registry: localhost:1099");
            System.out.println("=====================================");
            // System.out.println("Sample Users:");
            // System.out.println("  Admin: admin / admin123");
            // System.out.println("  Customer: john / john123");
            // System.out.println("  Customer: jane / jane123");
            // System.out.println("=====================================");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            System.err.println("Error details: " + e);
        }
    }
}