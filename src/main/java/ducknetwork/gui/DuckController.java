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
    private User loggedUser;

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

    @FXML private TextField txtUsername, txtEmail, txtFirstName, txtLastName, txtBirthDate, txtOccupation, txtEmpathy, txtSpeed, txtEndurance, txtDeleteEmail;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> comboUserType, comboDuckCreationType;
    @FXML private VBox personFieldsContainer, duckFieldsContainer;
    @FXML private Label lblStatus;
    @FXML private TableView<FriendshipDTO> friendshipTable;
    @FXML private TableColumn<FriendshipDTO, String> colUser1Email, colUser2Email;
    @FXML private ComboBox<String> comboUser1Email, comboUser2Email;

    @FXML private ListView<String> listNotifications, listRecentChats;
    @FXML private TextField txtSearchChatEmail;
    @FXML private Button btnQuickFriendRequest;
    @FXML private Label lblFriendStatus, lblChatStatus;

    private List<User> recentPartners;
    private User lastSearchedUser;

    @FXML private Label lblCommunityCount;
    @FXML private TextArea txtMostSociable;

    @FXML
    public void initialize() {
        initPaginationTab();
        initAdministrationTab();
        resetQuickRequestUI();
    }

    @FXML private Label lblNotificari;

    public void setLoggedUser(User user) {
        this.loggedUser = user;
        refreshNotifications();
        refreshRecentChats();
        updateNotificationLabel();
    }

    @FXML
    public void refreshNotifications() {
        if (loggedUser == null) return;
        try {
            List<FriendRequestDTO> reqs = service.getAllUserRequests(loggedUser.getId());
            listNotifications.setItems(FXCollections.observableArrayList(
                    reqs.stream().map(r -> r.getFromEmail() + " | Status: " + r.getStatus() + " | Data: " + r.getDate().format(DateTimeFormatter.ofPattern("dd-MM HH:mm"))).toList()
            ));

            updateNotificationLabel();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateNotificationLabel() {
        if (loggedUser == null || lblNotificari == null) return;
        long count = service.getPendingRequestsCount(loggedUser.getId());
        lblNotificari.setText(count > 0 ? "Notificari (" + count + ")" : "Notificari");
    }

    @FXML
    private void handleAddFriend() {
        try {
            String email1 = comboUser1Email.getValue();
            String email2 = comboUser2Email.getValue();
            if (email1 == null || email2 == null) { lblStatus.setText("Selecteaza utilizatorii!"); return; }
            service.sendFriendRequest(email1, email2);
            lblStatus.setText("Cerere trimisa!");
            refreshNotifications();
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
    private void handleAcceptRequest() {
        String sel = listNotifications.getSelectionModel().getSelectedItem();
        if (sel != null && sel.contains("RECEIVED FROM:")) {
            try {
                String fromEmail = sel.substring(sel.indexOf("FROM: ") + 6, sel.indexOf(" |")).trim();
                service.acceptFriendRequest(fromEmail, loggedUser.getEmail());
                lblChatStatus.setText("✅ Cerere acceptata!");
                refreshNotifications(); refreshUserLists();
            } catch (Exception e) { lblChatStatus.setText("❌ " + e.getMessage()); }
        } else if (sel != null) { lblChatStatus.setText("Nu poti accepta o cerere trimisa de tine."); }
    }

    @FXML
    private void handleRejectRequest() {
        String sel = listNotifications.getSelectionModel().getSelectedItem();
        if (sel != null && sel.contains("RECEIVED FROM:")) {
            try {
                String fromEmail = sel.substring(sel.indexOf("FROM: ") + 6, sel.indexOf(" |")).trim();
                service.rejectFriendRequest(fromEmail, loggedUser.getEmail());
                lblChatStatus.setText("Cerere respinsa.");
                refreshNotifications();
            } catch (Exception e) { lblChatStatus.setText("❌ " + e.getMessage()); }
        }
    }


    @FXML
    private void handleSearchAndChat() {
        String email = txtSearchChatEmail.getText().trim();
        resetQuickRequestUI(); lblChatStatus.setText("");
        if (email.isEmpty()) { lblChatStatus.setText("Introdu email."); return; }
        if (loggedUser != null && email.equals(loggedUser.getEmail())) { lblChatStatus.setText("Esti tu."); return; }
        try {
            User partner = service.findUserByEmail(email);
            if (partner != null) {
                lastSearchedUser = partner; lblChatStatus.setText("✅ Gasit: " + partner.getUsername());
                openChatWindow(partner);
                boolean areFriends = service.listAllFriendships().stream().anyMatch(f ->
                        (f.getUser1Email().equals(loggedUser.getEmail()) && f.getUser2Email().equals(partner.getEmail())) ||
                                (f.getUser2Email().equals(loggedUser.getEmail()) && f.getUser1Email().equals(partner.getEmail()))
                );
                if (areFriends) { lblFriendStatus.setVisible(true); lblFriendStatus.setManaged(true); }
                else { btnQuickFriendRequest.setVisible(true); btnQuickFriendRequest.setManaged(true); btnQuickFriendRequest.setText("Invite " + partner.getUsername()); }
            }
        } catch (Exception e) { lblChatStatus.setText("❌ Email negasit."); resetQuickRequestUI(); }
    }

    @FXML
    private void handleQuickRequest() {
        if (lastSearchedUser != null && loggedUser != null) {
            try {
                service.sendFriendRequest(loggedUser.getEmail(), lastSearchedUser.getEmail());
                lblChatStatus.setText("✅ Cerere trimisa!");
                btnQuickFriendRequest.setVisible(false); btnQuickFriendRequest.setManaged(false);
                refreshNotifications();
            } catch (Exception e) { lblChatStatus.setText("❌ " + e.getMessage()); }
        }
    }

    private void resetQuickRequestUI() {
        if(btnQuickFriendRequest != null) { btnQuickFriendRequest.setVisible(false); btnQuickFriendRequest.setManaged(false); }
        if(lblFriendStatus != null) { lblFriendStatus.setVisible(false); lblFriendStatus.setManaged(false); }
        lastSearchedUser = null;
    }

    @FXML
    public void refreshRecentChats() {
        if (loggedUser == null) return;
        recentPartners = service.getRecentChatPartners(loggedUser.getId());
        listRecentChats.setItems(FXCollections.observableArrayList(recentPartners.stream().map(User::getEmail).toList()));
    }

    @FXML private void handleOpenRecentChat() { int idx = listRecentChats.getSelectionModel().getSelectedIndex(); if (idx >= 0) openChatWindow(recentPartners.get(idx)); }

    private void openChatWindow(User partner) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ChatView.fxml"));
            Stage stage = new Stage(); stage.setScene(new Scene(loader.load()));
            ((ChatController)loader.getController()).setChatData(loggedUser, partner);
            stage.setTitle("Chat cu " + partner.getUsername()); stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
    @FXML private Label lblNotificationBadge;

    private void checkNewFriendRequests() {
        long count = service.getPendingRequestsCount(loggedUser.getId());
        if (count > 0) {
            lblNotificationBadge.setText(String.valueOf(count));
            lblNotificationBadge.setVisible(true);
        } else {
            lblNotificationBadge.setVisible(false);
        }
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
        loadDucksPage();
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
        refreshUserLists();
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
            if ("DUCK".equals(comboUserType.getValue())) {
                u = new SwimmingDuck(null, txtUsername.getText(), txtEmail.getText(), txtPassword.getText(), Double.parseDouble(txtSpeed.getText()), Double.parseDouble(txtEndurance.getText()));
            } else {
                u = new Person(null, txtUsername.getText(), txtEmail.getText(), txtPassword.getText(), txtFirstName.getText(), txtLastName.getText(), LocalDate.parse(txtBirthDate.getText()), txtOccupation.getText(), Integer.parseInt(txtEmpathy.getText()));
            }
            service.addUser(u); lblStatus.setText("Succes!"); refreshUserLists(); loadDucksPage(); clearAddForm();
        } catch (Exception e) { lblStatus.setText("Eroare: " + e.getMessage()); }
    }

    @FXML
    private void handleDeleteUser() {
        try { service.removeUserByEmail(txtDeleteEmail.getText()); refreshUserLists(); loadDucksPage(); } catch (Exception e) { lblStatus.setText(e.getMessage()); }
    }

    @FXML
    private void handleRemoveFriend() {
        FriendshipDTO sel = friendshipTable.getSelectionModel().getSelectedItem();
        if (sel != null) {
            try { service.removeFriendByEmails(sel.getUser1Email(), sel.getUser2Email()); refreshUserLists(); } catch (Exception e) { lblStatus.setText(e.getMessage()); }
        }
    }

    @FXML private void handleCalculateStats() { try { lblCommunityCount.setText(String.valueOf(service.numberOfCommunities())); } catch (Exception e) {} }

    private void clearAddForm() {
        txtUsername.clear(); txtEmail.clear(); txtPassword.clear(); txtFirstName.clear(); txtLastName.clear(); txtBirthDate.clear(); txtOccupation.clear(); txtEmpathy.clear(); txtSpeed.clear(); txtEndurance.clear();
    }
}