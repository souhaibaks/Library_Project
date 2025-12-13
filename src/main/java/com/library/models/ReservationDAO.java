package com.library.models;

import com.library.models.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Reservations
 */
public class ReservationDAO {
    
    private final LibraryItemDAO itemDAO = new LibraryItemDAO();
    private final UserDAO userDAO = new UserDAO();
    
    /**
     * Fetches all reservations from the database
     */
    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations ORDER BY reservation_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Reservation reservation = mapResultSetToReservation(rs);
                if (reservation != null) {
                    reservations.add(reservation);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reservations: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reservations;
    }
    
    /**
     * Gets a reservation by ID
     */
    public Reservation getReservationById(int id) {
        String sql = "SELECT * FROM reservations WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToReservation(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reservation by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Inserts a new reservation into the database
     */
    public int insertReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (user_id, item_id, reservation_date, due_date, return_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, reservation.getUser().getId());
            pstmt.setInt(2, reservation.getItem().getId());
            pstmt.setDate(3, reservation.getReservationDate() != null ? 
                         Date.valueOf(reservation.getReservationDate()) : Date.valueOf(LocalDate.now()));
            pstmt.setDate(4, reservation.getDueDate() != null ? 
                         Date.valueOf(reservation.getDueDate()) : null);
            pstmt.setDate(5, reservation.getReturnDate() != null ? 
                         Date.valueOf(reservation.getReturnDate()) : null);
            pstmt.setString(6, reservation.getStatus() != null ? 
                           reservation.getStatus().name() : Reservation.ReservationStatus.ACTIVE.name());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        reservation.setId(id);
                        
                        // Update item availability
                        updateItemAvailability(reservation.getItem().getId(), false);
                        
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting reservation: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Updates a reservation in the database
     */
    public boolean updateReservation(Reservation reservation) {
        String sql = "UPDATE reservations SET user_id = ?, item_id = ?, reservation_date = ?, " +
                     "due_date = ?, return_date = ?, status = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, reservation.getUser().getId());
            pstmt.setInt(2, reservation.getItem().getId());
            pstmt.setDate(3, reservation.getReservationDate() != null ? 
                         Date.valueOf(reservation.getReservationDate()) : null);
            pstmt.setDate(4, reservation.getDueDate() != null ? 
                         Date.valueOf(reservation.getDueDate()) : null);
            pstmt.setDate(5, reservation.getReturnDate() != null ? 
                         Date.valueOf(reservation.getReturnDate()) : null);
            pstmt.setString(6, reservation.getStatus() != null ? 
                           reservation.getStatus().name() : Reservation.ReservationStatus.ACTIVE.name());
            pstmt.setInt(7, reservation.getId());
            
            boolean updated = pstmt.executeUpdate() > 0;
            
            // Update item availability based on status
            if (updated) {
                boolean isAvailable = reservation.getStatus() == Reservation.ReservationStatus.RETURNED ||
                                     reservation.getStatus() == Reservation.ReservationStatus.CANCELLED;
                updateItemAvailability(reservation.getItem().getId(), isAvailable);
            }
            
            return updated;
        } catch (SQLException e) {
            System.err.println("Error updating reservation: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Updates the availability status of a library item
     */
    private void updateItemAvailability(int itemId, boolean isAvailable) {
        String sql = "UPDATE library_items SET is_available = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, isAvailable);
            pstmt.setInt(2, itemId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating item availability: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Maps a ResultSet row to a Reservation object
     */
    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        try {
            int userId = rs.getInt("user_id");
            int itemId = rs.getInt("item_id");
            
            User user = userDAO.getUserById(userId);
            LibraryItem item = itemDAO.getItemById(itemId);
            
            if (user == null || item == null) {
                System.err.println("Warning: Could not load user or item for reservation ID: " + rs.getInt("id"));
                return null;
            }
            
            Reservation reservation = new Reservation();
            reservation.setId(rs.getInt("id"));
            reservation.setUser(user);
            reservation.setItem(item);
            
            Date resDate = rs.getDate("reservation_date");
            if (resDate != null) {
                reservation.setReservationDate(resDate.toLocalDate());
            }
            
            Date dueDate = rs.getDate("due_date");
            if (dueDate != null) {
                reservation.setDueDate(dueDate.toLocalDate());
            }
            
            Date returnDate = rs.getDate("return_date");
            if (returnDate != null) {
                reservation.setReturnDate(returnDate.toLocalDate());
            }
            
            String statusStr = rs.getString("status");
            if (statusStr != null) {
                try {
                    reservation.setStatus(Reservation.ReservationStatus.valueOf(statusStr));
                } catch (IllegalArgumentException e) {
                    reservation.setStatus(Reservation.ReservationStatus.ACTIVE);
                }
            }
            
            return reservation;
        } catch (SQLException e) {
            System.err.println("Error mapping reservation: " + e.getMessage());
            throw e;
        }
    }
}

