package com.library.models;

import com.library.models.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Users
 */
public class UserDAO {
    
    /**
     * Fetches all users from the database
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
            e.printStackTrace();
        }
        
        return users;
    }
    
    /**
     * Gets a user by ID
     */
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Gets a user by email
     */
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by email: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Finds a user by full name (first name and last name)
     * Returns the first matching user if found
     */
    public User getUserByName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return null;
        }
        
        String[] nameParts = fullName.trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        
        String sql;
        PreparedStatement pstmt;
        
        try (Connection conn = DBConnection.getConnection()) {
            if (lastName.isEmpty()) {
                // Search by first name only
                sql = "SELECT * FROM users WHERE first_name = ? LIMIT 1";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, firstName);
            } else {
                // Search by both first and last name
                sql = "SELECT * FROM users WHERE first_name = ? AND last_name = ? LIMIT 1";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, firstName);
                pstmt.setString(2, lastName);
            }
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by name: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Inserts a new user into the database
     * Note: The database doesn't store passwords, so password handling is done separately
     */
    public int insertUser(User user) {
        String sql = "INSERT INTO users (first_name, last_name, email, phone_number, registration_date, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getFirstName());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPhoneNumber());
            pstmt.setDate(5, user.getRegistrationDate() != null ? 
                         Date.valueOf(user.getRegistrationDate()) : Date.valueOf(LocalDate.now()));
            pstmt.setBoolean(6, user.isActive());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        user.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Updates a user in the database
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, phone_number = ?, " +
                     "registration_date = ?, is_active = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getFirstName());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPhoneNumber());
            pstmt.setDate(5, user.getRegistrationDate() != null ? 
                         Date.valueOf(user.getRegistrationDate()) : null);
            pstmt.setBoolean(6, user.isActive());
            pstmt.setInt(7, user.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Checks if a user with the given email exists
     */
    public boolean userExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if user exists: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Maps a ResultSet row to a User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setPhoneNumber(rs.getString("phone_number"));
        
        Date regDate = rs.getDate("registration_date");
        if (regDate != null) {
            user.setRegistrationDate(regDate.toLocalDate());
        }
        
        user.setActive(rs.getBoolean("is_active"));
        
        return user;
    }
}

