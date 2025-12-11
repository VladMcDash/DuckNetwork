package ducknetwork.gui;

import ducknetwork.domain.*;
import ducknetwork.service.NetworkService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DuckController {

    private final NetworkService service = new NetworkService();
    private User loggedUser; // NOU: Userul logat

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

    @FXML private ComboBox<String> comboBoxDuckFilterType;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageInfoLabel;

    // --- ELEMENTE TAB 2: ADMINISTRARE ---
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
    @FXML private ComboBox<String> comboDuckCreationType;

    @FXML private TextField txtDeleteEmail;
    @FXML private ComboBox<String> comboUser1Email;
    @FXML private ComboBox<String> comboUser2Email;
    @FXML private Label lblStatus;

    @FXML private TableView<FriendshipDTO> friendshipTable;
    @FXML private TableColumn<FriendshipDTO, String> colUser1Email;
    @FXML private TableColumn<FriendshipDTO, String> colUser2Email;

    @FXML private Button btnOpenChat;

    @FXML private Label lblCommunityCount;
    @FXML private TextArea txtMostSociable;


    @FXML
    public void initialize() {
        initPaginationTab();
        initAdministrationTab();
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;
    }

    private void initPaginationTab() {
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        tableColumnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        tableColumnType.setCellValueFactory(new PropertyValueFactory<>("type"));
        tableColumnSpeed.setCellValueFactory(new PropertyValueFactory<>("speed"));
        tableColumnEndurance.setCellValueFactory(new PropertyValueFactory<>("endurance"));

        List<String> duckTypes = Arrays.stream(DuckType.values())
                .map(Enum::toString)
                .collect(Collectors.toList());

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

    @FXML private void handlePrevPage() { if (currentPage > 1) { currentPage--; loadDucksPage(); } }
    @FXML private void handleNextPage() { currentPage++; loadDucksPage(); }

    private void initAdministrationTab() {
        comboUserType.getItems().addAll("PERSON", "DUCK");
        List<String> duckTypes = Arrays.stream(DuckType.values()).map(Enum::toString).collect(Collectors.toList());
        comboDuckCreationType.getItems().addAll(duckTypes);

        comboUserType.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isDuck = "DUCK".equals(newVal);
            boolean isPerson = "PERSON".equals(newVal);
            duckFieldsContainer.setVisible(isDuck); duckFieldsContainer.setManaged(isDuck);
            personFieldsContainer.setVisible(isPerson); personFieldsContainer.setManaged(isPerson);
        });

        colUser1Email.setCellValueFactory(new PropertyValueFactory<>("user1Email"));
        colUser2Email.setCellValueFactory(new PropertyValueFactory<>("user2Email"));

        friendshipTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            btnOpenChat.setDisable(newVal == null);
        });

        refreshUserLists();
    }

    private void refreshUserLists() {
        try {
            List<User> users = service.listAllUsers();
            List<String> emails = users.stream().map(User::getEmail).collect(Collectors.toList());
            comboUser1Email.setItems(FXCollections.observableArrayList(emails));
            comboUser2Email.setItems(FXCollections.observableArrayList(emails));

            List<FriendshipDTO> friendships = service.listAllFriendships();
            friendshipTable.setItems(FXCollections.observableArrayList(friendships));

            txtDeleteEmail.clear();
            lblStatus.setText("Asteptare...");
        } catch (Exception e) {
            System.err.println("Eroare incarcare date: " + e.getMessage());
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
            Long tempId = null;

            if ("DUCK".equals(type)) {
                String duckType = comboDuckCreationType.getValue();
                double speed = Double.parseDouble(txtSpeed.getText());
                double endurance = Double.parseDouble(txtEndurance.getText());
                if (duckType == null) throw new IllegalArgumentException("Tipul ratei obligatoriu");

                if ("SWIMMING".equals(duckType)) newUser = new SwimmingDuck(tempId, username, email, password, speed, endurance);
                else if ("FLYING".equals(duckType)) newUser = new FlyingDuck(tempId, username, email, password, speed, endurance);
                else newUser = new FlyingAndSwimmingDuck(tempId, username, email, password, speed, endurance);
            } else if ("PERSON".equals(type)) {
                String firstName = txtFirstName.getText();
                String lastName = txtLastName.getText();
                String occupation = txtOccupation.getText();
                LocalDate birthDate = LocalDate.parse(txtBirthDate.getText(), DateTimeFormatter.ISO_DATE);
                int empathy = Integer.parseInt(txtEmpathy.getText());
                newUser = new Person(tempId, username, email, password, firstName, lastName, birthDate, occupation, empathy);
            } else {
                return;
            }
            service.addUser(newUser);
            lblStatus.setText("Succes adaugare: " + username);
            refreshUserLists(); loadDucksPage(); clearAddForm();
        } catch (Exception e) {
            lblStatus.setText("Eroare: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteUser() {
        try {
            String email = txtDeleteEmail.getText();
            if (email.isEmpty()) return;
            service.removeUserByEmail(email);
            lblStatus.setText("Succes stergere: " + email);
            refreshUserLists(); loadDucksPage();
        } catch (Exception e) {
            lblStatus.setText("Eroare: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddFriend() {
        try {
            String email1 = comboUser1Email.getValue();
            String email2 = comboUser2Email.getValue();
            if (email1 == null || email2 == null) return;
            service.addFriendByEmails(email1, email2);
            lblStatus.setText("Prietenie creata!");
            refreshUserLists();
        } catch (Exception e) {
            lblStatus.setText("Eroare: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveFriend() {
        try {
            String email1 = comboUser1Email.getValue();
            String email2 = comboUser2Email.getValue();
            if (email1 == null || email2 == null) return;
            service.removeFriendByEmails(email1, email2);
            lblStatus.setText("Prietenie stearsa!");
            refreshUserLists();
        } catch (Exception e) {
            lblStatus.setText("Eroare: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenChat() {
        FriendshipDTO selected = friendshipTable.getSelectionModel().getSelectedItem();
        if (selected != null && loggedUser != null) {
            try {
                String partnerEmail = selected.getUser1Email().equals(loggedUser.getEmail())
                        ? selected.getUser2Email()
                        : selected.getUser1Email();

                User partner = service.findUserByEmail(partnerEmail);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ChatView.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load()));

                ChatController chatCtrl = loader.getController();
                chatCtrl.setChatData(loggedUser, partner);

                stage.setTitle("Chat cu " + partner.getUsername());
                stage.show();

            } catch (Exception e) {
                lblStatus.setText("Eroare deschidere chat: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            lblStatus.setText("Selecteaza o prietenie si asigura-te ca esti logat!");
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
                    sb.append(u.getUsername()).append(" (ID: ").append(u.getId()).append(")\n");
                }
                txtMostSociable.setText(sb.toString());
            }
        } catch (Exception e) {
            txtMostSociable.setText("Eroare: " + e.getMessage());
        }
    }
}