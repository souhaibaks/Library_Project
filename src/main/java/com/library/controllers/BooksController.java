package com.library.controllers;

import com.library.models.Book;
import com.library.models.Reservation;
import com.library.models.User;
import com.library.models.UserDAO;
import com.library.services.BookService;
import com.library.services.ReservationService;
import com.library.services.UserService;
import com.library.utils.AlertUtils;
import com.library.utils.DateUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Handles wiring between the books.fxml view and the in-memory catalogue data.
 */
public class BooksController {

    @FXML
    private FlowPane booksContainer; // Replaces TableView

    @FXML
    private TextField searchField; // live filter input

    @FXML
    private TextField titleField; // form inputs for create/update
    @FXML
    private TextField authorField;
    @FXML
    private TextField isbnField;
    @FXML
    private TextField genreField;
    @FXML
    private TextField publisherField;
    @FXML
    private TextField pagesField;
    @FXML
    private DatePicker publicationDatePicker;
    @FXML
    private TextArea notesArea;

    private final ObservableList<Book> masterBooks = FXCollections.observableArrayList(); // canonical dataset
    private FilteredList<Book> filteredBooks;
    private final Map<Integer, String> bookNotes = new HashMap<>();
    private final BookService bookService = BookService.getInstance();

    @FXML
    private void initialize() {
        // Load books from database
        loadBooksFromDatabase();
        setupFiltering(); // This will also trigger the initial render
    }
    
    /**
     * Loads books from the database via BookService
     */
    private void loadBooksFromDatabase() {
        masterBooks.clear();
        masterBooks.addAll(bookService.getBooks());
    }

    private void renderBooks() {
        booksContainer.getChildren().clear();
        for (Book book : filteredBooks) {
            booksContainer.getChildren().add(createBookCard(book));
        }
    }

