package ducknetwork.domain;

import java.time.LocalDateTime;
import java.util.List;

public class Message {
    private Long id;
    private User from;
    private List<User> to;
    private String message;
    private LocalDateTime date;
    private Message reply;

    public Message(Long id, User from, List<User> to, String message, LocalDateTime date) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.message = message;
        this.date = date;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getFrom() { return from; }
    public List<User> getTo() { return to; }
    public String getMessage() { return message; }
    public LocalDateTime getDate() { return date; }

    public Message getReply() { return reply; }
    public void setReply(Message reply) { this.reply = reply; }

    @Override
    public String toString() {
        return from.getUsername() + ": " + message + " (" + date + ")";
    }
}