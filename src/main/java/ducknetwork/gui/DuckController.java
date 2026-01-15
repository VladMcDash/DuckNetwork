package ducknetwork.gui;

import ducknetwork.domain.*;
import ducknetwork.service.NetworkService;
import ducknetwork.util.Observer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DuckController implements Observer {
    private final NetworkService service = NetworkService.getInstance();
    private User loggedUser;
    private long lastPendingCount = 0;

    private int currentPage = 1;
    private final int pageSize = 10;
    private String currentFilterType = "TOATE";

    @FXML private TableView<Duck> tableView;
    @FXML private TableColumn<Duck, Long> tableColumnId;
    @FXML private TableColumn<Duck, String> tableColumnUsername, tableColumnEmail, tableColumnType;
    @FXML private TableColumn<Duck, Double> tableColumnSpeed, tableColumnEndurance;
    @FXML private ComboBox<String> comboBoxDuckFilterType;
    @FXML private Button prevButton, nextButton;
    @FXML private Label pageInfoLabel;

    @FXML private TextField txtUsername, txtEmail, txtFirstName, txtLastName, txtBirthDate, txtOccupation, txtEmpathy, txtSpeed, txtEndurance, txtDeleteEmail, txtSearchChatEmail;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> comboUserType, comboDuckCreationType;
    @FXML private VBox personFieldsContainer, duckFieldsContainer;
    @FXML private Label lblStatus, lblChatStatus, lblFriendStatus, lblNotificari;
    @FXML private Circle dotNewNotifications;
    @FXML private TableView<FriendshipDTO> friendshipTable;
    @FXML private TableColumn<FriendshipDTO, String> colUser1Email, colUser2Email;
    @FXML private ComboBox<String> comboUser1Email, comboUser2Email;
    @FXML private ListView<String> listNotifications, listRecentChats;
    @FXML private Button btnQuickFriendRequest;

    private List<User> recentPartners;
    private User lastSearchedUser;

    @FXML
    public void initialize() {
        initPaginationTab();
        initAdministrationTab();
        resetQuickRequestUI();
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;
        this.lastPendingCount = service.getPendingRequestsCount(user.getId());
        service.addObserver(this);
        update();
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            long currentPendingCount = service.getPendingRequestsCount(loggedUser.getId());

            // Verificăm dacă a apărut o cerere NOUĂ de prietenie pentru pop-up
            if (currentPendingCount > lastPendingCount) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Notificare");
                alert.setHeaderText(null);
                alert.setContentText("Ai primit o cerere de prietenie nouă!");
                alert.show();
            }
            lastPendingCount = currentPendingCount;

            // Refresh general interfață
            refreshNotifications();
            refreshRecentChats();
            refreshUserLists();
            loadDucksPage();

            if (lblNotificari != null) {
                lblNotificari.setText(currentPendingCount > 0 ? "Notificari (" + currentPendingCount + ")" : "Notificari");
            }
            if (dotNewNotifications != null) {
                dotNewNotifications.setVisible(currentPendingCount > 0);
            }
        });
    }

    @FXML
    public void refreshNotifications() {
        if (loggedUser == null) return;
        try {
            List<FriendRequestDTO> reqs = service.getAllUserRequests(loggedUser.getId());
            listNotifications.setItems(FXCollections.observableArrayList(
                    reqs.stream().map(r -> r.getFromEmail() + " | Status: " + r.getStatus() + " | Data: " + r.getDate().format(DateTimeFormatter.ofPattern("dd-MM HH:mm"))).toList()
            ));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void refreshRecentChats() {
        if (loggedUser == null) return;
        recentPartners = service.getRecentChatPartners(loggedUser.getId());
        listRecentChats.setItems(FXCollections.observableArrayList(
                recentPartners.stream().map(User::getEmail).toList()
        ));
    }

    @FXML
    private void handleAcceptRequest() {
        String sel = listNotifications.getSelectionModel().getSelectedItem();
        if (sel != null && sel.contains("RECEIVED FROM:")) {
            try {
                String fromEmail = sel.substring(sel.indexOf("FROM: ") + 6, sel.indexOf(" |")).trim();
                service.acceptFriendRequest(fromEmail, loggedUser.getEmail());
            } catch (Exception e) { lblChatStatus.setText("❌ " + e.getMessage()); }
        }
    }

    @FXML
    private void handleRejectRequest() {
        String sel = listNotifications.getSelectionModel().getSelectedItem();
        if (sel != null && sel.contains("RECEIVED FROM:")) {
            try {
                String fromEmail = sel.substring(sel.indexOf("FROM: ") + 6, sel.indexOf(" |")).trim();
                service.rejectFriendRequest(fromEmail, loggedUser.getEmail());
            } catch (Exception e) { lblChatStatus.setText("❌ " + e.getMessage()); }
        }
    }

    @FXML
    private void handleAddFriend() {
        try {
            service.sendFriendRequest(comboUser1Email.getValue(), comboUser2Email.getValue());
            lblStatus.setText("Cerere trimisa!");
        } catch (Exception e) { lblStatus.setText("Eroare: " + e.getMessage()); }
    }

    @FXML
    private void handleOpenChat() {
        FriendshipDTO selected = friendshipTable.getSelectionModel().getSelectedItem();
        if (selected != null && loggedUser != null) {
            try {
                String partnerEmail = selected.getUser1Email().equals(loggedUser.getEmail()) ? selected.getUser2Email() : selected.getUser1Email();
                openChatWindow(service.findUserByEmail(partnerEmail));
            } catch (Exception e) { lblStatus.setText("Eroare: " + e.getMessage()); }
        }
    }

    @FXML
    private void handleSearchAndChat() {
        String email = txtSearchChatEmail.getText().trim();
        resetQuickRequestUI();
        if (email.isEmpty()) return;
        try {
            User partner = service.findUserByEmail(email);
            if (partner != null) {
                lastSearchedUser = partner;
                openChatWindow(partner);
                boolean areFriends = service.listAllFriendships().stream().anyMatch(f ->
                        (f.getUser1Email().equals(loggedUser.getEmail()) && f.getUser2Email().equals(partner.getEmail())) ||
                                (f.getUser2Email().equals(loggedUser.getEmail()) && f.getUser1Email().equals(partner.getEmail()))
                );
                if (areFriends) { lblFriendStatus.setVisible(true); lblFriendStatus.setManaged(true); }
                else { btnQuickFriendRequest.setVisible(true); btnQuickFriendRequest.setManaged(true); btnQuickFriendRequest.setText("Invite " + partner.getUsername()); }
            }
        } catch (Exception e) { lblChatStatus.setText("❌ Email negasit."); }
    }

    @FXML
    private void handleQuickRequest() {
        if (lastSearchedUser != null && loggedUser != null) {
            try {
                service.sendFriendRequest(loggedUser.getEmail(), lastSearchedUser.getEmail());
                lblChatStatus.setText("✅ Cerere trimisa!");
                resetQuickRequestUI();
            } catch (Exception e) { lblChatStatus.setText("❌ " + e.getMessage()); }
        }
    }

    private void resetQuickRequestUI() {
        if(btnQuickFriendRequest != null) { btnQuickFriendRequest.setVisible(false); btnQuickFriendRequest.setManaged(false); }
        if(lblFriendStatus != null) { lblFriendStatus.setVisible(false); lblFriendStatus.setManaged(false); }
        lastSearchedUser = null;
    }

    @FXML private void handleOpenRecentChat() {
        int idx = listRecentChats.getSelectionModel().getSelectedIndex();
        if (idx >= 0) openChatWindow(recentPartners.get(idx));
    }

    private void openChatWindow(User partner) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ChatView.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            ChatController ctrl = loader.getController();
            ctrl.setChatData(loggedUser, partner);
            stage.setTitle("Chat cu " + partner.getUsername());
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void initPaginationTab() {
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        tableColumnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        tableColumnType.setCellValueFactory(new PropertyValueFactory<>("type"));
        tableColumnSpeed.setCellValueFactory(new PropertyValueFactory<>("speed"));
        tableColumnEndurance.setCellValueFactory(new PropertyValueFactory<>("endurance"));
        comboBoxDuckFilterType.getItems().add("TOATE");
        comboBoxDuckFilterType.getItems().addAll(Arrays.stream(DuckType.values()).map(Enum::toString).toList());
        comboBoxDuckFilterType.getSelectionModel().select(currentFilterType);
        comboBoxDuckFilterType.valueProperty().addListener((o, old, newVal) -> {
            if (newVal != null) { currentFilterType = newVal; currentPage = 1; loadDucksPage(); }
        });
    }

    private void loadDucksPage() {
        try {
            Page<Duck> p = service.getDucksPage(currentFilterType, currentPage, pageSize);
            if(p != null) {
                tableView.setItems(FXCollections.observableArrayList(p.getContent()));
                pageInfoLabel.setText("Pagina " + p.getCurrentPage() + " din " + p.getTotalPages());
                prevButton.setDisable(p.getCurrentPage() <= 1);
                nextButton.setDisable(p.getCurrentPage() >= p.getTotalPages());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handlePrevPage() { currentPage--; loadDucksPage(); }
    @FXML private void handleNextPage() { currentPage++; loadDucksPage(); }

    private void initAdministrationTab() {
        comboUserType.getItems().addAll("PERSON", "DUCK");
        comboDuckCreationType.getItems().addAll(Arrays.stream(DuckType.values()).map(Enum::toString).toList());
        comboUserType.valueProperty().addListener((o, old, newVal) -> {
            duckFieldsContainer.setVisible("DUCK".equals(newVal)); duckFieldsContainer.setManaged("DUCK".equals(newVal));
            personFieldsContainer.setVisible("PERSON".equals(newVal)); personFieldsContainer.setManaged("PERSON".equals(newVal));
        });
        colUser1Email.setCellValueFactory(new PropertyValueFactory<>("user1Email"));
        colUser2Email.setCellValueFactory(new PropertyValueFactory<>("user2Email"));
    }

    @FXML
    private void refreshUserLists() {
        try {
            List<User> users = service.listAllUsers();
            List<String> emails = users.stream().map(User::getEmail).toList();
            if(comboUser1Email != null) comboUser1Email.setItems(FXCollections.observableArrayList(emails));
            if(comboUser2Email != null) comboUser2Email.setItems(FXCollections.observableArrayList(emails));
            friendshipTable.setItems(FXCollections.observableArrayList(service.listAllFriendships()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleAddUser() {
        try {
            User u;
            if ("DUCK".equals(comboUserType.getValue())) u = new SwimmingDuck(null, txtUsername.getText(), txtEmail.getText(), txtPassword.getText(), Double.parseDouble(txtSpeed.getText()), Double.parseDouble(txtEndurance.getText()));
            else u = new Person(null, txtUsername.getText(), txtEmail.getText(), txtPassword.getText(), txtFirstName.getText(), txtLastName.getText(), LocalDate.parse(txtBirthDate.getText()), txtOccupation.getText(), Integer.parseInt(txtEmpathy.getText()));
            service.addUser(u); lblStatus.setText("Succes!"); clearAddForm();
        } catch (Exception e) { lblStatus.setText("Eroare: " + e.getMessage()); }
    }

    @FXML private void handleDeleteUser() { try { service.removeUserByEmail(txtDeleteEmail.getText()); } catch (Exception e) { lblStatus.setText(e.getMessage()); } }
    @FXML private void handleRemoveFriend() { FriendshipDTO sel = friendshipTable.getSelectionModel().getSelectedItem(); if (sel != null) service.removeFriendByEmails(sel.getUser1Email(), sel.getUser2Email()); }

    private void clearAddForm() {
        txtUsername.clear(); txtEmail.clear(); txtPassword.clear(); txtFirstName.clear(); txtLastName.clear(); txtBirthDate.clear(); txtOccupation.clear(); txtEmpathy.clear(); txtSpeed.clear(); txtEndurance.clear();
    }
}