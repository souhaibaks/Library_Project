package com.library;

import com.library.utils.AlertUtils;
import com.library.utils.DateUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Primary JavaFX Application that loads the books screen.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/library/views/books.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setTitle("Library Catalogue — " + DateUtils.format(LocalDate.now()));
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            AlertUtils.showError("Failed to start", "Unable to load books view: " + ex.getMessage());
            throw new RuntimeException("Unable to start JavaFX application", ex);
        }
    }
}

