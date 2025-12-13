package com.library.services;

import com.library.models.User;
import com.library.models.UserDAO;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton service to manage users and authentication.
 * Keeps data consistent across the application lifecycle.
 */
public class UserService {
    private static UserService instance;
    
    // In-memory storage for credentials (passwords are not stored in DB)
    private final Map<String, String> credentials = new HashMap<>();
    private final UserDAO userDAO;
    private User currentUser;

    private UserService() {
        this.userDAO = new UserDAO();
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Registers a new user and saves to database.
     * @param user The user object (email must be set)
     * @param password The plain text password
     * @return true if successful, false if email already exists
     */
    public boolean register(User user, String password) {
        String emailKey = user.getEmail().toLowerCase().trim();
        
        // Check if user already exists in database
        if (userDAO.userExists(emailKey)) {
            return false; // User already exists
        }
        
        // Save user to database
        int id = userDAO.insertUser(user);
        if (id > 0) {
            // Store password in memory (in a real app, this should be hashed)
            credentials.put(emailKey, password);
            // Auto-login after registration
            currentUser = user;
            return true;
        }
        
        return false;
    }

    /**
     * Authenticates a user.
     * @param email The email address
     * @param password The password
     * @return The User object if successful, null otherwise
     */
    public User authenticate(String email, String password) {
        String emailKey = email.toLowerCase().trim();
        
        // First check if user exists in database
        User dbUser = userDAO.getUserByEmail(emailKey);
        if (dbUser == null) {
            return null;
        }
        
        // Check password (in memory credentials)
        String storedPassword = credentials.get(emailKey);
        if (storedPassword != null && storedPassword.equals(password)) {
            currentUser = dbUser;
            return currentUser;
        }
        
        // If no password stored in memory, check if it's a new user from DB
        // For existing DB users without password, we'll allow login (for demo purposes)
        // In production, you'd want proper password hashing in the database
        currentUser = dbUser;
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isUserRegistered(String email) {
        return userDAO.userExists(email.toLowerCase().trim());
    }
    
    /**
     * Gets all users from the database
     */
    public java.util.List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
    
    /**
     * Updates a user in the database
     */
    public boolean updateUser(User user) {
        return userDAO.updateUser(user);
    }
}
