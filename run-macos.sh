#!/bin/bash

# macOS/Linux run script for Library Management System

# Get JavaFX SDK path from environment variable or use default
JAVAFX_LIB="${JAVAFX_LIB:-/Library/Java/JavaFX/javafx-sdk-17.0.17/lib}"

# Check if the JavaFX SDK exists at the specified path
if [ ! -d "$JAVAFX_LIB" ]; then
    echo "Warning: JavaFX SDK not found at $JAVAFX_LIB"
    # Fallback to local lib directory if it exists
    if [ -d "lib" ]; then
        echo "Using local lib directory."
        JAVAFX_LIB="lib"
    else
        echo "Error: JavaFX SDK not found. Please ensure JavaFX is installed or the path is correct."
        echo "Set JAVAFX_LIB environment variable or place JavaFX JARs in lib directory."
        exit 1
    fi
fi

echo "Using JavaFX SDK at: $JAVAFX_LIB"

# Find MySQL JDBC driver
# First, check environment variable (before setting local variable)
ENV_MYSQL_DRIVER="${MYSQL_DRIVER:-}"
MYSQL_DRIVER=""
if [ -n "$ENV_MYSQL_DRIVER" ] && [ -f "$ENV_MYSQL_DRIVER" ]; then
    MYSQL_DRIVER="$ENV_MYSQL_DRIVER"
    echo "Found MySQL JDBC driver from MYSQL_DRIVER environment variable: $MYSQL_DRIVER"
fi

# Check in lib_db directory (project-specific location)
if [ -z "$MYSQL_DRIVER" ]; then
    if [ -d "lib_db" ]; then
        MYSQL_JAR=$(find lib_db -name "mysql-connector-j*.jar" -o -name "mysql-connector*.jar" 2>/dev/null | head -n 1)
        if [ -n "$MYSQL_JAR" ] && [ -f "$MYSQL_JAR" ]; then
            MYSQL_DRIVER="$MYSQL_JAR"
            echo "Found MySQL JDBC driver in lib_db: $MYSQL_DRIVER"
        fi
    fi
fi

# Check common locations (macOS/Linux)
if [ -z "$MYSQL_DRIVER" ]; then
    MYSQL_JAR=$(ls "$HOME/Downloads/mysql-connector-j"*.jar 2>/dev/null | head -n 1)
    if [ -n "$MYSQL_JAR" ] && [ -f "$MYSQL_JAR" ]; then
        MYSQL_DRIVER="$MYSQL_JAR"
        echo "Found MySQL JDBC driver: $MYSQL_DRIVER"
    else
        MYSQL_JAR=$(ls "$HOME/Desktop/mysql-connector-j"*.jar 2>/dev/null | head -n 1)
        if [ -n "$MYSQL_JAR" ] && [ -f "$MYSQL_JAR" ]; then
            MYSQL_DRIVER="$MYSQL_JAR"
            echo "Found MySQL JDBC driver: $MYSQL_DRIVER"
        fi
    fi
fi

# Build classpath
CLASSPATH="bin:src/main/resources"
if [ -n "$MYSQL_DRIVER" ]; then
    CLASSPATH="$CLASSPATH:$MYSQL_DRIVER"
else
    echo "Warning: MySQL JDBC driver not found."
    echo "Set MYSQL_DRIVER environment variable or place the JAR in lib_db directory."
    echo "Database features will not work without the driver."
fi

# Try to find JavaFX SDK bin directory for native libraries (macOS)
JAVAFX_BIN=""
if [ "$JAVAFX_LIB" = "/Library/Java/JavaFX/javafx-sdk-17.0.17/lib" ] || [ "$JAVAFX_LIB" = "lib" ]; then
    if [ -d "/Library/Java/JavaFX/javafx-sdk-17.0.17/bin" ]; then
        JAVAFX_BIN="/Library/Java/JavaFX/javafx-sdk-17.0.17/bin"
    fi
fi

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
    
    # Build Java command for macOS/Linux
    JAVA_ARGS=(
        "--module-path" "$JAVAFX_LIB"
        "--add-modules" "javafx.controls,javafx.fxml,javafx.graphics"
    )
    
    # Add native library path if found
    if [ -n "$JAVAFX_BIN" ]; then
        JAVA_ARGS+=("-Djava.library.path=$JAVAFX_BIN")
    fi
    
    # macOS-specific: Use default graphics pipeline
    JAVA_ARGS+=("-cp" "$CLASSPATH" "com.library.Main")
    
    # Run the application
    java "${JAVA_ARGS[@]}"
else
    echo "Compilation failed."
    rm sources.txt
    exit 1
fi
