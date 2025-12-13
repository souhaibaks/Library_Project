package com.library.controllers;

import com.library.utils.AlertUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Lightweight activity log controller that powers history.fxml.
 */
public class HistoryController {

    @FXML
    private TableView<HistoryEntry> historyTable;
    @FXML
    private TableColumn<HistoryEntry, String> timestampColumn;
    @FXML
    private TableColumn<HistoryEntry, String> categoryColumn;
    @FXML
    private TableColumn<HistoryEntry, String> actionColumn;
    @FXML
    private TableColumn<HistoryEntry, String> detailsColumn;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categoryFilter;
    @FXML
    private Label summaryLabel;
    @FXML
    private TextArea detailArea;

    private final ObservableList<HistoryEntry> masterHistory = FXCollections.observableArrayList();
    private FilteredList<HistoryEntry> filteredHistory;
    private static final DateTimeFormatter HISTORY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

    @FXML
    private void initialize() {
        configureTable();
        setupFiltering();
        seedEntries();
        historyTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        detailArea.setText(newSelection.details());
                    } else {
                        detailArea.clear();
                    }
                }
        );
    }

    private void configureTable() {
        timestampColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                HISTORY_FORMATTER.format(cell.getValue().timestamp())));
        categoryColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().category()));
        actionColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().action()));
        detailsColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().details()));
    }

    private void setupFiltering() {
        filteredHistory = new FilteredList<>(masterHistory, entry -> true);
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        categoryFilter.setItems(FXCollections.observableArrayList("All", "Reservation", "Account", "Inventory", "System"));
        categoryFilter.getSelectionModel().selectFirst();
        categoryFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());

        SortedList<HistoryEntry> sortedEntries = new SortedList<>(filteredHistory);
        sortedEntries.comparatorProperty().bind(historyTable.comparatorProperty());
        historyTable.setItems(sortedEntries);
    }

    private void applyFilters() {
        String searchTerm = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ENGLISH);
        String category = categoryFilter.getValue();
        filteredHistory.setPredicate(entry -> {
            boolean matchesCategory = category == null || "All".equalsIgnoreCase(category) || entry.category().equalsIgnoreCase(category);
            if (!matchesCategory) {
                return false;
            }
            if (searchTerm.isEmpty()) {
                return true;
            }
            return contains(entry.action(), searchTerm)
                    || contains(entry.details(), searchTerm)
                    || contains(entry.category(), searchTerm);
        });
        updateSummary();
    }

    private void seedEntries() {
        if (!masterHistory.isEmpty()) {
            return;
        }
        masterHistory.addAll(
                new HistoryEntry(LocalDateTime.now().minusHours(1), "Reservation", "New hold created",
                        "Reserved \"Designing Data-Intensive Applications\" for Sofia Ramirez"),
                new HistoryEntry(LocalDateTime.now().minusDays(1), "Reservation", "Marked returned",
                        "Ethan Khan returned \"Refactoring\" on time"),
                new HistoryEntry(LocalDateTime.now().minusDays(2), "Account", "User activated",
                        "New membership approved for Maya Chen"),
                new HistoryEntry(LocalDateTime.now().minusDays(3), "Inventory", "Book added",
                        "\"Head First Design Patterns\" added to catalogue"),
                new HistoryEntry(LocalDateTime.now().minusDays(4), "System", "Backup completed",
                        "Nightly snapshot finished successfully")
        );
        updateSummary();
    }

    @FXML
    private void onClearFilters() {
        searchField.clear();
        categoryFilter.getSelectionModel().selectFirst();
    }

    @FXML
    private void onExportHistory() {
        AlertUtils.showInfo("Export not implemented",
                "This demo only shows how the UI wiring would work.");
    }

    private void updateSummary() {
        long total = filteredHistory.stream().count();
        long reservations = filteredHistory.stream().filter(entry -> "Reservation".equalsIgnoreCase(entry.category())).count();
        summaryLabel.setText("%d entries (%d reservations)".formatted(total, reservations));
    }

    private boolean contains(String value, String term) {
        return value != null && value.toLowerCase(Locale.ENGLISH).contains(term);
    }

    /**
     * Simple DTO representing an activity log entry.
     */
    public record HistoryEntry(LocalDateTime timestamp, String category, String action, String details) { }
}

