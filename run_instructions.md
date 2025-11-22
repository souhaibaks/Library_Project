# How to Run JavaFX Application

## Current Issue
The JavaFX JAR files are present, but the native libraries (.dylib files) are missing.
JavaFX requires both JAR files and native libraries to run.

## Solution Options

### Option 1: Download JavaFX SDK (Recommended)
1. Download JavaFX SDK for macOS from: https://openjfx.io/
2. Extract the SDK
3. Copy native libraries from the SDK's lib folder to your project's lib folder
4. Then run: java --module-path lib --add-modules javafx.controls,javafx.graphics -cp src Main

### Option 2: Use JavaFX via Maven/Gradle
Set up a build tool that automatically manages JavaFX dependencies including native libraries.

### Option 3: Use OpenJFX via Homebrew (if available)
Try: brew install openjfx
Then configure the native library path accordingly.
