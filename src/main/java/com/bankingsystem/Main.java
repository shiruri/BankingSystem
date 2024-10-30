package com.bankingsystem;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Initialize user account object
        UserAccount userAccount = new UserAccount();
        Scanner scan = new Scanner(System.in);

        // Check if the UserAccounts table is empty
        if (userAccount.isUserAccountTableEmpty()) {
            System.out.println("No user accounts found. Please create a new account.");
        } else {
            System.out.println("Existing user accounts found. Please log in.");
        }

        // Show menu for creating an account or logging in
        userAccount.menuLoginOrCreate();

        // Close the scanner to prevent resource leaks
        scan.close();
    }
}
