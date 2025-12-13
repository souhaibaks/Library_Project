package com.library.services;

import com.library.models.LibraryItem;
import com.library.models.Reservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationService {

    private static ReservationService instance;
    private final IntervalScheduler scheduler;
    private final ObservableList<Reservation> reservations;

    private ReservationService() {
        this.scheduler = new IntervalScheduler();
        this.reservations = FXCollections.observableArrayList();
    }

    public static synchronized ReservationService getInstance() {
        if (instance == null) {
            instance = new ReservationService();
        }
        return instance;
    }

    public ObservableList<Reservation> getReservations() {
        return reservations;
    }

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
    }

    /**
     * Checks if a book is available for the given period.
     *
     * @param allReservations The list of all reservations in the system (or filtered by item).
     * @param item            The item to check.
     * @param start           Start date.
     * @param end             End date.
     * @return true if available, false if conflicted.
     */
    public boolean isAvailable(List<Reservation> allReservations, LibraryItem item, LocalDate start, LocalDate end) {
        // Filter reservations for this specific item
        List<Reservation> itemReservations = allReservations.stream()
                .filter(r -> r.getItem().getId() == item.getId())
                .collect(Collectors.toList());

        // Check for conflicts
        return !scheduler.hasConflict(itemReservations, start, end);
    }

    // Future: Methods to save to DB
}
