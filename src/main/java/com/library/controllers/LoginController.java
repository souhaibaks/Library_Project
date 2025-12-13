package com.library.controllers;

import com.library.models.User;
import com.library.services.UserService;
import com.library.utils.AlertUtils;
import com.library.utils.DateUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Simple in-memory authentication demo that wires the login.fxml controls.
 */
public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        statusLabel.setText("Please sign in to continue.");
    }

    @FXML
    private void onLogin() {
        if (!isFilled(emailField) || !isFilled(passwordField)) {
            AlertUtils.showWarning("Missing credentials", "Enter both email and password.");
            return;
        }

        String email = emailField.getText();
        String password = passwordField.getText();
        
        User user = UserService.getInstance().authenticate(email, password);

        if (user != null) {
            statusLabel.setText("Signed in as %s".formatted(user.getFullName()));
            
            // Navigate to Dashboard
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/library/views/dashboard.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setTitle("Library Workspace — " + DateUtils.format(LocalDate.now()));
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
                AlertUtils.showError("Login Error", "Could not load the dashboard.");
            }

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
        statusLabel.setText("Please sign in to continue.");
    }

    @FXML
    private void onCreateAccount() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/library/views/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("Library Portal — Create Account");
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Navigation Error", "Could not load the registration screen.");
        }
    }

    private boolean isFilled(TextInputControl control) {
        return control.getText() != null && !control.getText().trim().isEmpty();
    }
}
