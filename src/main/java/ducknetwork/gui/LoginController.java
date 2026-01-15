package ducknetwork.gui;

import ducknetwork.domain.User;
import ducknetwork.service.NetworkService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {
    private final NetworkService service = NetworkService.getInstance();

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    private void handleLogin() {
        try {
            User user = service.login(txtUsername.getText(), txtPassword.getText());
            openMainWindow(user);
            txtUsername.clear();
            txtPassword.clear();
            lblError.setText("");
        } catch (Exception e) { lblError.setText(e.getMessage()); }
    }

    private void openMainWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/MainView.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            DuckController ctrl = loader.getController();
            ctrl.setLoggedUser(user);
            stage.setTitle("DuckNetwork - " + user.getUsername());
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}