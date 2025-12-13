package com.library.models;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to hold raw reservation data from ResultSet
 */
class ReservationData {
    int id;
    int userId;
    int itemId;
    Date reservationDate;
    Date dueDate;
    Date returnDate;
    String status;
}

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
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                return reservations;
            }
            
            List<ReservationData> rawReservations = new ArrayList<>();
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    ReservationData data = new ReservationData();
                    data.id = rs.getInt("id");
                    data.userId = rs.getInt("user_id");
                    data.itemId = rs.getInt("item_id");
                    data.reservationDate = rs.getDate("reservation_date");
                    data.dueDate = rs.getDate("due_date");
                    data.returnDate = rs.getDate("return_date");
                    data.status = rs.getString("status");
                    rawReservations.add(data);
                }
            }
            
            for (ReservationData data : rawReservations) {
                Reservation reservation = buildReservationFromData(data);
                if (reservation != null) {
                    reservations.add(reservation);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reservations: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
        
        return reservations;
    }
    
    /**
     * Gets a reservation by ID
     */
    public Reservation getReservationById(int id) {
        String sql = "SELECT * FROM reservations WHERE id = ?";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                return null;
            }
            
            ReservationData data = null;
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        data = new ReservationData();
                        data.id = rs.getInt("id");
                        data.userId = rs.getInt("user_id");
                        data.itemId = rs.getInt("item_id");
                        data.reservationDate = rs.getDate("reservation_date");
                        data.dueDate = rs.getDate("due_date");
                        data.returnDate = rs.getDate("return_date");
                        data.status = rs.getString("status");
                    }
                }
            }
            
            if (data != null) {
                return buildReservationFromData(data);
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
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                return -1;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
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
                            updateItemAvailability(reservation.getItem().getId(), false);
                            return id;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting reservation: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
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
     * Builds a Reservation object from raw data
     * This method is called AFTER the ResultSet is closed, so it's safe to make other DB calls
     */
    private Reservation buildReservationFromData(ReservationData data) {
        try {
            int reservationId = data.id;
            int userId = data.userId;
            int itemId = data.itemId;
            
            // Now we can safely call other DAO methods since ResultSet is closed
            System.out.println("ReservationDAO: Loading user ID=" + userId + " for reservation ID=" + reservationId);
            User user = userDAO.getUserById(userId);
            
            if (user == null) {
                System.err.println("ReservationDAO: ERROR - Could not load user (ID: " + userId + ") for reservation ID: " + reservationId);
                System.err.println("ReservationDAO: This reservation will be skipped. Please ensure the user exists in the database.");
                System.err.println("ReservationDAO: Try running: SELECT * FROM users WHERE id = " + userId);
                return null;
            }
            
            System.out.println("ReservationDAO: User loaded successfully: " + user.getFullName());
            
            System.out.println("ReservationDAO: Loading item ID=" + itemId + " for reservation ID=" + reservationId);
            LibraryItem item = itemDAO.getItemById(itemId);
            
            if (item == null) {
                System.err.println("ReservationDAO: ERROR - Could not load item (ID: " + itemId + ") for reservation ID: " + reservationId);
                System.err.println("ReservationDAO: This reservation will be skipped. Please ensure the item exists in the database.");
                System.err.println("ReservationDAO: Try running: SELECT * FROM library_items WHERE id = " + itemId);
                return null;
            }
            
            System.out.println("ReservationDAO: Item loaded successfully: " + item.getTitle());
            
            // Build the reservation object with the extracted data
            Reservation reservation = new Reservation();
            reservation.setId(reservationId);
            reservation.setUser(user);
            reservation.setItem(item);
            
            if (data.reservationDate != null) {
                reservation.setReservationDate(data.reservationDate.toLocalDate());
            }
            
            if (data.dueDate != null) {
                reservation.setDueDate(data.dueDate.toLocalDate());
            }
            
            if (data.returnDate != null) {
                reservation.setReturnDate(data.returnDate.toLocalDate());
            }
            
            if (data.status != null) {
                try {
                    reservation.setStatus(Reservation.ReservationStatus.valueOf(data.status));
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: Invalid status '" + data.status + "' for reservation ID: " + reservationId + ". Defaulting to ACTIVE.");
                    reservation.setStatus(Reservation.ReservationStatus.ACTIVE);
                }
            } else {
                reservation.setStatus(Reservation.ReservationStatus.ACTIVE);
            }
            
            return reservation;
        } catch (Exception e) {
            System.err.println("Error building reservation from data: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Diagnostic method to check the state of the reservations table
     * This helps debug why reservations might not be loading
     */
    public void diagnoseReservationsTable() {
        // Force output immediately
        System.out.flush();
        System.err.flush();
        
        try {
            System.out.println("\n========== ReservationDAO Diagnostic ==========");
            System.out.println("Diagnostic method called at: " + new java.util.Date());
            System.out.flush();
            
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("ERROR: Database connection is null!");
                System.out.println("========== End Diagnostic ==========\n");
                return;
            }
            
            if (conn.isClosed()) {
                System.err.println("ERROR: Database connection is closed!");
                System.out.println("========== End Diagnostic ==========\n");
                return;
            }
            
            System.out.println("✓ Database connection is active");
            
            // Check if table exists
            try (Statement stmt = conn.createStatement()) {
                String checkTableSql = "SELECT COUNT(*) as count FROM reservations";
                System.out.println("Executing: " + checkTableSql);
                try (ResultSet rs = stmt.executeQuery(checkTableSql)) {
                    if (rs.next()) {
                        int count = rs.getInt("count");
                        System.out.println("✓ Reservations table exists");
                        System.out.println("  Total reservations in table: " + count);
                        
                        if (count == 0) {
                            System.out.println("  ⚠ WARNING: Table is empty! No reservations to load.");
                            System.out.println("  → This is why you're seeing 0 reservations.");
                            System.out.println("  → Create a reservation through the UI to add data.");
                        } else {
                            // Show sample data
                            System.out.println("\n  Sample reservation data:");
                            String sampleSql = "SELECT id, user_id, item_id, reservation_date, due_date, status FROM reservations LIMIT 5";
                            try (ResultSet sampleRs = stmt.executeQuery(sampleSql)) {
                                int sampleCount = 0;
                                while (sampleRs.next()) {
                                    sampleCount++;
                                    System.out.println("    ID: " + sampleRs.getInt("id") + 
                                                     ", User ID: " + sampleRs.getInt("user_id") + 
                                                     ", Item ID: " + sampleRs.getInt("item_id") +
                                                     ", Status: " + sampleRs.getString("status"));
                                }
                                if (sampleCount == 0) {
                                    System.out.println("    (No sample data to show)");
                                }
                            }
                            
                            // Check for orphaned reservations (users/items that don't exist)
                            System.out.println("\n  Checking for orphaned reservations...");
                            String orphanCheck = "SELECT r.id, r.user_id, r.item_id " +
                                                "FROM reservations r " +
                                                "LEFT JOIN users u ON r.user_id = u.id " +
                                                "LEFT JOIN library_items li ON r.item_id = li.id " +
                                                "WHERE u.id IS NULL OR li.id IS NULL";
                            try (ResultSet orphanRs = stmt.executeQuery(orphanCheck)) {
                                int orphanCount = 0;
                                while (orphanRs.next()) {
                                    orphanCount++;
                                    int resId = orphanRs.getInt("id");
                                    int userId = orphanRs.getInt("user_id");
                                    int itemId = orphanRs.getInt("item_id");
                                    System.err.println("  ⚠ Orphaned reservation ID=" + resId + 
                                                     " (user_id=" + userId + " or item_id=" + itemId + " doesn't exist)");
                                }
                                if (orphanCount > 0) {
                                    System.err.println("  ⚠ Found " + orphanCount + " orphaned reservation(s) that will be skipped!");
                                    System.err.println("  → These reservations reference users or items that don't exist.");
                                } else {
                                    System.out.println("  ✓ All reservations have valid user_id and item_id references");
                                }
                            }
                        }
                    } else {
                        System.err.println("ERROR: Could not get count from reservations table");
                    }
                }
            }
            
        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            System.err.println("ERROR in diagnostic: " + errorMsg);
            if (errorMsg != null && (errorMsg.contains("doesn't exist") || errorMsg.contains("Unknown table") || errorMsg.contains("Table") && errorMsg.contains("doesn't exist"))) {
                System.err.println("→ Reservations table does not exist!");
                System.err.println("→ Please run the database_setup.sql script to create the table.");
            } else {
                System.err.println("→ SQL State: " + e.getSQLState());
                System.err.println("→ Error Code: " + e.getErrorCode());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("ERROR in diagnostic: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("========== End Diagnostic ==========\n");
            System.out.flush();
            System.err.flush();
        }
    }
}