    private VBox createBookCard(Book book) {
        VBox card = new VBox(10);
        card.getStyleClass().add("book-card");
        card.setPrefWidth(220);
        card.setPrefHeight(280);

        Label titleLabel = new Label(book.getTitle());
        titleLabel.getStyleClass().add("book-title");
        titleLabel.setWrapText(true);

        Label authorLabel = new Label("by " + book.getAuthor());
        authorLabel.getStyleClass().add("book-author");

        Label genreLabel = new Label(book.getGenre());
        genreLabel.getStyleClass().add("book-genre");

        // Availability Status
        LocalDate today = LocalDate.now();
        Optional<Reservation> activeRes = ReservationService.getInstance().getReservations().stream()
            .filter(r -> r.getItem().getId() == book.getId())
            .filter(r -> !today.isBefore(r.getReservationDate()) && !today.isAfter(r.getDueDate()))
            .findFirst();

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("book-status");
        if (activeRes.isPresent()) {
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, activeRes.get().getDueDate());
            statusLabel.setText("Reserved (%d days left)".formatted(daysLeft));
            statusLabel.setStyle("-fx-text-fill: #e74c3c;"); // Red for reserved
        } else {
            statusLabel.setText("Available");
            statusLabel.setStyle("-fx-text-fill: #27ae60;"); // Green for available
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button reserveBtn = new Button("Reserve");
        reserveBtn.getStyleClass().add("button");
        reserveBtn.setMaxWidth(Double.MAX_VALUE);
        reserveBtn.setOnAction(e -> onReserve(book));

        card.getChildren().addAll(titleLabel, authorLabel, genreLabel, statusLabel, spacer, reserveBtn);
        
        // Click to edit
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) populateForm(book);
        });
        
        return card;
    }

    /**
     * Creates the filtered/sorted views that drive the TableView.
     */
    private void setupFiltering() {
        filteredBooks = new FilteredList<>(masterBooks, book -> true);
        
        // Listener to re-render when filter changes
        filteredBooks.predicateProperty().addListener((obs, oldVal, newVal) -> renderBooks());

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            String searchTerm = newValue == null ? "" : newValue.trim().toLowerCase(Locale.ENGLISH);
            filteredBooks.setPredicate(book -> {
                if (searchTerm.isEmpty()) {
                    return true;
                }
                return containsIgnoreCase(book.getTitle(), searchTerm)
                        || containsIgnoreCase(book.getAuthor(), searchTerm)
                        || containsIgnoreCase(book.getIsbn(), searchTerm)
                        || containsIgnoreCase(book.getGenre(), searchTerm)
                        || containsIgnoreCase(book.getPublisher(), searchTerm);
            });
        });
        
        // Initial render
        renderBooks();
    }


    @FXML
    private void onAddBook() {
        // Guard against missing inputs before persisting to the table
        if (!validateForm()) {
            AlertUtils.showWarning("Incomplete data",
                    "Please provide title, author, ISBN, genre, publisher, pages and publication date.");
            return;
        }

        int pages;
        try {
            pages = Integer.parseInt(pagesField.getText().trim());
            if (pages <= 0) {
                throw new NumberFormatException("Pages must be positive");
            }
        } catch (NumberFormatException ex) {
            AlertUtils.showError("Invalid number of pages", "Enter a positive number.");
            return;
        }

        Book book = new Book(
                0, // ID will be set by database
                titleField.getText().trim(),
                authorField.getText().trim(),
                isbnField.getText().trim(),
                publicationDatePicker.getValue(),
                pages,
                genreField.getText().trim(),
                publisherField.getText().trim()
        );

        // Save to database via BookService
        boolean success = bookService.addBook(book);
        
        if (success) {
            // Reload books from database to get the updated list with IDs
            loadBooksFromDatabase();
            
            String note = notesArea.getText();
            if (note != null && !note.isBlank()) {
                bookNotes.put(book.getId(), note.trim());
            }
            
            // Refresh grid
            renderBooks(); 
            
            clearFormFields();
            AlertUtils.showInfo("Book saved", "Book \"%s\" has been added to the database.".formatted(book.getTitle()));
        } else {
            AlertUtils.showError("Error", "Failed to save book to database. Please try again.");
        }
    }

    @FXML
    private void onClearForm() {
        // Wipe the editor fields
        clearFormFields();
    }

    @FXML
    private void onClearSearch() {
        // Clearing the search field restores the full dataset
        searchField.clear();
    }

    private void populateForm(Book book) {
        // Mirrors the selected row into the editor form
        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
        isbnField.setText(book.getIsbn());
        genreField.setText(book.getGenre());
        publisherField.setText(book.getPublisher());
        pagesField.setText(String.valueOf(book.getNumberOfPages()));
        publicationDatePicker.setValue(book.getPublicationDate());
        notesArea.setText(bookNotes.getOrDefault(book.getId(), ""));
    }

    private boolean validateForm() {
        // Simple presence validation; business rules belong in a service layer
        return isFilled(titleField)
                && isFilled(authorField)
                && isFilled(isbnField)
                && isFilled(genreField)
                && isFilled(publisherField)
                && isFilled(pagesField)
                && publicationDatePicker.getValue() != null;
    }

    private boolean isFilled(TextField field) {
        return field.getText() != null && !field.getText().trim().isEmpty();
    }

    private void clearFormFields() {
        // Shared helper to reset the editor controls
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        genreField.clear();
        publisherField.clear();
        pagesField.clear();
        publicationDatePicker.setValue(null);
        notesArea.clear();
    }

    private boolean containsIgnoreCase(String value, String searchTerm) {
        // Utility used by the FilteredList predicate
        return value != null && value.toLowerCase(Locale.ENGLISH).contains(searchTerm);
    }

    private void onReserve(Book book) {
        Dialog<Reservation> dialog = new Dialog<>();
        dialog.setTitle("Reserve Book");
        dialog.setHeaderText("Reserve: " + book.getTitle());

        ButtonType reserveButtonType = new ButtonType("Confirm Reservation", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reserveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField borrowerField = new TextField();
        User currentUser = UserService.getInstance().getCurrentUser();
        if (currentUser != null) {
            borrowerField.setText(currentUser.getFullName());
            // REMOVE: borrowerField.setEditable(false);
        } else {
            borrowerField.setPromptText("Borrower Name");
        }
        borrowerField.setEditable(true); // Always allow editing

        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        DatePicker endDatePicker = new DatePicker(LocalDate.now().plusDays(7));

        grid.add(new Label("Borrower:"), 0, 0);
        grid.add(borrowerField, 1, 0);
        grid.add(new Label("Start Date:"), 0, 1);
        grid.add(startDatePicker, 1, 1);
        grid.add(new Label("End Date:"), 0, 2);
        grid.add(endDatePicker, 1, 2);

        dialog.getDialogPane().setContent(grid);

        final Button btnReserve = (Button) dialog.getDialogPane().lookupButton(reserveButtonType);
        btnReserve.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();

            if (start == null || end == null) {
                AlertUtils.showWarning("Invalid Dates", "Please select start and end dates.");
                event.consume();
                return;
            }
            if (end.isBefore(start)) {
                AlertUtils.showWarning("Invalid Dates", "End date cannot be before start date.");
                event.consume();
                return;
            }
            
            if (!ReservationService.getInstance().isAvailable(ReservationService.getInstance().getReservations(), book, start, end)) {
                AlertUtils.showError("Not Available", "This book is already reserved for the selected dates.");
                event.consume();
                return;
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == reserveButtonType) {
                // Get or create user for the reservation
                String borrowerName = borrowerField.getText().trim();
                User user = null;
                
                // First, try to use current logged-in user
                if (currentUser != null && currentUser.getFullName().equals(borrowerName)) {
                    user = currentUser;
                } else {
                    // Try to find user by name or create new one
                    UserDAO userDAO = new UserDAO();
                    // Try to find by email if it looks like an email
                    if (borrowerName.contains("@")) {
                        user = userDAO.getUserByEmail(borrowerName);
                    }
                    
                    // If not found, create a new user
                    if (user == null) {
                        String[] nameParts = borrowerName.split(" ", 2);
                        String firstName = nameParts.length > 0 ? nameParts[0] : borrowerName;
                        String lastName = nameParts.length > 1 ? nameParts[1] : "";
                        String email = borrowerName.toLowerCase().replace(" ", ".") + "@library.local";
                        
                        user = new User(0, firstName, lastName, email, "");
                        int userId = userDAO.insertUser(user);
                        if (userId <= 0) {
                            AlertUtils.showError("Error", "Could not create user in database.");
                            return null;
                        }
                        // User is now in database with valid ID
                    }
                }
                
                if (user == null || user.getId() == 0) {
                    AlertUtils.showError("Error", "Could not create or find user for reservation.");
                    return null;
                }
                
                return new Reservation(
                        0, // ID will be set by database
                        user,
                        book,
                        startDatePicker.getValue(),
                        endDatePicker.getValue(),
                        null,
                        Reservation.ReservationStatus.ACTIVE
                );
            }
            return null;
        });

        Optional<Reservation> result = dialog.showAndWait();

        result.ifPresent(reservation -> {
            ReservationService.getInstance().addReservation(reservation);
            AlertUtils.showInfo("Success", "Book reserved successfully and saved to database!");
            renderBooks(); // Refresh to show updated status
        });
    }
}

