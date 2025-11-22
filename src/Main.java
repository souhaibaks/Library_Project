import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Create UI components
        Label titleLabel = new Label("JavaFX Test Application");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label statusLabel = new Label("JavaFX is working correctly!");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: green;");
        
        Button testButton = new Button("Click Me!");
        testButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px;");
        
        // Button click handler
        testButton.setOnAction(e -> {
            statusLabel.setText("Button clicked! JavaFX is fully functional.");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: blue;");
        });
        
        // Create layout
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getChildren().addAll(titleLabel, statusLabel, testButton);
        
        // Create scene
        Scene scene = new Scene(root, 400, 300);
        
        // Set up stage
        primaryStage.setTitle("JavaFX Test - Library Project");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
