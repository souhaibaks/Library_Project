package com.library.controllers;

import com.library.models.User;
import com.library.services.UserService;
import com.library.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private void onRegister() {
        if (!isFilled(firstNameField) || !isFilled(lastNameField) || 
            !isFilled(emailField) || !isFilled(passwordField)) {
            AlertUtils.showWarning("Missing Fields", "Please fill in all fields to create an account.");
            return;
        }

        // In a real app, this would save to the database.
        // For this demo, we'll just simulate success and go back to login.
        
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Register via UserService
        User newUser = new User((int)(System.currentTimeMillis() / 1000), firstName, lastName, email, "");
        boolean success = UserService.getInstance().register(newUser, password);
        
        if (success) {
            AlertUtils.showInfo("Registration Successful", 
                "Account created for %s %s! Logging you in now.".formatted(firstName, lastName));
            
            navigateToDashboard();
        } else {
            AlertUtils.showError("Registration Failed", "An account with this email already exists.");
        }
    }

    @FXML
    private void onCancel() {
        navigateToLogin();
    }

    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/library/views/dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("Library Portal - Dashboard");
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Navigation Error", "Could not load the dashboard.");
        }
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/library/views/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("Library Portal - Login");
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Navigation Error", "Could not return to login screen.");
        }
    }

    private boolean isFilled(TextField field) {
        return field.getText() != null && !field.getText().trim().isEmpty();
    }
}
