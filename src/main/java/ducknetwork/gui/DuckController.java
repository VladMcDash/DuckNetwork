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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DuckController {

    private final NetworkService service = new NetworkService();

    // --- ELEMENTE TAB 1: PAGINARE ---
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

    // Filtrare
    @FXML private ComboBox<String> comboBoxDuckFilterType;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageInfoLabel;

    // --- ELEMENTE TAB 2: ADMINISTRARE ---
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPassword;
    @FXML private ComboBox<String> comboUserType;

    // Person specific fields
    @FXML private VBox personFieldsContainer;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtBirthDate;
    @FXML private TextField txtOccupation;
    @FXML private TextField txtEmpathy;

    // Duck specific fields
    @FXML private VBox duckFieldsContainer;
    @FXML private TextField txtSpeed;
    @FXML private TextField txtEndurance;
    @FXML private ComboBox<String> comboDuckCreationType; // Tipul ratei la creare

    // Delete & Friends
    @FXML private TextField txtDeleteEmail; // Ștergere după Email
    @FXML private ComboBox<String> comboUser1Email; // Selecție prieteni după Email
    @FXML private ComboBox<String> comboUser2Email; // Selecție prieteni după Email
    @FXML private Label lblStatus;

    // Tabel Prietenii (Task 2)
    @FXML private TableView<FriendshipDTO> friendshipTable;
    @FXML private TableColumn<FriendshipDTO, String> colUser1Email;
    @FXML private TableColumn<FriendshipDTO, String> colUser2Email;


    // --- ELEMENTE TAB 3: STATISTICI ---
    @FXML private Label lblCommunityCount;
    @FXML private TextArea txtMostSociable;


    @FXML
    public void initialize() {
        initPaginationTab();
        initAdministrationTab();
    }

    // ================== TAB 1 LOGIC ==================
    private void initPaginationTab() {
        // Set up TableView columns (Duck)
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        tableColumnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        tableColumnType.setCellValueFactory(new PropertyValueFactory<>("type"));
        tableColumnSpeed.setCellValueFactory(new PropertyValueFactory<>("speed"));
        tableColumnEndurance.setCellValueFactory(new PropertyValueFactory<>("endurance"));

        // Task 1: Populate ComboBox from ENUM (Filtrare)
        List<String> duckTypes = Arrays.stream(DuckType.values())
                .map(Enum::toString)
                .collect(Collectors.toList());

        // CORECTIE EROARE addAll: adaugam "TOATE" separat
        comboBoxDuckFilterType.getItems().add("TOATE");
        comboBoxDuckFilterType.getItems().addAll(duckTypes);

        comboBoxDuckFilterType.getSelectionModel().select(currentFilterType);

        comboBoxDuckFilterType.valueProperty().addListener((observable, oldValue, newValue) -> {
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

            updatePaginationControls(duckPage);

        } catch (Exception e) {
            System.err.println("Eroare la încărcarea paginii: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePaginationControls(Page<Duck> page) {
        pageInfoLabel.setText(String.format("Pagina %d din %d (Total: %d)",
                page.getCurrentPage(),
                page.getTotalPages(),
                page.getTotalElements()));
        prevButton.setDisable(page.getCurrentPage() <= 1);
        nextButton.setDisable(page.getCurrentPage() >= page.getTotalPages());
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

    // ================== TAB 2 LOGIC (ADMINISTRARE) ==================
    private void initAdministrationTab() {
        // Setup Combo Types
        comboUserType.getItems().addAll("PERSON", "DUCK");

        // Task 1: Populate ComboBox from ENUM (Creare)
        List<String> duckTypes = Arrays.stream(DuckType.values())
                .map(Enum::toString)
                .collect(Collectors.toList());
        comboDuckCreationType.getItems().addAll(duckTypes);

        // Setarea vizibilitatii containerelor in functie de tipul de utilizator
        comboUserType.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isDuck = "DUCK".equals(newVal);
            boolean isPerson = "PERSON".equals(newVal);

            duckFieldsContainer.setVisible(isDuck);
            duckFieldsContainer.setManaged(isDuck);

            personFieldsContainer.setVisible(isPerson);
            personFieldsContainer.setManaged(isPerson);
        });

        // Task 2: Setup Friendship Table columns
        colUser1Email.setCellValueFactory(new PropertyValueFactory<>("user1Email"));
        colUser2Email.setCellValueFactory(new PropertyValueFactory<>("user2Email"));

        refreshUserLists();
    }

    private void refreshUserLists() {
        try {
            List<User> users = service.listAllUsers();

            // Task 3: Populare ComboBox-uri cu Email-uri
            List<String> emails = users.stream().map(User::getEmail).collect(Collectors.toList());
            comboUser1Email.setItems(FXCollections.observableArrayList(emails));
            comboUser2Email.setItems(FXCollections.observableArrayList(emails));

            // Task 2: Actualizare Tabel Prietenii
            List<FriendshipDTO> friendships = service.listAllFriendships();
            friendshipTable.setItems(FXCollections.observableArrayList(friendships));

            txtDeleteEmail.clear();
            lblStatus.setText("Asteptare...");

        } catch (Exception e) {
            System.err.println("Nu s-au putut incarca datele pentru tabul Administrare: " + e.getMessage());
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
                lblStatus.setText("Eroare: Username si Tip sunt obligatorii!");
                return;
            }

            User newUser;
            Long tempId = null; // ID-ul este setat de baza de date

            if ("DUCK".equals(type)) {
                String duckType = comboDuckCreationType.getValue();
                double speed = Double.parseDouble(txtSpeed.getText());
                double endurance = Double.parseDouble(txtEndurance.getText());

                if (duckType == null) throw new IllegalArgumentException("Tipul ratei este obligatoriu.");

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
                    lblStatus.setText("Eroare: Data Nașterii trebuie să fie în format YYYY-MM-DD.");
                    return;
                }

                int empathy;
                try {
                    empathy = Integer.parseInt(txtEmpathy.getText());
                } catch (NumberFormatException e) {
                    lblStatus.setText("Eroare: Empatia trebuie să fie un număr întreg.");
                    return;
                }

                // Apelul constructorului corect (9 parametri)
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
            lblStatus.setText("Eroare de format numeric (viteză, rezistență, empatie).");
        } catch (Exception e) {
            lblStatus.setText("Eroare la adaugare: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteUser() {
        // Task 4: Ștergere după Email
        try {
            String email = txtDeleteEmail.getText();
            if (email.isEmpty()) {
                lblStatus.setText("Introdu Email-ul utilizatorului de sters.");
                return;
            }

            service.removeUserByEmail(email);

            lblStatus.setText("Succes: Utilizator " + email + " sters.");
            refreshUserLists();
            loadDucksPage();
        } catch (Exception e) {
            lblStatus.setText("Eroare la stergere: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddFriend() {
        // Task 3: Adăugare Prietenie după Email
        try {
            String email1 = comboUser1Email.getValue();
            String email2 = comboUser2Email.getValue();

            if (email1 == null || email2 == null) {
                lblStatus.setText("Selecteaza ambii utilizatori (Email)!");
                return;
            }

            service.addFriendByEmails(email1, email2);
            lblStatus.setText("Succes: Prietenie creata intre " + email1 + " si " + email2);
            refreshUserLists();
        } catch (Exception e) {
            lblStatus.setText("Eroare prietenie: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveFriend() {
        // Task 3: Ștergere Prietenie după Email
        try {
            String email1 = comboUser1Email.getValue();
            String email2 = comboUser2Email.getValue();

            if (email1 == null || email2 == null) {
                lblStatus.setText("Selecteaza ambii utilizatori (Email)!");
                return;
            }

            service.removeFriendByEmails(email1, email2);
            lblStatus.setText("Succes: Prietenie stearsa intre " + email1 + " si " + email2);
            refreshUserLists();
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

    // ================== TAB 3 LOGIC (STATISTICI) ==================
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