package ducknetwork.gui;

import ducknetwork.domain.Duck;
import ducknetwork.domain.Page; // Importul noului DTO
import ducknetwork.service.NetworkService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class DuckController {

    private final NetworkService service = new NetworkService();

    // Starea de Paginare
    private int currentPage = 1;
    private final int pageSize = 10; // Câte elemente pe pagină
    private String currentFilterType = "TOATE"; // Tipul curent de filtrare

    // FXML Components (existente)
    @FXML private TableView<Duck> tableView;
    @FXML private TableColumn<Duck, Long> tableColumnId;
    @FXML private TableColumn<Duck, String> tableColumnUsername;
    @FXML private TableColumn<Duck, String> tableColumnEmail;
    @FXML private TableColumn<Duck, String> tableColumnType;
    @FXML private TableColumn<Duck, Double> tableColumnSpeed;
    @FXML private TableColumn<Duck, Double> tableColumnEndurance;

    @FXML private ComboBox<String> comboBoxType;

    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageInfoLabel;


    @FXML
    public void initialize() {
        // Configurarea Coloanelor
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        tableColumnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        tableColumnType.setCellValueFactory(new PropertyValueFactory<>("type"));
        tableColumnSpeed.setCellValueFactory(new PropertyValueFactory<>("speed"));
        tableColumnEndurance.setCellValueFactory(new PropertyValueFactory<>("endurance"));

        // Configurarea ComboBox-ului
        comboBoxType.getItems().addAll("TOATE", "SWIMMING", "FLYING", "FLYING_AND_SWIMMING");
        comboBoxType.getSelectionModel().select(currentFilterType);

        // Listener pentru filtrarea PE BAZA DE DATE (NU mai este FilteredList)
        comboBoxType.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(currentFilterType)) {
                currentFilterType = newValue;
                currentPage = 1; // Resetăm la prima pagină la schimbarea filtrului
                loadDucksPage();
            }
        });

        // Încărcarea inițială a datelor
        loadDucksPage();
    }

    /**
     * Încarcă pagina curentă de rațe de la Service, aplicând filtrul curent.
     */
    private void loadDucksPage() {
        try {
            Page<Duck> duckPage = service.getDucksPage(currentFilterType, currentPage, pageSize);

            tableView.setItems(FXCollections.observableArrayList(duckPage.getContent()));

            updatePaginationControls(duckPage);

        } catch (Exception e) {
            System.err.println("Eroare la încărcarea paginii: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Actualizează starea butoanelor și a label-ului de informații.
     */
    private void updatePaginationControls(Page<Duck> page) {
        pageInfoLabel.setText(String.format("Pagina %d din %d (Total: %d)",
                page.getCurrentPage(),
                page.getTotalPages(),
                page.getTotalElements()));

        prevButton.setDisable(page.getCurrentPage() <= 1);
        nextButton.setDisable(page.getCurrentPage() >= page.getTotalPages());
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadDucksPage();
        }
    }

    @FXML
    private void handleNextPage() {
        currentPage++;
        loadDucksPage();
    }
}