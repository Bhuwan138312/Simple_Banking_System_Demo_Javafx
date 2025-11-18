package com.example.banking;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import java.io.*;
import java.text.DecimalFormat;
import java.util.Random;

public class SignupController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    // Address field removed as it's not in the FXML

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField initialDepositField;

    @FXML
    private ComboBox<String> accountTypeCombo;

    @FXML
    private Button createAccountButton;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private static final String CSV_FILE = "accounts.csv";

    @FXML
    private void initialize() {
        createCSVFileIfNotExists();

        // Initialize ComboBox with account types matching FXML
        if (accountTypeCombo.getItems().isEmpty()) {
            accountTypeCombo.getItems().addAll("Current Account", "Savings Account");
        }
    }

    @FXML
    private void handleSignup() {
        try {
            System.out.println("Starting signup process..."); // Debug log

            validateFieldsOrThrow();

            String accountNumber = generateAccountNumber();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = ""; // No address field in FXML
            String password = passwordField.getText();
            String accountType = accountTypeCombo.getValue();
            double initialDeposit = Double.parseDouble(initialDepositField.getText().trim());

            System.out.println("Generated account number: " + accountNumber); // Debug log

            if (emailExists(email)) {
                throw new FormValidationException("An account with this email already exists");
            }

            System.out.println("Saving account to CSV..."); // Debug log
            saveAccountToCSV(accountNumber, firstName, lastName, email, phone, address, password, accountType, initialDeposit);

            clearForm();
            showSignupSuccessPopup(accountNumber, password);
            System.out.println("Signup completed successfully!"); // Debug log

        } catch (FormValidationException e) {
            System.err.println("Validation error: " + e.getMessage());
            showError(e.getMessage());
            showPopup(e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Number format error: " + e.getMessage());
            showError("Please enter a valid initial deposit amount");
            showPopup("Please enter a valid initial deposit amount");
        } catch (Exception e) {
            // Log the detailed error for debugging
            System.err.println("Unexpected error during signup: " + e.getMessage());
            e.printStackTrace();

            // Show only a user-friendly message for unknown errors
            showError("Something went wrong. Please check your details and try again.");
            showPopup("Something went wrong. Please check your details and try again.");
        }
    }

    /**
     * Throws FormValidationException if any required field is not filled or invalid.
     */
    private void validateFieldsOrThrow() throws FormValidationException {
        StringBuilder errors = new StringBuilder();

        if (firstNameField.getText().trim().isEmpty()) {
            errors.append("First name is required\n");
        }
        if (lastNameField.getText().trim().isEmpty()) {
            errors.append("Last name is required\n");
        }
        if (emailField.getText().trim().isEmpty()) {
            errors.append("Email is required\n");
        } else if (!isValidEmail(emailField.getText().trim())) {
            errors.append("Please enter a valid email address\n");
        }
        if (phoneField.getText().trim().isEmpty()) {
            errors.append("Phone number is required\n");
        }
        // Address field not present in FXML, so skip validation
        if (passwordField.getText().isEmpty()) {
            errors.append("Password is required\n");
        } else if (passwordField.getText().length() < 6) {
            errors.append("Password must be at least 6 characters long\n");
        }
        if (initialDepositField.getText().trim().isEmpty()) {
            errors.append("Initial deposit is required\n");
        } else {
            try {
                double deposit = Double.parseDouble(initialDepositField.getText().trim());
                if (deposit < 100) {
                    errors.append("Minimum initial deposit is $100\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Please enter a valid deposit amount\n");
            }
        }
        if (accountTypeCombo.getValue() == null || accountTypeCombo.getValue().isEmpty()) {
            errors.append("Please select an account type\n");
        }

        if (errors.length() > 0) {
            throw new FormValidationException(errors.toString().trim());
        }
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }

    private boolean emailExists(String email) {
        File csvFile = new File(CSV_FILE);
        if (!csvFile.exists()) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // Skip empty lines

                String[] parts = line.split(",", -1); // -1 to include empty strings
                if (parts.length >= 4 && parts[3].trim().equalsIgnoreCase(email)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error checking email: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder accountNumber = new StringBuilder("ACC");

        for (int i = 0; i < 7; i++) {
            accountNumber.append(random.nextInt(10));
        }

        // Avoid infinite recursion by limiting attempts
        String generatedNumber = accountNumber.toString();
        int attempts = 0;
        while (accountNumberExists(generatedNumber) && attempts < 100) {
            accountNumber = new StringBuilder("ACC");
            for (int i = 0; i < 7; i++) {
                accountNumber.append(random.nextInt(10));
            }
            generatedNumber = accountNumber.toString();
            attempts++;
        }

        return generatedNumber;
    }

    private boolean accountNumberExists(String accountNumber) {
        File csvFile = new File(CSV_FILE);
        if (!csvFile.exists()) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // Skip empty lines

                String[] parts = line.split(",", -1);
                if (parts.length >= 1 && parts[0].trim().equals(accountNumber)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error checking account number: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private void saveAccountToCSV(String accountNumber, String firstName, String lastName,
                                  String email, String phone, String address, String password,
                                  String accountType, double balance) throws IOException {

        // Ensure the CSV file exists
        createCSVFileIfNotExists();

        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE, true))) {
            DecimalFormat df = new DecimalFormat("#.00");

            // Escape any commas in the data by replacing them with semicolons
            firstName = firstName.replace(",", ";");
            lastName = lastName.replace(",", ";");
            email = email.replace(",", ";");
            phone = phone.replace(",", ";");
            address = address.replace(",", ";");
            accountType = accountType.replace(",", ";");

            String csvLine = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    accountNumber, firstName, lastName, email, phone, address, password, accountType, df.format(balance));

            System.out.println("Writing to CSV: " + csvLine); // Debug log
            writer.println(csvLine);
            writer.flush(); // Ensure data is written immediately
        }
    }

    private void createCSVFileIfNotExists() {
        File file = new File(CSV_FILE);
        if (!file.exists()) {
            try {
                // Create parent directories if they don't exist
                file.getParentFile().mkdirs();

                try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
                    writer.println("AccountNumber,FirstName,LastName,Email,Phone,Address,Password,AccountType,Balance");
                    writer.flush();
                }
                System.out.println("Created CSV file: " + file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error creating CSV file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        // No addressField to clear
        passwordField.clear();
        initialDepositField.clear();
        accountTypeCombo.setValue(null);
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
        if (successLabel != null) {
            successLabel.setVisible(false);
        }
    }

    private void showSuccess(String message) {
        if (successLabel != null) {
            successLabel.setText(message);
            successLabel.setVisible(true);
        }
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }

    // Show popup dialog for validation errors
    private void showPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Please correct the following issues:");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Show popup dialog for successful signup with account number and password
    private void showSignupSuccessPopup(String accountNumber, String password) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sign Up Successful!");
        alert.setHeaderText("Successfully signed up!");
        alert.setContentText("Your account number is: " + accountNumber
                + "\nYour password is: " + password
                + "\n\nPlease save these details for login.");

        // Optional: Add a "Copy Details" button for account number and password
        ButtonType copyBtn = new ButtonType("Copy Details", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(copyBtn, closeBtn);

        alert.showAndWait().ifPresent(response -> {
            if (response == copyBtn) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString("Account Number: " + accountNumber + "\nPassword: " + password);
                clipboard.setContent(content);
            }
        });
    }

    public void HandleLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login - Digital Banking");

        } catch (Exception e) {
            showError("Error loading login page.");
            showPopup("Error loading login page.");
            e.printStackTrace();
        }
    }
}