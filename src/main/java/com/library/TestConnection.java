package com.library;

import com.library.models.DBConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        System.out.println("=== Database Connection Test ===\n");
        
        // Test connection
        System.out.println("1. Testing database connection...");
        boolean isConnected = DBConnection.testConnection();
        
        if (isConnected) {
            System.out.println("✓ Connection successful!\n");
            
            // Get connection details
            try {
                Connection conn = DBConnection.getConnection();
                if (conn != null) {
                    DatabaseMetaData metaData = conn.getMetaData();
                    
                    System.out.println("2. Database Information:");
                    System.out.println("   Database URL: " + metaData.getURL());
                    System.out.println("   Database Product: " + metaData.getDatabaseProductName());
                    System.out.println("   Database Version: " + metaData.getDatabaseProductVersion());
                    System.out.println("   Driver Name: " + metaData.getDriverName());
                    System.out.println("   Driver Version: " + metaData.getDriverVersion());
                    System.out.println("   Username: " + metaData.getUserName());
                    
                    System.out.println("\n✓ All connection details retrieved successfully!");
                }
            } catch (SQLException e) {
                System.err.println("✗ Error retrieving connection details: " + e.getMessage());
            }
        } else {
            System.err.println("✗ Connection failed!");
            System.err.println("\nPlease check:");
            System.err.println("  1. MySQL is installed and running");
            System.err.println("  2. Database 'library_db' exists");
            System.err.println("  3. Username and password are correct");
            System.err.println("  4. MySQL JDBC driver is in classpath");
        }
        
        // Close connection
        DBConnection.closeConnection();
        System.out.println("\n=== Test Complete ===");
    }
}

