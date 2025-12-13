package com.library.services;

import com.library.models.Reservation;

import java.time.LocalDate;
import java.util.List;

/**
 * Utility class for checking interval overlaps to prevent double booking.
 */
public class IntervalScheduler {

    /**
     * Checks if a new reservation interval conflicts with any existing reservations for the same item.
     *
     * @param existingReservations List of existing reservations for the specific item.
     * @param newStart             Start date of the new reservation.
     * @param newEnd               End date of the new reservation.
     * @return true if there is a conflict (overlap), false otherwise.
     */
    public boolean hasConflict(List<Reservation> existingReservations, LocalDate newStart, LocalDate newEnd) {
        if (newStart == null || newEnd == null || newStart.isAfter(newEnd)) {
            throw new IllegalArgumentException("Invalid date range provided.");
        }

        for (Reservation reservation : existingReservations) {
            // Skip cancelled or returned reservations if necessary (business logic decision)
            // Assuming we only care about ACTIVE or PENDING reservations that hold the book.
            if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED ||
                reservation.getStatus() == Reservation.ReservationStatus.RETURNED) {
                continue;
            }

            if (isOverlapping(reservation.getReservationDate(), reservation.getDueDate(), newStart, newEnd)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if two date ranges overlap.
     * Overlap occurs if (StartA <= EndB) and (StartB <= EndA).
     * This assumes inclusive dates (e.g., if a book is due on the 5th, it is considered unavailable on the 5th).
     */
    private boolean isOverlapping(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }
}
