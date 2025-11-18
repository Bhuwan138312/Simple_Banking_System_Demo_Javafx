package com.example.banking;

public class UserSession {
    private static String currentUser;

    public static void setCurrentUser(String accountNumber) {
        currentUser = accountNumber;
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public static void clearSession() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null && !currentUser.isEmpty();
    }
}