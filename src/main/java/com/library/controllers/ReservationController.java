package com.library.controllers;

import com.library.models.Book;
import com.library.models.LibraryItem;
import com.library.models.Magazine;
import com.library.models.Reservation;
import com.library.utils.AlertUtils;
import com.library.utils.DateUtils;
import com.library.services.BookService;
import com.library.services.ReservationService;
import com.library.services.UserService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.Locale;

/**
 * Demo controller that wires reservation.fxml to an in-memory list of reservations.
 */
public class ReservationController {

    @FXML
    private TableView<Reservation> reservationsTable;
    @FXML
    private TableColumn<Reservation, Number> reservationIdColumn;
    @FXML
    private TableColumn<Reservation, String> userColumn;
    @FXML
    private TableColumn<Reservation, String> itemColumn;
    @FXML
    private TableColumn<Reservation, String> reservedColumn;
    @FXML
    private TableColumn<Reservation, String> dueColumn;
    @FXML
    private TableColumn<Reservation, String> returnColumn;
    @FXML
    private TableColumn<Reservation, String> statusColumn;
    @FXML
    private TableColumn<Reservation, String> contactColumn;

    @FXML
    private TextField searchField;
    private final ReservationService reservationService = ReservationService.getInstance();
    private final BookService bookService = BookService.getInstance();
    private final UserService userService = UserService.getInstance();
    private FilteredList<Reservation> filteredReservations;

    @FXML
    private void initialize() {
        bookService.refresh();
        userService.refresh();
        
        configureTable();
        setupFiltering();
        
        reservationService.getReservations().addListener((ListChangeListener<Reservation>) change -> {
            Platform.runLater(() -> {
                reservationsTable.refresh();
            });
        });
        
        reservationService.refresh();
        
        Platform.runLater(() -> {
            reservationsTable.refresh();
        });
    }

    private void configureTable() {
        reservationIdColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
        userColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getUser() != null ? cell.getValue().getUser().getFullName() : "—"));
        itemColumn.setCellValueFactory(cell -> {
            LibraryItem item = cell.getValue().getItem();
            if (item == null) {
                return new ReadOnlyObjectWrapper<>("—");
            }
            String itemType = item instanceof Book ? "Book" : item instanceof Magazine ? "Magazine" : "Item";
            return new ReadOnlyObjectWrapper<>(String.format("%s (%s)", item.getTitle(), itemType));
        });
        reservedColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                DateUtils.format(cell.getValue().getReservationDate())));
        dueColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                DateUtils.format(cell.getValue().getDueDate())));
        returnColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                DateUtils.format(cell.getValue().getReturnDate())));
        statusColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                formatStatus(cell.getValue().getStatus(), cell.getValue().isOverdue())));
        contactColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getUser() != null ? cell.getValue().getUser().getEmail() : "—"));
    }

    private void setupFiltering() {
        // Recreate FilteredList to ensure it's bound to the current reservations list
        filteredReservations = new FilteredList<>(reservationService.getReservations(), reservation -> true);
        
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            String searchTerm = newValue == null ? "" : newValue.trim().toLowerCase(Locale.ENGLISH);
            filteredReservations.setPredicate(reservation -> {
                if (searchTerm.isEmpty()) {
                    return true;
                }
                return containsIgnoreCase(reservation.getUser() != null ? reservation.getUser().getFullName() : "", searchTerm)
                        || containsIgnoreCase(reservation.getItem() != null ? reservation.getItem().getTitle() : "", searchTerm)
                        || containsIgnoreCase(reservation.getStatus() != null ? reservation.getStatus().name() : "", searchTerm);
            });
        });

        SortedList<Reservation> sortedReservations = new SortedList<>(filteredReservations);
        sortedReservations.comparatorProperty().bind(reservationsTable.comparatorProperty());
        reservationsTable.setItems(sortedReservations);
        
        // Force table refresh to ensure it displays the data
        reservationsTable.refresh();
    }
    
    /**
     * Refreshes the table by re-binding it to the reservations list
     */
    private void refreshTable() {
        setupFiltering();
    }



    @FXML
    private void onMarkReturned() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showInfo("Select reservation", "Choose a reservation to mark as returned.");
            return;
        }
        if (selected.getStatus() == Reservation.ReservationStatus.RETURNED) {
            AlertUtils.showInfo("Already returned", "This reservation is already closed.");
            return;
        }
        selected.markAsReturned();
        
        // Update in database
        boolean success = reservationService.updateReservation(selected);
        
        if (success) {
            // Refresh items to update availability in database
            bookService.refresh();
            
            // Refresh reservations to get updated list
            // This will trigger the listener in BooksController to refresh the catalog
            reservationService.refresh();
            
            // Refresh table to show updated reservation
            refreshTable();
            
            AlertUtils.showInfo("Reservation updated",
                    "Reservation #%d is now returned and updated in database. Item is now available.\n\n" +
                    "The catalog view will automatically update to show the item as available.".formatted(selected.getId()));
        } else {
            AlertUtils.showError("Error", "Failed to update reservation in database.");
        }
    }

    @FXML
    private void onClearSearch() {
        searchField.clear();
    }


    private String formatStatus(Reservation.ReservationStatus status, boolean overdue) {
        if (status == null) {
            return "";
        }
        if (overdue) {
            return status.name() + " (Overdue)";
        }
        return status.name();
    }

    private boolean containsIgnoreCase(String value, String term) {
        return value != null && value.toLowerCase(Locale.ENGLISH).contains(term);
    }
}

