package com.library.services;

import com.library.models.User;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton service to manage users and authentication.
 * Keeps data consistent across the application lifecycle.
 */
public class UserService {
    private static UserService instance;
    
    // In-memory storage for users and credentials
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, String> credentials = new HashMap<>();
    private User currentUser;

    private UserService() {
        // Private constructor to prevent instantiation
        // No seed users as requested ("remove all existing users")
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Registers a new user.
     * @param user The user object (email must be set)
     * @param password The plain text password
     * @return true if successful, false if email already exists
     */
    public boolean register(User user, String password) {
        String emailKey = user.getEmail().toLowerCase().trim();
        if (users.containsKey(emailKey)) {
            return false; // User already exists
        }
        users.put(emailKey, user);
        credentials.put(emailKey, password);
        // Auto-login after registration
        currentUser = user; 
        return true;
    }

    /**
     * Authenticates a user.
     * @param email The email address
     * @param password The password
     * @return The User object if successful, null otherwise
     */
    public User authenticate(String email, String password) {
        String emailKey = email.toLowerCase().trim();
        String storedPassword = credentials.get(emailKey);
        
        if (storedPassword != null && storedPassword.equals(password)) {
            currentUser = users.get(emailKey);
            return currentUser;
        }
        return null;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isUserRegistered(String email) {
        return users.containsKey(email.toLowerCase().trim());
    }
}
