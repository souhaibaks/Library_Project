package com.library;

import javafx.application.Application;

/**
 * Plain entry point that delegates to {@link App}.
 */
public final class Main {

    private Main() {
        // Prevent instantiation
    }

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}

