package ducknetwork.gui;

import ducknetwork.domain.Message;
import ducknetwork.domain.User;
import ducknetwork.service.NetworkService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import ducknetwork.domain.ReplyMessage;

import java.util.List;

public class ChatController {
    private final NetworkService service = new NetworkService();

    private User currentUser;
    private User chatPartner;

    @FXML private ListView<String> messageList;
    @FXML private TextArea inputMessage;

    private Message selectedMessageForReply = null;

    public void setChatData(User currentUser, User chatPartner) {
        this.currentUser = currentUser;
        this.chatPartner = chatPartner;
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

    private void loadMessages() {
        List<Message> messages = service.getConversation(currentUser.getId(), chatPartner.getId());
        messageList.getItems().clear();
        for (Message m : messages) {
            String prefix = m.getFrom().getId().equals(currentUser.getId()) ? "Eu" : chatPartner.getUsername();
            String replyText = "";
            if (m instanceof ReplyMessage) {
                Message original = ((ReplyMessage) m).getReply();
                if (original != null) {
                    replyText = " [Reply to: " + original.getMessage() + "] ";
                }
            }
            messageList.getItems().add(prefix + replyText + ": " + m.getMessage());
        }
        messageList.scrollTo(messageList.getItems().size() - 1);
    }

    @FXML
    private void handleSendMessage() {
        String text = inputMessage.getText().trim();
        if (!text.isEmpty()) {
            service.sendMessage(currentUser.getId(), List.of(chatPartner.getEmail()), text, selectedMessageForReply);
            inputMessage.clear();
            inputMessage.setPromptText("Scrie un mesaj...");
            selectedMessageForReply = null;
            loadMessages();
        }
    }
}