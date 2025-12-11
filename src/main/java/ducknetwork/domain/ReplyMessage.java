package ducknetwork.domain;

import java.time.LocalDateTime;
import java.util.List;

public class ReplyMessage extends Message {

    public ReplyMessage(Long id, User from, List<User> to, String message, LocalDateTime date, Message originalMessage) {
        super(id, from, to, message, date);
        this.setReply(originalMessage);
    }
}