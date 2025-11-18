package com.example.banking;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label accountNumberLabel;

    @FXML
    private Label accountTypeLabel;

    @FXML
    private Label balanceLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private VBox transactionList;

    private static final String CSV_FILE = "accounts.csv";
    private static final String TRANSACTIONS_FILE = "transactions.csv";
    private Account currentAccount;
    private DecimalFormat currencyFormat = new DecimalFormat("NRP#,##0.00");

    @FXML
    private void initialize() {
        loadCurrentAccountData();
        loadTransactionHistory();
        createTransactionFileIfNotExists();
    }

    private void loadCurrentAccountData() {
        String currentAccountNumber = UserSession.getCurrentUser();

        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 9 && parts[0].trim().equals(currentAccountNumber)) {
                    currentAccount = new Account(
                            parts[0].trim(),
                            parts[1].trim(),
                            parts[2].trim(),
                            parts[3].trim(),
                            parts[4].trim(),
                            parts[5].trim(),
                            parts[6].trim(),
                            parts[7].trim(),
                            Double.parseDouble(parts[8].trim())
                    );

                    updateUI();
                    break;
                }
            }
        } catch (IOException e) {
            showAlert("Error", "Error loading account data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateUI() {
        if (currentAccount != null) {
            welcomeLabel.setText("Welcome, " + currentAccount.getFirstName() + " " + currentAccount.getLastName());
            accountNumberLabel.setText(currentAccount.getAccountNumber());
            accountTypeLabel.setText(currentAccount.getAccountType());
            balanceLabel.setText(currencyFormat.format(currentAccount.getBalance()));
        }
    }

    @FXML
    private void handleDeposit() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Deposit Money");
        dialog.setHeaderText("Enter deposit amount");
        dialog.setContentText("Amount ($):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double amount = Double.parseDouble(result.get());
                if (amount > 0) {
                    currentAccount.setBalance(currentAccount.getBalance() + amount);
                    updateAccountInCSV();
                    updateUI();
                    addTransaction("DEPOSIT", amount, "Deposit to account");
                    loadTransactionHistory();
                    showAlert("Success", "Successfully deposited " + currencyFormat.format(amount), Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Amount must be greater than 0", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid amount", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleWithdraw() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Withdraw Money");
        dialog.setHeaderText("Enter withdrawal amount");
        dialog.setContentText("Amount ($):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double amount = Double.parseDouble(result.get());
                if (amount > 0) {
                    if (amount <= currentAccount.getBalance()) {
                        currentAccount.setBalance(currentAccount.getBalance() - amount);
                        updateAccountInCSV();
                        updateUI();
                        addTransaction("WITHDRAWAL", amount, "Withdrawal from account");
                        loadTransactionHistory();
                        showAlert("Success", "Successfully withdrew " + currencyFormat.format(amount), Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Error", "Insufficient balance", Alert.AlertType.ERROR);
                    }
                } else {
                    showAlert("Error", "Amount must be greater than 0", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid amount", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleAddInterest() {
        if (currentAccount.getAccountType().equals("Savings Account")) {
            double interestRate = 0.05; // 5% annual interest
            double interest = currentAccount.getBalance() * interestRate;
            currentAccount.setBalance(currentAccount.getBalance() + interest);
            updateAccountInCSV();
            updateUI();
            addTransaction("INTEREST", interest, "Interest added to savings account");
            loadTransactionHistory();
            showAlert("Success", "Interest of " + currencyFormat.format(interest) + " added to your account", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Information", "Interest is only available for Savings Accounts", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleViewAllAccounts() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("All Accounts");
        alert.setHeaderText("Account Information");

        StringBuilder accountsInfo = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 9) {
                    accountsInfo.append("Account: ").append(parts[0].trim())
                            .append(" | Name: ").append(parts[1].trim()).append(" ").append(parts[2].trim())
                            .append(" | Type: ").append(parts[7].trim())
                            .append(" | Balance: ").append(currencyFormat.format(Double.parseDouble(parts[8].trim())))
                            .append("\n");
                }
            }
        } catch (IOException e) {
            accountsInfo.append("Error loading accounts: ").append(e.getMessage());
        }

        TextArea textArea = new TextArea(accountsInfo.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setExpandableContent(textArea);
        alert.getDialogPane().setExpanded(true);
        alert.showAndWait();
    }

    @FXML
    private void handleCreateNewAccount() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Signup.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("Create New Account");
            newStage.show();

        } catch (Exception e) {
            showAlert("Error", "Error opening signup page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Logout");
        confirmAlert.setHeaderText("Are you sure you want to logout?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                UserSession.clearSession();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Login - Digital Banking");

            } catch (Exception e) {
                showAlert("Error", "Error during logout: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleExit() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Exit Application");
        confirmAlert.setHeaderText("Are you sure you want to exit?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.exit(0);
        }
    }

    private void updateAccountInCSV() {
        try {
            List<String> lines = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 1 && parts[0].trim().equals(currentAccount.getAccountNumber())) {
                        // Update this account's balance
                        line = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%.2f",
                                currentAccount.getAccountNumber(),
                                currentAccount.getFirstName(),
                                currentAccount.getLastName(),
                                currentAccount.getEmail(),
                                currentAccount.getPhone(),
                                currentAccount.getAddress(),
                                currentAccount.getPassword(),
                                currentAccount.getAccountType(),
                                currentAccount.getBalance());
                    }
                    lines.add(line);
                }
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
                for (String line : lines) {
                    writer.println(line);
                }
            }

        } catch (IOException e) {
            showAlert("Error", "Error updating account: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void addTransaction(String type, double amount, String description) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TRANSACTIONS_FILE, true))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            writer.println(String.format("%s,%s,%s,%.2f,%s,%s",
                    currentAccount.getAccountNumber(),
                    LocalDateTime.now().format(formatter),
                    type,
                    amount,
                    description,
                    currencyFormat.format(currentAccount.getBalance())));
        } catch (IOException e) {
            System.err.println("Error saving transaction: " + e.getMessage());
        }
    }

    private void loadTransactionHistory() {
        transactionList.getChildren().clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(TRANSACTIONS_FILE))) {
            String line;
            List<String> transactions = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6 && parts[0].trim().equals(currentAccount.getAccountNumber())) {
                    transactions.add(line);
                }
            }

            // Show only last 10 transactions
            int start = Math.max(0, transactions.size() - 10);
            for (int i = transactions.size() - 1; i >= start; i--) {
                String[] parts = transactions.get(i).split(",");
                createTransactionCard(parts[1], parts[2], Double.parseDouble(parts[3]), parts[4]);
            }

            if (transactions.isEmpty()) {
                Label noTransactions = new Label("No transactions yet");
                noTransactions.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
                transactionList.getChildren().add(noTransactions);
            }

        } catch (IOException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
        }
    }

    private void createTransactionCard(String date, String type, double amount, String description) {
        HBox card = new HBox();
        card.setSpacing(15);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #e9ecef; -fx-border-radius: 8;");

        VBox leftContent = new VBox(5);
        Label typeLabel = new Label(type);
        typeLabel.setFont(Font.font("System Bold", 14));

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #6c757d;");

        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11;");

        leftContent.getChildren().addAll(typeLabel, descLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amountLabel = new Label(currencyFormat.format(amount));
        amountLabel.setFont(Font.font("System Bold", 16));

        if (type.equals("DEPOSIT") || type.equals("INTEREST")) {
            amountLabel.setStyle("-fx-text-fill: #28a745;");
        } else {
            amountLabel.setStyle("-fx-text-fill: #dc3545;");
        }

        card.getChildren().addAll(leftContent, spacer, amountLabel);
        transactionList.getChildren().add(card);
    }

    private void createTransactionFileIfNotExists() {
        File file = new File(TRANSACTIONS_FILE);
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(TRANSACTIONS_FILE))) {
                writer.println("AccountNumber,DateTime,Type,Amount,Description,Balance");
            } catch (IOException e) {
                System.err.println("Error creating transactions file: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}