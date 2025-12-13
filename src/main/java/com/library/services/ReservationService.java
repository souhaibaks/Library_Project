package com.library.services;

import com.library.models.LibraryItem;
import com.library.models.Reservation;
import com.library.models.ReservationDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationService {

    private static ReservationService instance;
    private final IntervalScheduler scheduler;
    private final ObservableList<Reservation> reservations;
    private final ReservationDAO reservationDAO;

    private ReservationService() {
        this.scheduler = new IntervalScheduler();
        this.reservations = FXCollections.observableArrayList();
        this.reservationDAO = new ReservationDAO();
        loadReservationsFromDatabase();
    }

    public static synchronized ReservationService getInstance() {
        if (instance == null) {
            instance = new ReservationService();
        }
        return instance;
    }

    /**
     * Loads all reservations from the database
     */
    private void loadReservationsFromDatabase() {
        List<Reservation> reservationList = reservationDAO.getAllReservations();
        reservations.clear();
        reservations.addAll(reservationList);
    }

    public ObservableList<Reservation> getReservations() {
        return reservations;
    }

    /**
     * Adds a reservation and saves it to the database
     */
    public void addReservation(Reservation reservation) {
        int id = reservationDAO.insertReservation(reservation);
        if (id > 0) {
            reservations.add(reservation);
        }
    }

    /**
     * Updates a reservation in the database
     */
    public boolean updateReservation(Reservation reservation) {
        if (reservationDAO.updateReservation(reservation)) {
            // Refresh the list
            loadReservationsFromDatabase();
            return true;
        }
        return false;
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
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.ACTIVE)
                .collect(Collectors.toList());

        // Check for conflicts
        return !scheduler.hasConflict(itemReservations, start, end);
    }

    /**
     * Refreshes the reservations list from the database
     */
    public void refresh() {
        loadReservationsFromDatabase();
    }
}
