package com.library;

import com.library.utils.AlertUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/library/views/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setTitle("Library Portal - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            AlertUtils.showError("Failed to start", "Unable to load login view: " + ex.getMessage());
            throw new RuntimeException("Unable to start JavaFX application", ex);
        }
    }
}
