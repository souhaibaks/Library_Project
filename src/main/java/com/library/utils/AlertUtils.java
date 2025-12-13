package com.library.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Convenience helpers for showing JavaFX alerts without repeating boilerplate.
 */
public final class AlertUtils {

    private AlertUtils() {
    }

    /**
     * Shows an informational alert with the provided title and message.
     */
    public static void showInfo(String title, String message) {
        show(AlertType.INFORMATION, title, message);
    }

    /**
     * Shows a warning dialog.
     */
    public static void showWarning(String title, String message) {
        show(AlertType.WARNING, title, message);
    }

    /**
     * Shows an error dialog.
     */
    public static void showError(String title, String message) {
        show(AlertType.ERROR, title, message);
    }

    private static void show(AlertType type, String title, String message) {
        if (Platform.isFxApplicationThread()) {
            display(type, title, message);
        } else {
            Platform.runLater(() -> display(type, title, message));
        }
    }

    private static void display(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}

