package ducknetwork.gui;

import ducknetwork.domain.Message;
import ducknetwork.domain.User;
import ducknetwork.domain.ReplyMessage;
import ducknetwork.service.NetworkService;
import ducknetwork.util.Observer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Collections;
import java.util.List;

public class ChatController implements Observer {
    private final NetworkService service = NetworkService.getInstance();
    private User currentUser;
    private User chatPartner;
    private Message selectedMessageForReply = null;

    @FXML private ListView<String> messageList;
    @FXML private TextArea inputMessage;

    public void setChatData(User current, User partner) {
        this.currentUser = current;
        this.chatPartner = partner;
        service.addObserver(this);
        loadMessages();

        messageList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            int index = messageList.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                List<Message> msgs = service.getConversation(currentUser.getId(), chatPartner.getId());
                if (index < msgs.size()) {
                    selectedMessageForReply = msgs.get(index);
                    inputMessage.setPromptText("Reply to: " + selectedMessageForReply.getMessage().substring(0, Math.min(selectedMessageForReply.getMessage().length(), 20)) + "...");
                }
            }
        });
    }

    @Override
    public void update() { Platform.runLater(this::loadMessages); }

    private void loadMessages() {
        List<Message> messages = service.getConversation(currentUser.getId(), chatPartner.getId());
        messageList.getItems().clear();
        for (Message m : messages) {
            String prefix = m.getFrom().getId().equals(currentUser.getId()) ? "Eu" : chatPartner.getUsername();
            String replyText = (m instanceof ReplyMessage && ((ReplyMessage) m).getReply() != null)
                    ? " [Reply to: " + ((ReplyMessage) m).getReply().getMessage() + "] " : "";
            messageList.getItems().add(prefix + replyText + ": " + m.getMessage());
        }
        if (!messageList.getItems().isEmpty()) messageList.scrollTo(messageList.getItems().size() - 1);
    }

    @FXML
    private void handleSendMessage() {
        String text = inputMessage.getText().trim();
        if (!text.isEmpty()) {
            service.sendMessage(currentUser.getId(), Collections.singletonList(chatPartner.getEmail()), text, selectedMessageForReply);
            inputMessage.clear();
            inputMessage.setPromptText("Scrie un mesaj...");
            selectedMessageForReply = null;
        }
    }
}