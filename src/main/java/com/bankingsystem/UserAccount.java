package com.bankingsystem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserAccount {
    private static final Logger log = LogManager.getLogger(); 
    Scanner scan = new Scanner(System.in);
    protected final String userName;
    protected double userBalance;

    public UserAccount(String userName, double userBalance) {
        this.userName = userName;
        this.userBalance = userBalance;
    }

    public UserAccount() {
        this.userName = ""; // Initialize userName with an empty string for default constructor
    }

    public void menuLoginOrCreate() {
        System.out.println("+---------------------------------------------------------+");
        System.out.println("|                   Java Bank Application                 |");
        System.out.println("+---------------------------------------------------------+");
        System.out.println("|                   A. Create Account                     |");
        System.out.println("|                   B. Log into Account                   |");
        System.out.println("|                   C. Quit Application                   |");
        System.out.println("+---------------------------------------------------------+");

        String userInput;
        do {
            System.out.print("Enter your choice: ");
            userInput = scan.nextLine();
            if (userInput.isBlank() || (!userInput.equalsIgnoreCase("A") && !userInput.equalsIgnoreCase("B") && !userInput.equalsIgnoreCase("C"))) {
                log.error("Invalid Option");
            } else {
                log.info("Processing request");
                if (userInput.equalsIgnoreCase("A")) {
                    UserAccountRegistration();
                } else if (userInput.equalsIgnoreCase("B")) {
                    userLogin(); // Call login functionality
                } else if (userInput.equalsIgnoreCase("C")) {
                    System.out.println("Exiting application...");
                }
            }
        } while (!userInput.equalsIgnoreCase("C"));
    }

    // Method for user login
    public void userLogin() {
        System.out.print("Enter your username: ");
        String username = scan.nextLine();
        System.out.print("Enter your password: ");
        String password = scan.nextLine();

        if (authenticateUser(username, password)) {
            System.out.println("Login successful! Welcome, " + username + "!");
            // Proceed with further options after login (e.g., view balance, transfer money)
        } else {
            System.out.println("Invalid username or password. Please try again.");
        }
    }

    // Method to authenticate user credentials
    private boolean authenticateUser(String username, String password) {
        String sql = "SELECT userPassword FROM UserAccounts WHERE userName = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                String storedPassword = resultSet.getString("userPassword");
                return storedPassword.equals(password); // Check if the entered password matches the stored password
            }
        } catch (SQLException e) {
            System.out.println("Error while authenticating user: " + e.getMessage());
        }
        return false; // Return false if user not found or exception occurs
    }

    void accountCreationMenu() {
        System.out.println("+---------------------------------------------------------+");
        System.out.println("|                   Java Bank Application                 |");
        System.out.println("+---------------------------------------------------------+");
        System.out.println("|                      Account Creation                   |");
        System.out.println("|                        A. Continue                      |");
        System.out.println("|                        B. Quit                          |");
        System.out.println("+---------------------------------------------------------+");
    }

    public void UserAccountRegistration() {
        String userRegistration;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        accountCreationMenu();
    
        do {
            System.out.print("Enter your choice: ");
            userRegistration = scan.nextLine();
    
            if (!userRegistration.equalsIgnoreCase("A") && !userRegistration.equalsIgnoreCase("B")) {
                System.out.println("Invalid Choice. Please input (A or B).");
            } else if (userRegistration.equalsIgnoreCase("A")) {
                System.out.println("Processing Account Creation");
                System.out.print("Input name: ");
                String userName = scan.nextLine();
    
                // Additional details (pin, birthdate, phone)
                String pin;
                do {
                    System.out.print("Input 4-digit pin: ");
                    pin = scan.nextLine();
                } while (!isValidPin(pin));
    
                LocalDate birthdate = null;
                boolean validDate = false;
                while (!validDate) {
                    System.out.print("Enter birthdate (dd-MM-yyyy): ");
                    String inputDate = scan.nextLine();
                    try {
                        birthdate = LocalDate.parse(inputDate, formatter);
                        validDate = true;
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date format. Please use dd-MM-yyyy.");
                    }
                }
    
                System.out.print("Enter phone number: ");
                long phoneNumber = scan.nextLong();
                scan.nextLine(); // Clear the buffer
    
                // Register account in the database
                AccountRegistration(userName, pin, birthdate, phoneNumber);
            }
        } while (!userRegistration.equalsIgnoreCase("B"));
    }

    private void AccountRegistration(String userName, String pin, LocalDate birthdate, long phoneNumber) {
        System.out.println("+--------------------------------------+");
        System.out.println("|         ACCOUNT REGISTRATION         |");
        System.out.println("+------------+-------------------------+");
        System.out.printf("| NAME       | %-23s |\n", userName);
        System.out.printf("| PIN        | %-23s |\n", "****");      // Masked pin
        System.out.printf("| BIRTHDATE  | %-23s |\n", birthdate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        System.out.printf("| NUMBER     | %-23d |\n", phoneNumber);
        System.out.println("+------------+-------------------------+");

        String sql = "INSERT INTO UserAccounts (userName, userPassword, userBalance, pin, birthdate, phoneNumber) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, "defaultPassword"); // You may choose to use a default or leave it blank
            pstmt.setDouble(3, 0.0); // Initial balance
            pstmt.setString(4, pin);
            pstmt.setDate(5, java.sql.Date.valueOf(birthdate));
            pstmt.setLong(6, phoneNumber);

            pstmt.executeUpdate();
            System.out.println("Account registered successfully!");
        } catch (SQLException e) {
            System.out.println("Failed to register account.");
            e.printStackTrace();
        }
    }

    private boolean isValidPin(String pin) {
        return pin.matches("\\d{4}"); // Checks if pin is exactly 4 digits
    }

    public static Connection getConnection() {
        String dbURL = "jdbc:mysql://localhost:3306/BankingSystemDB";
        String username = "root";
        String password = "pokemon2626";

        try {
            return DriverManager.getConnection(dbURL, username, password);
        } catch (SQLException e) {
            log.error("Failed to connect to the database: {}", e.getMessage());
            return null; // Optionally, return null or rethrow the exception
        }
    }

    public boolean isUserAccountTableEmpty() {
        String countSql = "SELECT COUNT(*) FROM UserAccounts";
        try (Connection conn = getConnection();
             PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            var resultSet = countStmt.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count == 0; // Returns true if the table is empty
            }
        } catch (SQLException e) {
            System.out.println("Failed to check if UserAccounts table is empty.");
            e.printStackTrace();
        }
        return false; // Return false in case of an exception
    }
}
