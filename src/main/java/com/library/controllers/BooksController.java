package com.library.controllers;

import com.library.models.Book;
import com.library.utils.AlertUtils;
import com.library.utils.DateUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles wiring between the books.fxml view and the in-memory catalogue data.
 */
public class BooksController {

    @FXML
    private TableView<Book> booksTable; // backing list of books shown to the user
    @FXML
    private TableColumn<Book, Number> idColumn;
    @FXML
    private TableColumn<Book, String> titleColumn; // table columns map to Book properties
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, String> isbnColumn;
    @FXML
    private TableColumn<Book, String> genreColumn;
    @FXML
    private TableColumn<Book, String> publisherColumn;
    @FXML
    private TableColumn<Book, Number> pagesColumn;
    @FXML
    private TableColumn<Book, String> publishedColumn;
    @FXML
    private TableColumn<Book, String> availabilityColumn;

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
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1000); // mock id source

    @FXML
    private void initialize() {
        // Configure UI bindings and load demo data
        configureTable();
        setupFiltering();
        seedSampleData();
        booksTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        populateForm(newSelection);
                    }
                }
        );
    }

    /**
     * Binds table columns to the appropriate Book fields.
     */
    private void configureTable() {
        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
        titleColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getTitle()));
        authorColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getAuthor()));
        isbnColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getIsbn()));
        genreColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getGenre()));
        publisherColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getPublisher()));
        pagesColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getNumberOfPages()));
        publishedColumn.setCellValueFactory(cell -> {
            LocalDate date = cell.getValue().getPublicationDate();
            return new ReadOnlyObjectWrapper<>(DateUtils.format(date));
        });
        availabilityColumn.setCellValueFactory(cell -> {
            String availability = cell.getValue().isAvailable() ? "Available" : "Checked out";
            return new ReadOnlyObjectWrapper<>(availability);
        });
    }

    /**
     * Creates the filtered/sorted views that drive the TableView.
     */
    private void setupFiltering() {
        filteredBooks = new FilteredList<>(masterBooks, book -> true);
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
        SortedList<Book> sortedBooks = new SortedList<>(filteredBooks);
        sortedBooks.comparatorProperty().bind(booksTable.comparatorProperty());
        booksTable.setItems(sortedBooks);
    }

    /**
     * Adds three well-known programming titles so the UI is populated.
     */
    private void seedSampleData() {
        if (!masterBooks.isEmpty()) {
            return;
        }
        masterBooks.addAll(
                new Book(ID_GENERATOR.getAndIncrement(), "Effective Java", "Joshua Bloch",
                        "9780134685991", LocalDate.of(2018, 1, 6), 416, "Programming", "Addison-Wesley"),
                new Book(ID_GENERATOR.getAndIncrement(), "Clean Code", "Robert C. Martin",
                        "9780132350884", LocalDate.of(2008, 8, 1), 464, "Programming", "Prentice Hall"),
                new Book(ID_GENERATOR.getAndIncrement(), "The Pragmatic Programmer", "Andrew Hunt",
                        "9780135957059", LocalDate.of(2019, 9, 13), 352, "Programming", "Addison-Wesley")
        );
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

        int id = ID_GENERATOR.getAndIncrement();
        Book book = new Book(
                id,
                titleField.getText().trim(),
                authorField.getText().trim(),
                isbnField.getText().trim(),
                publicationDatePicker.getValue(),
                pages,
                genreField.getText().trim(),
                publisherField.getText().trim()
        );

        masterBooks.add(book);
        String note = notesArea.getText();
        if (note != null && !note.isBlank()) {
            bookNotes.put(id, note.trim());
        }
        booksTable.getSelectionModel().select(book);
        clearFormFields();
        AlertUtils.showInfo("Book saved", "Book \"%s\" has been added.".formatted(book.getTitle()));
    }

    @FXML
    private void onClearForm() {
        // Deselect table row and wipe the editor fields
        booksTable.getSelectionModel().clearSelection();
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
}

