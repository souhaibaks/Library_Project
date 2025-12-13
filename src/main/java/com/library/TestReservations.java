package com.library;

import com.library.models.Reservation;
import com.library.models.ReservationDAO;

import java.util.List;

public class TestReservations {
    public static void main(String[] args) {
        System.out.println("=== Testing Reservation Fetching ===\n");
        
        ReservationDAO dao = new ReservationDAO();
        
        // Run diagnostic
        System.out.println("Step 1: Running diagnostic...");
        dao.diagnoseReservationsTable();
        
        // Try to fetch reservations
        System.out.println("Step 2: Fetching all reservations...");
        List<Reservation> reservations = dao.getAllReservations();
        
        System.out.println("\nStep 3: Results:");
        System.out.println("  Total reservations fetched: " + reservations.size());
        
        if (reservations.isEmpty()) {
            System.out.println("\n⚠ No reservations found. Possible reasons:");
            System.out.println("  1. The reservations table is empty");
            System.out.println("  2. Reservations exist but have invalid foreign keys");
            System.out.println("  3. Database connection issue");
        } else {
            System.out.println("\n✓ Successfully loaded reservations:");
            for (Reservation r : reservations) {
                System.out.println("  - ID: " + r.getId() + 
                                 ", User: " + (r.getUser() != null ? r.getUser().getFullName() : "null") +
                                 ", Item: " + (r.getItem() != null ? r.getItem().getTitle() : "null") +
                                 ", Status: " + r.getStatus());
            }
        }
        
        System.out.println("\n=== Test Complete ===");
    }
}

