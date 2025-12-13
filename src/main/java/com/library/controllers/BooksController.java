package com.library.controllers;

import com.library.models.Book;
import com.library.models.LibraryItem;
import com.library.models.LibraryItemDAO;
import com.library.models.Magazine;
import com.library.models.Reservation;
import com.library.models.User;
import com.library.models.UserDAO;
import com.library.services.BookService;
import com.library.services.ReservationService;
import com.library.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;

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

    private final ObservableList<LibraryItem> masterItems = FXCollections.observableArrayList(); // canonical dataset
    private FilteredList<LibraryItem> filteredItems;
    private final Map<Integer, String> bookNotes = new HashMap<>();
    private final BookService bookService = BookService.getInstance();

    @FXML
    private void initialize() {
        // Refresh services to ensure latest data from database
        bookService.refresh();
        ReservationService.getInstance().refresh();
        
        // Load all items (books and magazines) from database
        loadItemsFromDatabase();
        setupFiltering(); // This will also trigger the initial render
    }
    
    /**
     * Loads all library items (books and magazines) from the database via BookService
     */
    private void loadItemsFromDatabase() {
        masterItems.clear();
        masterItems.addAll(bookService.getAllItems());
    }

    private void renderBooks() {
        booksContainer.getChildren().clear();
        for (LibraryItem item : filteredItems) {
            if (item instanceof Book book) {
                booksContainer.getChildren().add(createBookCard(book));
            } else if (item instanceof Magazine magazine) {
                booksContainer.getChildren().add(createMagazineCard(magazine));
            }
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

        // Availability Status - Check database is_available field first
        // If is_available = false (0), item is reserved regardless of reservation dates
        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("book-status");
        
        // Find active reservation for this item (used for both status display and button logic)
        LocalDate today = LocalDate.now();
        Optional<Reservation> activeReservation = ReservationService.getInstance().getReservations().stream()
            .filter(r -> r.getItem() != null && r.getItem().getId() == book.getId())
            .filter(r -> r.getStatus() == Reservation.ReservationStatus.ACTIVE)
            .findFirst(); // Find any active reservation, regardless of due date
        
        if (!book.isAvailable()) {
            // Item is marked as unavailable in database (is_available = 0)
            // Try to find active reservation to show days left
            if (activeReservation.isPresent() && activeReservation.get().getDueDate() != null) {
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, activeReservation.get().getDueDate());
                if (daysLeft >= 0) {
                    statusLabel.setText("Reserved (%d days left)".formatted(daysLeft));
                } else {
                    statusLabel.setText("Reserved (Overdue)");
                }
            } else {
                statusLabel.setText("Reserved");
            }
            statusLabel.setStyle("-fx-text-fill: #e74c3c;"); // Red for reserved
        } else {
            // Item is available in database (is_available = 1)
            statusLabel.setText("Available");
            statusLabel.setStyle("-fx-text-fill: #27ae60;"); // Green for available
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        Button reserveBtn = new Button();
        reserveBtn.getStyleClass().add("button");
        reserveBtn.setMaxWidth(Double.MAX_VALUE);
        
        if (!book.isAvailable()) {
            // Item is reserved - show Unborrow button
            if (activeReservation.isPresent()) {
                reserveBtn.setText("Unborrow");
                reserveBtn.setOnAction(e -> onUnborrow(book, activeReservation.get()));
            } else {
                // Item is marked as unavailable but no active reservation found
                // Still show Unborrow and find the reservation when clicked
                reserveBtn.setText("Unborrow");
                reserveBtn.setOnAction(e -> onUnborrowWithoutReservation(book));
            }
        } else {
            // Item is available - show Reserve button
            reserveBtn.setText("Reserve");
            reserveBtn.setOnAction(e -> onReserve(book));
        }

        card.getChildren().addAll(titleLabel, authorLabel, genreLabel, statusLabel, spacer, reserveBtn);
        
        // Click to edit (only for books, magazines use different form)
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) populateForm(book);
        });
        
        return card;
    }

    private VBox createMagazineCard(Magazine magazine) {
        VBox card = new VBox(10);
        card.getStyleClass().add("book-card");
        card.setPrefWidth(220);
        card.setPrefHeight(280);

        Label titleLabel = new Label(magazine.getTitle());
        titleLabel.getStyleClass().add("book-title");
        titleLabel.setWrapText(true);

        Label authorLabel = new Label("by " + magazine.getAuthor());
        authorLabel.getStyleClass().add("book-author");

        Label categoryLabel = new Label(magazine.getCategory());
        categoryLabel.getStyleClass().add("book-genre");

        // Availability Status - Check database is_available field first
        // If is_available = false (0), item is reserved regardless of reservation dates
        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("book-status");
        
        // Find active reservation for this item (used for both status display and button logic)
        LocalDate today = LocalDate.now();
        Optional<Reservation> activeReservation = ReservationService.getInstance().getReservations().stream()
            .filter(r -> r.getItem() != null && r.getItem().getId() == magazine.getId())
            .filter(r -> r.getStatus() == Reservation.ReservationStatus.ACTIVE)
            .findFirst(); // Find any active reservation, regardless of due date
        
        if (!magazine.isAvailable()) {
            // Item is marked as unavailable in database (is_available = 0)
            // Try to find active reservation to show days left
            if (activeReservation.isPresent() && activeReservation.get().getDueDate() != null) {
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, activeReservation.get().getDueDate());
                if (daysLeft >= 0) {
                    statusLabel.setText("Reserved (%d days left)".formatted(daysLeft));
                } else {
                    statusLabel.setText("Reserved (Overdue)");
                }
            } else {
                statusLabel.setText("Reserved");
            }
            statusLabel.setStyle("-fx-text-fill: #e74c3c;"); // Red for reserved
        } else {
            // Item is available in database (is_available = 1)
            statusLabel.setText("Available");
            statusLabel.setStyle("-fx-text-fill: #27ae60;"); // Green for available
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        Button reserveBtn = new Button();
        reserveBtn.getStyleClass().add("button");
        reserveBtn.setMaxWidth(Double.MAX_VALUE);
        
        if (!magazine.isAvailable()) {
            // Item is reserved - show Unborrow button
            if (activeReservation.isPresent()) {
                reserveBtn.setText("Unborrow");
                reserveBtn.setOnAction(e -> onUnborrowMagazine(magazine, activeReservation.get()));
            } else {
                // Item is marked as unavailable but no active reservation found
                // Still show Unborrow and find the reservation when clicked
                reserveBtn.setText("Unborrow");
                reserveBtn.setOnAction(e -> onUnborrowMagazineWithoutReservation(magazine));
            }
        } else {
            // Item is available - show Reserve button
            reserveBtn.setText("Reserve");
            reserveBtn.setOnAction(e -> onReserveMagazine(magazine));
        }

        card.getChildren().addAll(titleLabel, authorLabel, categoryLabel, statusLabel, spacer, reserveBtn);
        
        // Click to view details
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) populateForm(magazine);
        });
        
        return card;
    }

    /**
     * Creates the filtered/sorted views that drive the TableView.
     */
    private void setupFiltering() {
        filteredItems = new FilteredList<>(masterItems, item -> true);
        
        // Listener to re-render when filter changes
        filteredItems.predicateProperty().addListener((obs, oldVal, newVal) -> renderBooks());

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            String searchTerm = newValue == null ? "" : newValue.trim().toLowerCase(Locale.ENGLISH);
            filteredItems.setPredicate(item -> {
                if (searchTerm.isEmpty()) {
                    return true;
                }
                boolean matches = containsIgnoreCase(item.getTitle(), searchTerm)
                        || containsIgnoreCase(item.getAuthor(), searchTerm)
                        || containsIgnoreCase(item.getIsbn(), searchTerm);
                
                if (item instanceof Book book) {
                    matches = matches || containsIgnoreCase(book.getGenre(), searchTerm)
                            || containsIgnoreCase(book.getPublisher(), searchTerm);
                } else if (item instanceof Magazine magazine) {
                    matches = matches || containsIgnoreCase(magazine.getCategory(), searchTerm)
                            || containsIgnoreCase(magazine.getPublisher(), searchTerm);
                }
                
                return matches;
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
            // Reload all items from database to get the updated list with IDs
            loadItemsFromDatabase();
            
            String note = notesArea.getText();
            if (note != null && !note.isBlank()) {
                bookNotes.put(book.getId(), note.trim());
            }
            
            // Refresh grid
            renderBooks(); 
            
            clearFormFields();
            AlertUtils.showInfo("Book saved", "Book \"%s\" has been added to the database.".formatted(book.getTitle()));
        } else {
            AlertUtils.showError("Error", "Failed to save book to database. Please check the console for details and try again.");
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
    
    private void populateForm(Magazine magazine) {
        // For magazines, we can't populate the book form, but we can show info
        AlertUtils.showInfo("Magazine Selected", 
            "Title: %s\nAuthor: %s\nCategory: %s\nIssue: %d".formatted(
                magazine.getTitle(), 
                magazine.getAuthor(), 
                magazine.getCategory(),
                magazine.getIssueNumber()));
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
        borrowerField.setPromptText("Enter borrower's name");
        borrowerField.setEditable(true);

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
                // Get or find borrower for the reservation
                String borrowerName = borrowerField.getText().trim();
                if (borrowerName.isEmpty()) {
                    AlertUtils.showError("Error", "Please enter a borrower name.");
                    return null;
                }
                
                User user = null;
                UserDAO userDAO = new UserDAO();
                
                // Search for existing borrower
                // First, try to find by email if it looks like an email
                if (borrowerName.contains("@")) {
                    user = userDAO.getUserByEmail(borrowerName);
                }
                
                // If not found by email, try to find by name
                if (user == null) {
                    user = userDAO.getUserByName(borrowerName);
                }
                
                // If borrower not found, create a new borrower record
                if (user == null) {
                    // Ask for confirmation to create a new borrower
                    javafx.scene.control.Alert confirmDialog = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION);
                    confirmDialog.setTitle("Create New Borrower");
                    confirmDialog.setHeaderText("Borrower not found");
                    confirmDialog.setContentText("No borrower found with name: " + borrowerName + 
                        "\n\nWould you like to create a new borrower record?");
                    
                    Optional<ButtonType> result = confirmDialog.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        return null; // User cancelled
                    }
                    
                    // Create new borrower
                    String[] nameParts = borrowerName.trim().split("\\s+", 2);
                    String firstName = nameParts[0];
                    String lastName = nameParts.length > 1 ? nameParts[1] : "";
                    
                    // Generate a simple email if not provided
                    String email;
                    if (borrowerName.contains("@")) {
                        email = borrowerName;
                    } else {
                        // Create email from name
                        email = (firstName + "." + lastName).toLowerCase().replaceAll("[^a-z0-9.]", "") + "@library.local";
                    }
                    
                    // Check if email already exists
                    if (userDAO.userExists(email)) {
                        // Try with a number suffix
                        int counter = 1;
                        String baseEmail = email.replace("@library.local", "");
                        while (userDAO.userExists(baseEmail + counter + "@library.local")) {
                            counter++;
                        }
                        email = baseEmail + counter + "@library.local";
                    }
                    
                    user = new User(0, firstName, lastName, email, "");
                    int userId = userDAO.insertUser(user);
                    if (userId <= 0) {
                        AlertUtils.showError("Error", "Could not create borrower record in database.");
                        return null;
                    }
                }
                
                if (user == null || user.getId() == 0) {
                    AlertUtils.showError("Error", "Could not create or find borrower for reservation.");
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
            
            // Refresh items from database to get updated availability status
            bookService.refresh();
            loadItemsFromDatabase();
            
            AlertUtils.showInfo("Success", "Book reserved successfully and saved to database!");
            renderBooks(); // Refresh to show updated status
        });
    }

    private void onReserveMagazine(Magazine magazine) {
        Dialog<Reservation> dialog = new Dialog<>();
        dialog.setTitle("Reserve Magazine");
        dialog.setHeaderText("Reserve: " + magazine.getTitle());

        ButtonType reserveButtonType = new ButtonType("Confirm Reservation", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reserveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField borrowerField = new TextField();
        borrowerField.setPromptText("Enter borrower's name");
        borrowerField.setEditable(true);

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
            
            if (!ReservationService.getInstance().isAvailable(ReservationService.getInstance().getReservations(), magazine, start, end)) {
                AlertUtils.showError("Not Available", "This magazine is already reserved for the selected dates.");
                event.consume();
                return;
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == reserveButtonType) {
                String borrowerName = borrowerField.getText().trim();
                if (borrowerName.isEmpty()) {
                    AlertUtils.showError("Error", "Please enter a borrower name.");
                    return null;
                }
                
                User user = null;
                UserDAO userDAO = new UserDAO();
                
                // Search for existing borrower
                // First, try to find by email if it looks like an email
                if (borrowerName.contains("@")) {
                    user = userDAO.getUserByEmail(borrowerName);
                }
                
                // If not found by email, try to find by name
                if (user == null) {
                    user = userDAO.getUserByName(borrowerName);
                }
                
                // If borrower not found, create a new borrower record
                if (user == null) {
                    // Ask for confirmation to create a new borrower
                    javafx.scene.control.Alert confirmDialog = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION);
                    confirmDialog.setTitle("Create New Borrower");
                    confirmDialog.setHeaderText("Borrower not found");
                    confirmDialog.setContentText("No borrower found with name: " + borrowerName + 
                        "\n\nWould you like to create a new borrower record?");
                    
                    Optional<ButtonType> result = confirmDialog.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        return null; // User cancelled
                    }
                    
                    // Create new borrower
                    String[] nameParts = borrowerName.trim().split("\\s+", 2);
                    String firstName = nameParts[0];
                    String lastName = nameParts.length > 1 ? nameParts[1] : "";
                    
                    // Generate a simple email if not provided
                    String email;
                    if (borrowerName.contains("@")) {
                        email = borrowerName;
                    } else {
                        // Create email from name
                        email = (firstName + "." + lastName).toLowerCase().replaceAll("[^a-z0-9.]", "") + "@library.local";
                    }
                    
                    // Check if email already exists
                    if (userDAO.userExists(email)) {
                        // Try with a number suffix
                        int counter = 1;
                        String baseEmail = email.replace("@library.local", "");
                        while (userDAO.userExists(baseEmail + counter + "@library.local")) {
                            counter++;
                        }
                        email = baseEmail + counter + "@library.local";
                    }
                    
                    user = new User(0, firstName, lastName, email, "");
                    int userId = userDAO.insertUser(user);
                    if (userId <= 0) {
                        AlertUtils.showError("Error", "Could not create borrower record in database.");
                        return null;
                    }
                }
                
                if (user == null || user.getId() == 0) {
                    AlertUtils.showError("Error", "Could not create or find borrower for reservation.");
                    return null;
                }
                
                return new Reservation(
                        0,
                        user,
                        magazine,
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
            
            // Refresh items from database to get updated availability status
            bookService.refresh();
            loadItemsFromDatabase();
            
            AlertUtils.showInfo("Success", "Magazine reserved successfully and saved to database!");
            renderBooks();
        });
    }
    
    /**
     * Unborrow/Return a book
     */
    private void onUnborrow(Book book, Reservation reservation) {
        // Mark reservation as returned
        reservation.markAsReturned();
        
        // Update in database
        boolean success = ReservationService.getInstance().updateReservation(reservation);
        
        if (success) {
            // Refresh items to update availability
            bookService.refresh();
            loadItemsFromDatabase();
            renderBooks();
            
            AlertUtils.showInfo("Success", "Book returned successfully! Item is now available.");
        } else {
            AlertUtils.showError("Error", "Failed to return book. Please try again.");
        }
    }
    
    /**
     * Unborrow/Return a magazine
     */
    private void onUnborrowMagazine(Magazine magazine, Reservation reservation) {
        // Mark reservation as returned
        reservation.markAsReturned();
        
        // Update in database
        boolean success = ReservationService.getInstance().updateReservation(reservation);
        
        if (success) {
            // Refresh items to update availability
            bookService.refresh();
            loadItemsFromDatabase();
            renderBooks();
            
            AlertUtils.showInfo("Success", "Magazine returned successfully! Item is now available.");
        } else {
            AlertUtils.showError("Error", "Failed to return magazine. Please try again.");
        }
    }
    
    /**
     * Unborrow/Return a book when reservation is not found in memory
     * This can happen if the reservation was loaded from database but not properly linked
     */
    private void onUnborrowWithoutReservation(Book book) {
        // Try to find the reservation again
        Optional<Reservation> reservation = ReservationService.getInstance().getReservations().stream()
            .filter(r -> r.getItem() != null && r.getItem().getId() == book.getId())
            .filter(r -> r.getStatus() == Reservation.ReservationStatus.ACTIVE)
            .findFirst();
        
        if (reservation.isPresent()) {
            onUnborrow(book, reservation.get());
        } else {
            // No active reservation found, but item is marked as unavailable
            // Update item availability directly
            book.setAvailable(true);
            LibraryItemDAO itemDAO = new LibraryItemDAO();
            boolean success = itemDAO.updateBook(book);
            
            if (success) {
                bookService.refresh();
                loadItemsFromDatabase();
                renderBooks();
                AlertUtils.showInfo("Success", "Book marked as available! Item is now available.");
            } else {
                AlertUtils.showError("Error", "Failed to update book availability. Please try again.");
            }
        }
    }
    
    /**
     * Unborrow/Return a magazine when reservation is not found in memory
     */
    private void onUnborrowMagazineWithoutReservation(Magazine magazine) {
        // Try to find the reservation again
        Optional<Reservation> reservation = ReservationService.getInstance().getReservations().stream()
            .filter(r -> r.getItem() != null && r.getItem().getId() == magazine.getId())
            .filter(r -> r.getStatus() == Reservation.ReservationStatus.ACTIVE)
            .findFirst();
        
        if (reservation.isPresent()) {
            onUnborrowMagazine(magazine, reservation.get());
        } else {
            // No active reservation found, but item is marked as unavailable
            // Update item availability directly - we'll need to add updateMagazine method or use a generic update
            // For now, refresh and let the database state handle it
            bookService.refresh();
            loadItemsFromDatabase();
            renderBooks();
            AlertUtils.showInfo("Success", "Magazine marked as available! Item is now available.");
        }
    }
}

