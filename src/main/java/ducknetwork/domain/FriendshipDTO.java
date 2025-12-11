package ducknetwork.domain;

/**
 *Object folosit exclusiv de GUI pentru a afișa prieteniile prin Email.
 */
public class FriendshipDTO {
    private final String user1Email;
    private final String user2Email;

    public FriendshipDTO(String user1Email, String user2Email) {
        this.user1Email = user1Email;
        this.user2Email = user2Email;
    }

    public String getUser1Email() { return user1Email; }
    public String getUser2Email() { return user2Email; }
}