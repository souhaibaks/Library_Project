# FXML Cheat Sheet for Library Project

Quick reference for understanding and editing `*.fxml` files like `com/library/views/books.fxml`.

## Core Concepts
- **FXML** is XML describing a JavaFX scene graph. Each tag maps to a JavaFX class (e.g., `<TableView>` → `javafx.scene.control.TableView`).
- **Namespaces**:
  - `xmlns="http://javafx.com/javafx/17"` tells the loader which JavaFX version.
  - `xmlns:fx="http://javafx.com/fxml"` enables `fx:*` attributes (controller wiring, IDs, etc.).
- **Controller binding**: `fx:controller="com.library.controllers.BooksController"` links the file to a Java class whose `@FXML` fields/methods correspond to `fx:id` and `onAction` attributes inside the FXML.
  - Docs: <https://openjfx.io/javadoc/17/javafx.fxml/javafx/fxml/doc-files/introduction_to_fxml.html>

## Common Attributes
- **`fx:id="controlName"`**: exposes the node to the controller via `@FXML private NodeType controlName;`.
- **Event handlers**: `onAction="#methodName"` calls `@FXML private void methodName(ActionEvent event)` in the controller.
- **Layout hints**: Attributes like `GridPane.columnIndex="1"` or `HBox.hgrow="ALWAYS"` configure layout managers using static properties.
- **`styleClass` / `stylesheets`**: hook into CSS (e.g., `<Label styleClass="header-title"/>`).

## Import Statements
Use `<?import ...?>` directives at the top to avoid verbose fully-qualified names:
```
<?import javafx.scene.control.TableView?>
<?import javafx.scene.layout.BorderPane?>
```
These match the controls and layouts you plan to instantiate.

## Root Containers
- Common layout panes:
  - `BorderPane`: top/center/bottom/etc. slots, used in `books.fxml`.
  - `VBox`, `HBox`: vertical/horizontal stacking.
  - `GridPane`: grid-based forms; use `ColumnConstraints` and `RowConstraints` for sizing.
  - Reference: <https://openjfx.io/javadoc/17/javafx.graphics/javafx/scene/layout/package-summary.html>

## Connecting to Controllers
Example from `books.fxml`:
```
<BorderPane ... fx:controller="com.library.controllers.BooksController">
    <center>
        <TableView fx:id="booksTable">
            <columns>
                <TableColumn fx:id="titleColumn" text="Title"/>
            </columns>
        </TableView>
    </center>
    <bottom>
        <Button text="Save" onAction="#onAddBook"/>
    </bottom>
</BorderPane>
```
- `fx:id` ↔ `@FXML private TableView<Book> booksTable;`
- `onAction="#onAddBook"` ↔ `@FXML private void onAddBook()` method.

## Loading FXML
- Use `FXMLLoader` in Java:
```
FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/library/views/books.fxml"));
Parent root = loader.load();
```
- Scene can then be created from `root` and shown.
- Documentation: <https://openjfx.io/javadoc/17/javafx.fxml/javafx/fxml/FXMLLoader.html>

## Resources & Styling
- To apply CSS, set `stylesheets` in the FXML or via Java (`scene.getStylesheets().add("/com/library/css/style.css");`).
- Use `<fx:include>` to modularize views (not yet used in this project but handy for reusing headers/footers).
- Reference on styling: <https://openjfx.io/javadoc/17/javafx.graphics/javafx/scene/doc-files/cssref.html>

## Tips for Editing
- Keep IDs unique within a file.
- Remember to import every control/layout you instantiate; Scene Builder can manage imports automatically.
- Validate structure by loading the FXML through `FXMLLoader` (runtime errors point to missing controllers, typos, etc.).

This sheet should help when extending `books.fxml` or creating new views (login, reservation, history) using the same patterns.
