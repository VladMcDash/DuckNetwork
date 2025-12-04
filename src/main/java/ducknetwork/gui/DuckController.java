package ducknetwork.gui;

import ducknetwork.domain.*;
import ducknetwork.service.NetworkService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class DuckController {

    private final NetworkService service = new NetworkService();

    private int currentPage = 1;
    private final int pageSize = 10;
    private String currentFilterType = "TOATE";

    @FXML private TableView<Duck> tableView;
    @FXML private TableColumn<Duck, Long> tableColumnId;
    @FXML private TableColumn<Duck, String> tableColumnUsername;
    @FXML private TableColumn<Duck, String> tableColumnEmail;
    @FXML private TableColumn<Duck, String> tableColumnType;
    @FXML private TableColumn<Duck, Double> tableColumnSpeed;
    @FXML private TableColumn<Duck, Double> tableColumnEndurance;

    @FXML private ComboBox<String> comboBoxFilterType;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageInfoLabel;

    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPassword;
    @FXML private ComboBox<String> comboUserType;

    @FXML private VBox personFieldsContainer;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtBirthDate;
    @FXML private TextField txtOccupation;
    @FXML private TextField txtEmpathy;

    @FXML private VBox duckFieldsContainer;
    @FXML private TextField txtSpeed;
    @FXML private TextField txtEndurance;
    @FXML private ComboBox<String> comboDuckType;

    @FXML private TextField txtDeleteId;
    @FXML private ComboBox<Long> comboUser1;
    @FXML private ComboBox<Long> comboUser2;
    @FXML private Label lblStatus;

    @FXML private Label lblCommunityCount;
    @FXML private TextArea txtMostSociable;


    @FXML
    public void initialize() {
        initPaginationTab();
        initAdministrationTab();
    }

    private void initPaginationTab() {
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        tableColumnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        tableColumnType.setCellValueFactory(new PropertyValueFactory<>("type"));
        tableColumnSpeed.setCellValueFactory(new PropertyValueFactory<>("speed"));
        tableColumnEndurance.setCellValueFactory(new PropertyValueFactory<>("endurance"));

        comboBoxFilterType.getItems().addAll("TOATE", "SWIMMING", "FLYING", "FLYING_AND_SWIMMING");
        comboBoxFilterType.getSelectionModel().select(currentFilterType);

        comboBoxFilterType.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(currentFilterType)) {
                currentFilterType = newValue;
                currentPage = 1;
                loadDucksPage();
            }
        });

        loadDucksPage();
    }

    private void loadDucksPage() {
        try {
            Page<Duck> duckPage = service.getDucksPage(currentFilterType, currentPage, pageSize);
            tableView.setItems(FXCollections.observableArrayList(duckPage.getContent()));

            pageInfoLabel.setText(String.format("Pagina %d din %d (Total: %d)",
                    duckPage.getCurrentPage(),
                    duckPage.getTotalPages(),
                    duckPage.getTotalElements()));
            prevButton.setDisable(duckPage.getCurrentPage() <= 1);
            nextButton.setDisable(duckPage.getCurrentPage() >= duckPage.getTotalPages());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadDucksPage();
        }
    }

    @FXML private void handleNextPage() {
        currentPage++;
        loadDucksPage();
    }

    private void initAdministrationTab() {
        // Setup Combo Types
        comboUserType.getItems().addAll("PERSON", "DUCK");
        comboDuckType.getItems().addAll("SWIMMING", "FLYING", "FLYING_AND_SWIMMING");

        comboUserType.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isDuck = "DUCK".equals(newVal);
            boolean isPerson = "PERSON".equals(newVal);

            duckFieldsContainer.setVisible(isDuck);
            duckFieldsContainer.setManaged(isDuck);

            personFieldsContainer.setVisible(isPerson);
            personFieldsContainer.setManaged(isPerson);

        });

        refreshUserLists();
    }

    private void refreshUserLists() {
        try {
            List<User> users = service.listAllUsers();
            List<Long> ids = users.stream().map(User::getId).collect(Collectors.toList());
            comboUser1.setItems(FXCollections.observableArrayList(ids));
            comboUser2.setItems(FXCollections.observableArrayList(ids));

            txtDeleteId.clear();
        } catch (Exception e) {
            System.err.println("Nu s-au putut incarca utilizatorii pentru combobox: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddUser() {
        try {
            String type = comboUserType.getValue();
            String username = txtUsername.getText();
            String email = txtEmail.getText();
            String password = txtPassword.getText();

            if (username.isEmpty() || type == null) {
                lblStatus.setText("Eroare: Username/Tip sunt empty!");
                return;
            }

            User newUser;
            Long tempId = null;

            if ("DUCK".equals(type)) {
                String duckType = comboDuckType.getValue();
                double speed = Double.parseDouble(txtSpeed.getText());
                double endurance = Double.parseDouble(txtEndurance.getText());

                if ("SWIMMING".equals(duckType)) {
                    newUser = new SwimmingDuck(tempId, username, email, password, speed, endurance);
                } else if ("FLYING".equals(duckType)) {
                    newUser = new FlyingDuck(tempId, username, email, password, speed, endurance);
                } else {
                    newUser = new FlyingAndSwimmingDuck(tempId, username, email, password, speed, endurance);
                }
            } else if ("PERSON".equals(type)) {
                String firstName = txtFirstName.getText();
                String lastName = txtLastName.getText();
                String occupation = txtOccupation.getText();

                LocalDate birthDate;
                try {
                    birthDate = LocalDate.parse(txtBirthDate.getText(), DateTimeFormatter.ISO_DATE);
                } catch (Exception e) {
                    lblStatus.setText("Eroare: Data Nasterii trebuie sa fie in format YYYY-MM-DD.");
                    return;
                }

                int empathy;
                try {
                    empathy = Integer.parseInt(txtEmpathy.getText());
                } catch (NumberFormatException e) {
                    lblStatus.setText("Eroare: Empatia trebuie sa fie un int.");
                    return;
                }

                newUser = new Person(tempId, username, email, password,
                        firstName, lastName, birthDate,
                        occupation, empathy);
            } else {
                lblStatus.setText("Eroare: Tipul de utilizator nu este valid.");
                return;
            }

            service.addUser(newUser);
            lblStatus.setText("Succes: Utilizator " + username + " adaugat!");

            refreshUserLists();
            loadDucksPage();
            clearAddForm();

        } catch (NumberFormatException e) {
            lblStatus.setText("Eroare de format numeric.");
            e.printStackTrace();
        } catch (Exception e) {
            lblStatus.setText("Eroare la adaugare: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteUser() {
        try {
            String idStr = txtDeleteId.getText();
            if (idStr.isEmpty()) {
                lblStatus.setText("Introdu ID-ul utilizatorului de sters.");
                return;
            }
            Long id = Long.parseLong(idStr);
            service.removeUser(id);

            lblStatus.setText("Succes: Utilizator " + id + " sters.");
            refreshUserLists();
            loadDucksPage();
        } catch (Exception e) {
            lblStatus.setText("Eroare la stergere: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddFriend() {
        try {
            Long id1 = comboUser1.getValue();
            Long id2 = comboUser2.getValue();

            if (id1 == null || id2 == null) {
                lblStatus.setText("Selecteaza ambii utilizatori!");
                return;
            }

            service.addFriend(id1, id2);
            lblStatus.setText("Succes: Prietenie creata intre " + id1 + " si " + id2);
        } catch (Exception e) {
            lblStatus.setText("Eroare prietenie: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveFriend() {
        try {
            Long id1 = comboUser1.getValue();
            Long id2 = comboUser2.getValue();

            if (id1 == null || id2 == null) {
                lblStatus.setText("Selecteaza ambii utilizatori!");
                return;
            }

            service.removeFriend(id1, id2);
            lblStatus.setText("Succes: Prietenie stearsa intre " + id1 + " si " + id2);
        } catch (Exception e) {
            lblStatus.setText("Eroare stergere prietenie: " + e.getMessage());
        }
    }

    private void clearAddForm() {
        txtUsername.clear(); txtEmail.clear(); txtPassword.clear();
        txtFirstName.clear(); txtLastName.clear(); txtBirthDate.clear();
        txtOccupation.clear(); txtEmpathy.clear();
        txtSpeed.clear(); txtEndurance.clear();
    }

    @FXML
    private void handleCalculateStats() {
        try {
            int communities = service.numberOfCommunities();
            lblCommunityCount.setText(String.valueOf(communities));

            List<User> socialCommunity = service.mostSociableCommunity();
            if (socialCommunity.isEmpty()) {
                txtMostSociable.setText("Nu exista comunitati.");
            } else {
                StringBuilder sb = new StringBuilder();
                for (User u : socialCommunity) {
                    sb.append(u.getUsername())
                            .append(" (ID: ").append(u.getId()).append(")\n");
                }
                txtMostSociable.setText(sb.toString());
            }

        } catch (Exception e) {
            txtMostSociable.setText("Eroare la calcul statistici: " + e.getMessage());
        }
    }
}