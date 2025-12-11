package ducknetwork.gui;

import ducknetwork.domain.User;
import ducknetwork.service.NetworkService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    private final NetworkService service = new NetworkService();

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    private void handleLogin() {
        try {
            String username = txtUsername.getText();
            String password = txtPassword.getText();

            User loggedUser = service.login(username, password);

            openMainWindow(loggedUser);

            ((Stage) txtUsername.getScene().getWindow()).close();

        } catch (Exception e) {
            lblError.setText(e.getMessage());
        }
    }

    private void openMainWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/MainView.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            DuckController controller = loader.getController();
            controller.setLoggedUser(user);

            stage.setTitle("Duck Network - Logged as: " + user.getUsername());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}