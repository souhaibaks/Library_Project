#!/bin/bash

# Define the path to JavaFX SDK
# This path was provided by the user.
JAVAFX_LIB="/Library/Java/JavaFX/javafx-sdk-17.0.17/lib"

# Check if the JavaFX SDK exists at the specified path
if [ ! -d "$JAVAFX_LIB" ]; then
    echo "Warning: JavaFX SDK not found at $JAVAFX_LIB"
    # Fallback to local lib directory if it exists
    if [ -d "lib" ]; then
        echo "Using local lib directory."
        JAVAFX_LIB="lib"
    else
        echo "Error: JavaFX SDK not found. Please ensure JavaFX is installed or the path is correct."
        exit 1
    fi
fi

echo "Using JavaFX SDK at: $JAVAFX_LIB"

# Create a directory for compiled classes
mkdir -p bin

# Clean previous build
rm -rf bin/*

# Find all Java source files
echo "Finding source files..."
find src/main/java -name "*.java" > sources.txt

# Compile the project
echo "Compiling..."
javac --module-path "$JAVAFX_LIB" \
      --add-modules javafx.controls,javafx.fxml,javafx.graphics \
      -d bin \
      -sourcepath src/main/java \
      @sources.txt

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful."
    rm sources.txt
    
    echo "Running application..."
    
    # Run the application
    # We include src/main/resources in the classpath so resources can be loaded
    java --module-path "$JAVAFX_LIB" \
         --add-modules javafx.controls,javafx.fxml,javafx.graphics \
         -cp bin:src/main/resources \
         com.library.Main
else
    echo "Compilation failed."
    rm sources.txt
    exit 1
fi
