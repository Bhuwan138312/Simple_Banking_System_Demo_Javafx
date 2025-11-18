package com.example.banking;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.*;

public class LoginController {

    @FXML
    private TextField accountNumberField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button signupButton;

    @FXML
    private Label errorLabel;

    private static final String CSV_FILE = "accounts.csv";

    @FXML
    private void initialize() {
        // Create CSV file with headers if it doesn't exist
        createCSVFileIfNotExists();
    }

    @FXML
    private void handleLogin() {
        String accountNumber = accountNumberField.getText().trim();
        String password = passwordField.getText();

        if (accountNumber.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        if (validateLogin(accountNumber, password)) {
            try {
                // Store current user info in session
                UserSession.setCurrentUser(accountNumber);
                // Load dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Banking Dashboard");
            } catch (Exception e) {
                showError("Error loading dashboard: " + e.getMessage());
            }
        } else {
            showError("Invalid account number or password");
        }
    }

    @FXML
    private void handleSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Signup.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Create Account");

        } catch (Exception e) {
            showError("Error loading signup page: " + e.getMessage());
        }
    }

    private boolean validateLogin(String accountNumber, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 8) {
                    String storedAccountNumber = parts[0].trim();
                    String storedPassword = parts[6].trim();

                    if (storedAccountNumber.equals(accountNumber) && storedPassword.equals(password)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            showError("Error reading account data: " + e.getMessage());
        }

        return false;
    }

    private void createCSVFileIfNotExists() {
        File file = new File(CSV_FILE);
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
                writer.println("AccountNumber,FirstName,LastName,Email,Phone,Address,Password,AccountType,Balance");
            } catch (IOException e) {
                System.err.println("Error creating CSV file: " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}