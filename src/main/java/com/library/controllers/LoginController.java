package com.library.controllers;

import com.library.models.User;
import com.library.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Simple in-memory authentication demo that wires the login.fxml controls.
 */
public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox rememberMeCheck;
    @FXML
    private Label statusLabel;

    private final Map<String, String> credentials = new HashMap<>();
    private final Map<String, User> users = new HashMap<>();

    @FXML
    private void initialize() {
        seedUsers();
        statusLabel.setText("Please sign in to continue.");
    }

    private void seedUsers() {
        if (!credentials.isEmpty()) {
            return;
        }
        registerUser(new User(1, "Ava", "Nguyen", "ava@library.local", "555-0100"), "welcome1!");
        registerUser(new User(2, "Noah", "Silva", "noah@library.local", "555-0110"), "welcome2!");
        registerUser(new User(3, "Liam", "Turner", "liam@library.local", "555-0120"), "welcome3!");
    }

    private void registerUser(User user, String password) {
        String key = key(user.getEmail());
        users.put(key, user);
        credentials.put(key, password);
    }

    @FXML
    private void onLogin() {
        if (!isFilled(emailField) || !isFilled(passwordField)) {
            AlertUtils.showWarning("Missing credentials", "Enter both email and password.");
            return;
        }

        String email = key(emailField.getText());
        String password = passwordField.getText();
        String expected = credentials.get(email);

        if (expected != null && expected.equals(password)) {
            User user = users.get(email);
            statusLabel.setText("Signed in as %s".formatted(user.getFullName()));
            AlertUtils.showInfo("Login successful",
                    "Welcome back, %s%s".formatted(
                            user.getFirstName(),
                            rememberMeCheck.isSelected() ? " (session remembered)" : ""));
            passwordField.clear();
        } else {
            statusLabel.setText("Authentication failed");
            AlertUtils.showError("Invalid credentials",
                    "We couldn't match that email and password. Please try again.");
        }
    }

    @FXML
    private void onClearForm() {
        emailField.clear();
        passwordField.clear();
        rememberMeCheck.setSelected(false);
        statusLabel.setText("Please sign in to continue.");
    }

    @FXML
    private void onForgotPassword() {
        if (!isFilled(emailField)) {
            AlertUtils.showInfo("Reset instructions", "Enter your email first so we know who to help.");
            return;
        }
        String email = key(emailField.getText());
        if (!credentials.containsKey(email)) {
            AlertUtils.showWarning("Unknown email",
                    "We don't have an account for \"%s\" yet.".formatted(emailField.getText().trim()));
            return;
        }
        AlertUtils.showInfo("Reset link sent",
                "Check %s for a temporary password.".formatted(emailField.getText().trim()));
    }

    private boolean isFilled(TextInputControl control) {
        return control.getText() != null && !control.getText().trim().isEmpty();
    }

    private String key(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ENGLISH);
    }
}
