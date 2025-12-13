package com.library.models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";
    private static Connection connection = null;
    
    private DBConnection() {
        // Private constructor to prevent instantiation
    }
    
    public static Connection getConnection() {
        try {
            // Check if connection is null or closed, then create a new one
            if (connection == null || connection.isClosed()) {
                // Load MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Create connection
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Database connection established successfully!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.err.println("Failed to connect to database!");
            e.printStackTrace();
            // Try to create a new connection
            try {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Database connection re-established successfully!");
            } catch (SQLException ex) {
                System.err.println("Failed to re-connect to database!");
                ex.printStackTrace();
                return null;
            }
        }
        return connection;
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database connection!");
                e.printStackTrace();
            }
        }
    }
    
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}

