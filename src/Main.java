import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/library/views/books.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setTitle("Library Catalogue");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to load books.fxml", ex);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
