# JavaFX Cheat Sheet for Library Project

This document summarizes the core JavaFX classes used throughout the Library project and links them to the official API docs so you can look up deeper details quickly.

## TableView Ecosystem
- **`TableView<T>`**: Displays rows of `T` objects. Key methods:
  - `getItems()` / `setItems(ObservableList<T>)` to bind data.
  - `getSelectionModel()` returns a `MultipleSelectionModel<T>` that manages row selection.
  - [API reference](https://openjfx.io/javadoc/17/javafx.controls/javafx/scene/control/TableView.html)
- **`TableColumn<S, T>`**: Defines a single column. You typically set `setCellValueFactory` to map from each row (type `S`) to the cell value (`T`).
  - [API reference](https://openjfx.io/javadoc/17/javafx.controls/javafx/scene/control/TableColumn.html)
- **Selection helpers**:
  - `getSelectionModel().select(item)` selects a row programmatically.
  - `getSelectionModel().clearSelection()` clears the selection (used in `BooksController.onClearForm`).
  - [Selection model docs](https://openjfx.io/javadoc/17/javafx.controls/javafx/scene/control/MultipleSelectionModel.html)

## Observable Collections
- **`ObservableList<T>`** (`javafx.collections`): List implementation that notifies UI controls of changes. Wrap data via `FXCollections.observableArrayList()`.
  - [API reference](https://openjfx.io/javadoc/17/javafx.base/javafx/collections/ObservableList.html)
- **`FilteredList<T>`**: Creates a filtered view of another list. Assign a predicate via `setPredicate` and bind to a `TableView`.
  - [API reference](https://openjfx.io/javadoc/17/javafx.base/javafx/collections/transformation/FilteredList.html)
- **`SortedList<T>`**: Takes an existing list (often a `FilteredList`) and adds comparator-based sorting. Bind the comparator to the table so column sorting works automatically.
  - [API reference](https://openjfx.io/javadoc/17/javafx.base/javafx/collections/transformation/SortedList.html)

## UI Controls Referenced in the Project
- **`TextField` / `TextArea`**: Basic text inputs. `TextField` exposes `textProperty()` for binding; `TextArea` spans multiple lines.
- **`DatePicker`**: Lets a user choose dates; returns a `LocalDate` via `getValue()`.
- **`Button`**: Triggered via `setOnAction` or FXML `onAction="#handler"`.
- All controls share the same API reference site: <https://openjfx.io/javadoc/17/javafx.controls/javafx/scene/control/package-summary.html>

## Application Startup
- **`Application`**: Base class you extend (`App`) to boot JavaFX. Override `start(Stage primaryStage)`.
  - [API reference](https://openjfx.io/javadoc/17/javafx.graphics/javafx/application/Application.html)
- **`Stage`**: Top-level window. Use `setScene` and `show`.
- **`Scene`**: Container for the UI graph; typically created from FXML or code.
- **`FXMLLoader`**: Loads `.fxml` files and wires controllers (`fx:controller`). In this project, `App` does `new FXMLLoader(getClass().getResource("/com/library/views/books.fxml"))`.

## Alerts & Platform Utilities
- **`Alert` / `AlertType`**: Simple modal dialogs. `AlertUtils` wraps `Alert` creation.
- **`Platform.runLater`**: Ensures code executes on the JavaFX Application Thread, required when showing alerts or updating UI from background operations.

## Common Tasks Recap
- **Clearing selection**: `booksTable.getSelectionModel().clearSelection();`
- **Binding column values**: `titleColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getTitle()));`
- **Filtering data**: attach a listener to `searchField.textProperty()` and update the `FilteredList` predicate.
- **Formatting dates**: centralize via `DateUtils.format(LocalDate)` to keep UI consistent.

Keep this file handy when editing controllers—it highlights where each JavaFX type lives and links straight to the docs if you need the full API surface.
