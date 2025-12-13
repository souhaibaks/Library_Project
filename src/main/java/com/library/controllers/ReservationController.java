package com.library.controllers;

import com.library.models.Book;
import com.library.models.Reservation;
import com.library.models.User;
import com.library.utils.AlertUtils;
import com.library.utils.DateUtils;
import com.library.services.ReservationService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
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
import java.util.concurrent.atomic.AtomicInteger;

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
    private ComboBox<Book> itemPicker;
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
    private final ObservableList<Book> bookOptions = FXCollections.observableArrayList();
    private final Map<Integer, String> reservationNotes = new HashMap<>();
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(4000);
    private final ReservationService reservationService = ReservationService.getInstance();
    private FilteredList<Reservation> filteredReservations;

    @FXML
    private void initialize() {
        configureChoiceBoxes();
        configureTable();
        setupFiltering();
        seedSampleData();
        reservationsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        populateForm(newSelection);
                    }
                }
        );
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

        itemPicker.setItems(bookOptions);
        itemPicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(Book book) {
                return book == null ? "" : "%s — %s".formatted(book.getTitle(), book.getAuthor());
            }

            @Override
            public Book fromString(String string) {
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
    }

    private void seedSampleData() {
        if (!reservationService.getReservations().isEmpty()) {
            return;
        }

        // Sample catalog and patrons
        bookOptions.addAll(
                new Book(2001, "Designing Data-Intensive Applications", "Martin Kleppmann",
                        "9781449373320", LocalDate.of(2017, 3, 16), 616, "Programming", "O'Reilly"),
                new Book(2002, "Refactoring", "Martin Fowler",
                        "9780134757599", LocalDate.of(2018, 11, 20), 448, "Programming", "Addison-Wesley"),
                new Book(2003, "Head First Design Patterns", "Eric Freeman",
                        "9780596007126", LocalDate.of(2004, 10, 25), 694, "Programming", "O'Reilly")
        );
        userOptions.addAll(
                new User(301, "Sofia", "Ramirez", "sofia@library.local", "555-0140"),
                new User(302, "Ethan", "Khan", "ethan@library.local", "555-0150"),
                new User(303, "Maya", "Chen", "maya@library.local", "555-0160")
        );

        Reservation reservationOne = new Reservation(
                ID_GENERATOR.getAndIncrement(),
                userOptions.get(0),
                bookOptions.get(0),
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(12),
                null,
                Reservation.ReservationStatus.ACTIVE
        );
        Reservation reservationTwo = new Reservation(
                ID_GENERATOR.getAndIncrement(),
                userOptions.get(1),
                bookOptions.get(1),
                LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(5),
                LocalDate.now().minusDays(3),
                Reservation.ReservationStatus.RETURNED
        );
        Reservation reservationThree = new Reservation(
                ID_GENERATOR.getAndIncrement(),
                userOptions.get(2),
                bookOptions.get(2),
                LocalDate.now().minusDays(15),
                LocalDate.now().minusDays(1),
                null,
                Reservation.ReservationStatus.OVERDUE
        );

        reservationService.getReservations().addAll(reservationOne, reservationTwo, reservationThree);
        reservationNotes.put(reservationThree.getId(), "Send reminder email tomorrow.");
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
                ID_GENERATOR.getAndIncrement(),
                userPicker.getValue(),
                itemPicker.getValue(),
                reservationDate,
                dueDate,
                returnDate,
                status
        );

        reservationService.addReservation(reservation);
        String note = notesArea.getText();
        if (note != null && !note.isBlank()) {
            reservationNotes.put(reservation.getId(), note.trim());
        }
        reservationsTable.getSelectionModel().select(reservation);
        clearForm();
        AlertUtils.showInfo("Reservation created",
                "Reserved \"%s\" for %s."
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
        reservationNotes.put(selected.getId(),
                "Marked as returned on %s".formatted(DateUtils.format(selected.getReturnDate())));
        reservationsTable.refresh();
        populateForm(selected);
        AlertUtils.showInfo("Reservation updated",
                "Reservation #%d is now returned.".formatted(selected.getId()));
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
        if (reservation.getItem() instanceof Book book) {
            itemPicker.getSelectionModel().select(book);
        } else {
            itemPicker.getSelectionModel().clearSelection();
        }
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

