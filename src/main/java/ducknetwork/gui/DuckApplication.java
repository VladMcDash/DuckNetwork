package ducknetwork.gui;

import ducknetwork.persistence.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.URL;

public class DuckApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load the single expected resource path. Adjust the path or move the FXML to `src/main/resources/gui/MainView.fxml`.
        URL fxml = getClass().getResource("/gui/MainView.fxml");
        if (fxml == null) {
            throw new IllegalStateException("FXML not found at `/gui/MainView.fxml`. Place the file at `src/main/resources/gui/MainView.fxml` or update the path.");
        }
        Parent root = FXMLLoader.load(fxml);
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Override
    public void stop() {
        Database.getInstance().closeConnection();
    }

    private void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Eroare Critică");
        alert.setContentText(text);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}