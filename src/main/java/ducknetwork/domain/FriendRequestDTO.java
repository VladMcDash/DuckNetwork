package ducknetwork.domain;

import java.time.LocalDateTime;

public class FriendRequestDTO {
    private final String fromEmail;
    private final String status;
    private final LocalDateTime date;

    public FriendRequestDTO(String fromEmail, String status, LocalDateTime date) {
        this.fromEmail = fromEmail;
        this.status = status;
        this.date = date;
    }

    public String getFromEmail() { return fromEmail; }
    public String getStatus() { return status; }
    public LocalDateTime getDate() { return date; }
}