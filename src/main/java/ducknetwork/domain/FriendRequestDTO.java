package ducknetwork.domain;

import java.time.LocalDateTime;

public class FriendRequestDTO {
    private String fromEmail;
    private FriendshipStatus status;
    private LocalDateTime date;

    public FriendRequestDTO(String fromEmail, FriendshipStatus status, LocalDateTime date) {
        this.fromEmail = fromEmail;
        this.status = status;
        this.date = date;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public LocalDateTime getDate() {
        return date;
    }
}