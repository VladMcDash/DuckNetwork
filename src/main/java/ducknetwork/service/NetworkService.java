package ducknetwork.service;

import ducknetwork.domain.*;
import ducknetwork.exceptions.DomainExceptions;
import ducknetwork.persistence.Database;
import ducknetwork.repository.*;
import ducknetwork.util.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class NetworkService {

    private final UserRepository userRepo = new UserRepository();
    private final FriendRepository friendRepo = new FriendRepository();
    private final DuckRepository duckRepo = new DuckRepository();
    private final MessageRepository messageRepo = new MessageRepository();
    private final FriendRequestRepository requestRepo = new FriendRequestRepository();

    public User login(String username, String rawPassword) {
        User user = userRepo.findByUsername(username);
        if (user == null) throw new RuntimeException("Utilizator inexistent!");
        String inputHash = PasswordHasher.hash(rawPassword);
        if (!user.getPassword().equals(inputHash)) throw new RuntimeException("Parola incorecta!");
        return user;
    }


    public void sendMessage(Long fromUserId, List<String> toEmails, String text, Message replyTo) {
        User from = findById(fromUserId);
        List<User> recipients = toEmails.stream()
                .map(this::findUserByEmail)
                .collect(Collectors.toList());

        Message msg;
        if (replyTo != null) {
            msg = new ReplyMessage(null, from, recipients, text, LocalDateTime.now(), replyTo);
        } else {
            msg = new Message(null, from, recipients, text, LocalDateTime.now());
        }
        messageRepo.save(msg);
    }

    public List<Message> getConversation(Long user1Id, Long user2Id) {
        return messageRepo.findConversation(user1Id, user2Id);
    }

    public List<User> getRecentChatPartners(Long userId) {
        List<User> partners = new ArrayList<>();
        String sql = "SELECT DISTINCT CASE WHEN from_user_id = ? THEN mr.to_user_id ELSE from_user_id END as partner_id " +
                "FROM messages m JOIN message_recipients mr ON m.id = mr.message_id " +
                "WHERE from_user_id = ? OR mr.to_user_id = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId); ps.setLong(2, userId); ps.setLong(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User p = userRepo.findById(rs.getLong("partner_id"));
                    if (p != null) partners.add(p);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return partners;
    }


    public List<FriendRequestDTO> getAllUserRequests(Long userId) {
        return requestRepo.findAllRequestsForUser(userId, userRepo);
    }

    public void sendFriendRequest(String fromEmail, String toEmail) {
        User from = findUserByEmail(fromEmail);
        User to = findUserByEmail(toEmail);
        requestRepo.save(from.getId(), to.getId());
    }

    public void acceptFriendRequest(String fromEmail, String toEmail) {
        User from = findUserByEmail(fromEmail);
        User to = findUserByEmail(toEmail);
        requestRepo.updateStatus(from.getId(), to.getId(), "APPROVED");
        addFriendByEmails(fromEmail, toEmail);
    }

    public void rejectFriendRequest(String fromEmail, String toEmail) {
        User from = findUserByEmail(fromEmail);
        User to = findUserByEmail(toEmail);
        requestRepo.updateStatus(from.getId(), to.getId(), "REJECTED");
    }
    public long getPendingRequestsCount(Long userId) {
        return getAllUserRequests(userId).stream()
                .filter(r -> r.getFromEmail().startsWith("RECEIVED FROM:") && r.getStatus().equals("PENDING"))
                .count();
    }

    //
    public User findUserByEmail(String email) {
        User u = userRepo.findByEmail(email);
        if (u == null) throw new DomainExceptions.UserNotFoundException("Email negasit: " + email);
        return u;
    }

    public User findById(Long id) {
        User u = userRepo.findById(id);
        if (u == null) throw new DomainExceptions.UserNotFoundException("ID negasit: " + id);
        return u;
    }

    public List<User> listAllUsers() { return userRepo.findAll(); }

    public List<FriendshipDTO> listAllFriendships() {
        List<User> allUsers = userRepo.findAll();
        List<FriendshipDTO> friendships = new ArrayList<>();
        Set<String> processedPairs = new HashSet<>();
        for (User u : allUsers) {
            String e1 = u.getEmail();
            for (Long fid : friendRepo.getFriendIds(u.getId())) {
                User f = userRepo.findById(fid);
                if (f == null) continue;
                String e2 = f.getEmail();
                String pair = e1.compareTo(e2) < 0 ? e1 + "-" + e2 : e2 + "-" + e1;
                if (processedPairs.add(pair)) friendships.add(new FriendshipDTO(e1, e2));
            }
        }
        return friendships;
    }

    public void addFriendByEmails(String e1, String e2) {
        User u1 = findUserByEmail(e1);
        User u2 = findUserByEmail(e2);
        friendRepo.addFriend(u1.getId(), u2.getId());
    }

    public void removeFriendByEmails(String e1, String e2) {
        User u1 = findUserByEmail(e1);
        User u2 = findUserByEmail(e2);
        friendRepo.removeFriend(u1.getId(), u2.getId());
    }

    public void removeUserByEmail(String email) {
        User u = findUserByEmail(email);
        userRepo.delete(u.getId());
    }

    public User addUser(User user) { return userRepo.save(user); }

    public Page<Duck> getDucksPage(String type, int page, int size) {
        return duckRepo.findPage(type, page, size);
    }

    public int numberOfCommunities() { return getCommunities().size(); }

    public List<List<User>> getCommunities() {
        List<User> all = userRepo.findAll();
        Map<Long, User> byId = all.stream().collect(Collectors.toMap(User::getId, u -> u));
        Set<Long> visited = new HashSet<>();
        List<List<User>> components = new ArrayList<>();
        for (User u : all) {
            if (!visited.contains(u.getId())) components.add(bfsCollect(u.getId(), visited, byId));
        }
        return components;
    }

    private List<User> bfsCollect(Long start, Set<Long> visited, Map<Long, User> byId) {
        List<User> comp = new ArrayList<>();
        Queue<Long> q = new LinkedList<>();
        visited.add(start); q.add(start);
        while (!q.isEmpty()) {
            Long cur = q.poll();
            User u = byId.get(cur);
            if (u != null) {
                comp.add(u);
                for (Long fid : friendRepo.getFriendIds(cur)) {
                    if (!visited.contains(fid)) { visited.add(fid); q.add(fid); }
                }
            }
        }
        return comp;
    }

    public List<User> mostSociableCommunity() {
        List<List<User>> comps = getCommunities();
        return comps.stream().max(Comparator.comparingInt(List::size)).orElse(Collections.emptyList());
    }
}