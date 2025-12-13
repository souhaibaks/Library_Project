package com.library.controllers;

import com.library.models.Book;
import com.library.models.LibraryItem;
import com.library.models.Magazine;
import com.library.models.Reservation;
import com.library.models.User;
import com.library.utils.AlertUtils;
import com.library.utils.DateUtils;
import com.library.services.BookService;
import com.library.services.ReservationService;
import com.library.services.UserService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    @FXML
    private ComboBox<User> userPicker;
    @FXML
    private ComboBox<LibraryItem> itemPicker;
    @FXML
    private DatePicker reservationDatePicker;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private DatePicker returnDatePicker;
    @FXML
    private ComboBox<Reservation.ReservationStatus> statusPicker;
    @FXML
    private TextArea notesArea;

    private final ObservableList<User> userOptions = FXCollections.observableArrayList();
    private final ObservableList<LibraryItem> itemOptions = FXCollections.observableArrayList();
    private final Map<Integer, String> reservationNotes = new HashMap<>();
    private final ReservationService reservationService = ReservationService.getInstance();
    private final BookService bookService = BookService.getInstance();
    private final UserService userService = UserService.getInstance();
    private FilteredList<Reservation> filteredReservations;

    @FXML
    private void initialize() {
        // IMPORTANT: Load items and users first, then reservations
        // This ensures that when reservations are loaded, their associated items/users exist
        System.out.println("ReservationController: Initializing...");
        bookService.refresh();
        userService.refresh();
        
        loadDataFromDatabase();
        configureChoiceBoxes();
        configureTable();
        
        // Setup filtering and table binding BEFORE loading reservations
        setupFiltering();
        
        // Add listener to update table when reservations change
        reservationService.getReservations().addListener((ListChangeListener<Reservation>) change -> {
            System.out.println("ReservationController: Reservations list changed. Current size: " + reservationService.getReservations().size());
            Platform.runLater(() -> {
                reservationsTable.refresh();
                System.out.println("ReservationController: Table refreshed. Table items count: " + reservationsTable.getItems().size());
            });
        });
        
        // Load reservations from database AFTER setting up the table
        System.out.println("ReservationController: Loading reservations from database...");
        reservationService.refresh();
        
        reservationsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        populateForm(newSelection);
                    }
                }
        );
        
        // Debug: Print reservation count
        System.out.println("ReservationController: Initialization complete. Total reservations: " + reservationService.getReservations().size());
        System.out.println("ReservationController: Table items count: " + reservationsTable.getItems().size());
        
        // Force table refresh after a short delay to ensure data is loaded
        Platform.runLater(() -> {
            reservationsTable.refresh();
            System.out.println("ReservationController: Final table refresh. Items: " + reservationsTable.getItems().size());
        });
    }
    
    /**
     * Loads books, magazines, users, and reservations from the database
     */
    private void loadDataFromDatabase() {
        // Load all items (books and magazines) from database
        itemOptions.clear();
        itemOptions.addAll(bookService.getAllItems());
        
        // Load users from database
        userOptions.clear();
        userOptions.addAll(userService.getAllUsers());
        
        // Reservations are already loaded by ReservationService
    }

    private void configureChoiceBoxes() {
        userPicker.setItems(userOptions);
        userPicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(User user) {
                return user == null ? "" : "%s (%s)".formatted(user.getFullName(), user.getEmail());
            }

            @Override
            public User fromString(String string) {
                return null;
            }
        });

        itemPicker.setItems(itemOptions);
        itemPicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LibraryItem item) {
                if (item == null) return "";
                if (item instanceof Book book) {
                    return "%s — %s (Book)".formatted(book.getTitle(), book.getAuthor());
                } else if (item instanceof Magazine magazine) {
                    return "%s — %s (Magazine)".formatted(magazine.getTitle(), magazine.getAuthor());
                }
                return item.getTitle() + " — " + item.getAuthor();
            }

            @Override
            public LibraryItem fromString(String string) {
                return null;
            }
        });

        statusPicker.setItems(FXCollections.observableArrayList(Reservation.ReservationStatus.values()));
    }

    private void configureTable() {
        reservationIdColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
        userColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getUser() != null ? cell.getValue().getUser().getFullName() : "—"));
        itemColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getItem() != null ? cell.getValue().getItem().getTitle() : "—"));
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
    private void onAddReservation() {
        if (!validateForm()) {
            AlertUtils.showWarning("Missing information",
                    "Choose a patron, an item, and supply the due date.");
            return;
        }

        LocalDate reservationDate = reservationDatePicker.getValue() != null
                ? reservationDatePicker.getValue()
                : LocalDate.now();
        LocalDate dueDate = dueDatePicker.getValue();
        LocalDate returnDate = returnDatePicker.getValue();

        if (returnDate != null && returnDate.isBefore(reservationDate)) {
            AlertUtils.showWarning("Invalid dates", "Return date cannot be before the reservation date.");
            return;
        }
        if (dueDate.isBefore(reservationDate)) {
            AlertUtils.showWarning("Invalid due date", "Due date must be on or after the reservation date.");
            return;
        }

        // Check for conflicts using Interval Scheduling
        if (!reservationService.isAvailable(reservationService.getReservations(), itemPicker.getValue(), reservationDate, dueDate)) {
            AlertUtils.showError("Booking Conflict", 
                "The selected item is already reserved during the chosen period.\nPlease select different dates.");
            return;
        }

        Reservation.ReservationStatus status = statusPicker.getValue();
        if (status == null) {
            status = returnDate != null
                    ? Reservation.ReservationStatus.RETURNED
                    : Reservation.ReservationStatus.ACTIVE;
        }

        Reservation reservation = new Reservation(
                0, // ID will be set by database
                userPicker.getValue(),
                itemPicker.getValue(),
                reservationDate,
                dueDate,
                returnDate,
                status
        );

        reservationService.addReservation(reservation);
        
        // Refresh items from database to get updated availability status
        bookService.refresh();
        
        // Refresh reservations from database to get updated list with IDs
        reservationService.refresh();
        
        // Re-bind table to show the updated reservations
        refreshTable();
        
        String note = notesArea.getText();
        if (note != null && !note.isBlank()) {
            reservationNotes.put(reservation.getId(), note.trim());
        }
        reservationsTable.getSelectionModel().select(reservation);
        clearForm();
        AlertUtils.showInfo("Reservation created",
                "Reserved \"%s\" for %s and saved to database."
                        .formatted(reservation.getItem().getTitle(), reservation.getUser().getFullName()));
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
            // Refresh items to update availability
            bookService.refresh();
            
            reservationNotes.put(selected.getId(),
                    "Marked as returned on %s".formatted(DateUtils.format(selected.getReturnDate())));
            
            // Refresh table to show updated reservation
            refreshTable();
            populateForm(selected);
            AlertUtils.showInfo("Reservation updated",
                    "Reservation #%d is now returned and updated in database. Item is now available.".formatted(selected.getId()));
        } else {
            AlertUtils.showError("Error", "Failed to update reservation in database.");
        }
    }
    
    /**
     * Unborrow/Return a library item - same as marking returned but with clearer naming
     */
    @FXML
    private void onUnborrow() {
        onMarkReturned(); // Reuse the same logic
    }

    @FXML
    private void onClearForm() {
        reservationsTable.getSelectionModel().clearSelection();
        clearForm();
    }

    @FXML
    private void onClearSearch() {
        searchField.clear();
    }

    private void populateForm(Reservation reservation) {
        userPicker.getSelectionModel().select(reservation.getUser());
        itemPicker.getSelectionModel().select(reservation.getItem());
        reservationDatePicker.setValue(reservation.getReservationDate());
        dueDatePicker.setValue(reservation.getDueDate());
        returnDatePicker.setValue(reservation.getReturnDate());
        statusPicker.setValue(reservation.getStatus());
        notesArea.setText(reservationNotes.getOrDefault(reservation.getId(), ""));
    }

    private boolean validateForm() {
        return userPicker.getValue() != null
                && itemPicker.getValue() != null
                && dueDatePicker.getValue() != null;
    }

    private void clearForm() {
        userPicker.getSelectionModel().clearSelection();
        itemPicker.getSelectionModel().clearSelection();
        reservationDatePicker.setValue(null);
        dueDatePicker.setValue(null);
        returnDatePicker.setValue(null);
        statusPicker.getSelectionModel().clearSelection();
        notesArea.clear();
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

