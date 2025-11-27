package ducknetwork.gui;

import ducknetwork.domain.Duck;
import ducknetwork.service.NetworkService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class DuckController {

    private final NetworkService service = new NetworkService();
    private ObservableList<Duck> model = FXCollections.observableArrayList();

    @FXML
    private TableView<Duck> tableView;
    @FXML
    private TableColumn<Duck, Long> tableColumnId;
    @FXML
    private TableColumn<Duck, String> tableColumnUsername;
    @FXML
    private TableColumn<Duck, String> tableColumnEmail;
    @FXML
    private TableColumn<Duck, String> tableColumnType;
    @FXML
    private TableColumn<Duck, Double> tableColumnSpeed;
    @FXML
    private TableColumn<Duck, Double> tableColumnEndurance;

    @FXML
    private ComboBox<String> comboBoxType;

    @FXML
    public void initialize() {
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        tableColumnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        tableColumnType.setCellValueFactory(new PropertyValueFactory<>("type"));
        tableColumnSpeed.setCellValueFactory(new PropertyValueFactory<>("speed"));
        tableColumnEndurance.setCellValueFactory(new PropertyValueFactory<>("endurance"));

        List<Duck> ducks = service.listAllDucks();
        model.setAll(ducks);

        FilteredList<Duck> filteredData = new FilteredList<>(model, p -> true);

        tableView.setItems(filteredData);

        comboBoxType.getItems().addAll("TOATE", "SWIMMING", "FLYING", "FLYING_AND_SWIMMING");
        comboBoxType.getSelectionModel().select("TOATE");

        comboBoxType.valueProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(duck -> {
                if (newValue == null || "TOATE".equals(newValue)) {
                    return true;
                }

                String type = duck.getType();
                return type != null && type.equals(newValue);
            });
        });
    }
}