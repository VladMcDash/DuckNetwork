package ducknetwork.service;

import ducknetwork.domain.*;
import ducknetwork.exceptions.DomainExceptions;
import ducknetwork.persistence.Database;
import ducknetwork.repository.*;
import ducknetwork.util.Observer;
import ducknetwork.util.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class NetworkService {
    // --- SINGLETON PATTERN ---
    private static NetworkService instance;

    public static NetworkService getInstance() {
        if (instance == null) {
            instance = new NetworkService();
        }
        return instance;
    }

    private final UserRepository userRepo = new UserRepository();
    private final FriendRepository friendRepo = new FriendRepository();
    private final DuckRepository duckRepo = new DuckRepository();
    private final MessageRepository messageRepo = new MessageRepository();
    private final FriendRequestRepository requestRepo = new FriendRequestRepository();

    private final List<Observer> observers = new ArrayList<>();

    private NetworkService() {}

    public void addObserver(Observer o) { observers.add(o); }
    public void removeObserver(Observer o) { observers.remove(o); }
    public void notifyObservers() { observers.forEach(Observer::update); }

    public User login(String username, String rawPassword) {
        User user = userRepo.findByUsername(username);
        if (user == null) throw new RuntimeException("Utilizator inexistent!");
        String inputHash = PasswordHasher.hash(rawPassword);
        if (!user.getPassword().equals(inputHash)) throw new RuntimeException("Parola incorecta!");
        return user;
    }

    // --- FRIEND REQUESTS ---
    public void sendFriendRequest(String fromEmail, String toEmail) {
        User from = findUserByEmail(fromEmail);
        User to = findUserByEmail(toEmail);
        requestRepo.save(from.getId(), to.getId());
        notifyObservers();
    }

    public void acceptFriendRequest(String fromEmail, String toEmail) {
        User from = findUserByEmail(fromEmail);
        User to = findUserByEmail(toEmail);
        requestRepo.updateStatus(from.getId(), to.getId(), FriendshipStatus.APPROVED);
        friendRepo.addFriend(from.getId(), to.getId());
        notifyObservers();
    }

    public void rejectFriendRequest(String fromEmail, String toEmail) {
        User from = findUserByEmail(fromEmail);
        User to = findUserByEmail(toEmail);
        requestRepo.updateStatus(from.getId(), to.getId(), FriendshipStatus.REJECTED);
        notifyObservers();
    }

    public long getPendingRequestsCount(Long userId) {
        return getAllUserRequests(userId).stream()
                .filter(r -> r.getFromEmail().startsWith("RECEIVED FROM:") &&
                        r.getStatus() == FriendshipStatus.PENDING)
                .count();
    }

    public List<FriendRequestDTO> getAllUserRequests(Long userId) {
        return requestRepo.findAllRequestsForUser(userId, userRepo);
    }

    // --- MESSAGES & RECENT CHATS ---
    public void sendMessage(Long fromUserId, List<String> toEmails, String text, Message replyTo) {
        User from = findById(fromUserId);
        List<User> recipients = toEmails.stream().map(this::findUserByEmail).collect(Collectors.toList());
        Message msg = (replyTo != null) ? new ReplyMessage(null, from, recipients, text, LocalDateTime.now(), replyTo)
                : new Message(null, from, recipients, text, LocalDateTime.now());
        messageRepo.save(msg);
        notifyObservers();
    }

    public List<Message> getConversation(Long u1, Long u2) {
        return messageRepo.findConversation(u1, u2);
    }

    /**
     * Returnează numărul total de mesaje primite de utilizator (pentru detectarea mesajelor noi).
     */
    public long getTotalMessagesReceivedCount(Long userId) {
        String sql = "SELECT COUNT(*) FROM message_recipients WHERE to_user_id = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<User> getRecentChatPartners(Long userId) {
        List<User> partners = new ArrayList<>();
        // Interogare SQL pentru a extrage ID-urile partenerilor de chat (atât expeditori cât și destinatari)
        String sql = "SELECT partner_id FROM (" +
                "  SELECT mr.to_user_id AS partner_id, MAX(m.date_sent) as last_date " +
                "  FROM messages m JOIN message_recipients mr ON m.id = mr.message_id " +
                "  WHERE m.from_user_id = ? GROUP BY mr.to_user_id " +
                "  UNION " +
                "  SELECT m.from_user_id AS partner_id, MAX(m.date_sent) as last_date " +
                "  FROM messages m JOIN message_recipients mr ON m.id = mr.message_id " +
                "  WHERE mr.to_user_id = ? GROUP BY m.from_user_id" +
                ") AS combined GROUP BY partner_id ORDER BY MAX(last_date) DESC";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User p = userRepo.findById(rs.getLong("partner_id"));
                    if (p != null) partners.add(p);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return partners;
    }

    // --- UTILS ---
    public User findUserByEmail(String email) {
        User u = userRepo.findByEmail(email);
        if (u == null) throw new DomainExceptions.UserNotFoundException("Email negasit: " + email);
        return u;
    }

    public User findById(Long id) {
        User u = userRepo.findById(id);
        if (u == null) throw new DomainExceptions.UserNotFoundException("ID negasit");
        return u;
    }

    public List<User> listAllUsers() { return userRepo.findAll(); }

    public List<FriendshipDTO> listAllFriendships() {
        List<User> all = userRepo.findAll();
        List<FriendshipDTO> friendships = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        for (User u : all) {
            for (Long fid : friendRepo.getFriendIds(u.getId())) {
                User f = userRepo.findById(fid);
                if (f == null) continue;
                String pair = u.getEmail().compareTo(f.getEmail()) < 0 ? u.getEmail() + "-" + f.getEmail() : f.getEmail() + "-" + u.getEmail();
                if (processed.add(pair)) friendships.add(new FriendshipDTO(u.getEmail(), f.getEmail()));
            }
        }
        return friendships;
    }

    public void removeFriendByEmails(String e1, String e2) {
        User u1 = findUserByEmail(e1);
        User u2 = findUserByEmail(e2);
        friendRepo.removeFriend(u1.getId(), u2.getId());
        notifyObservers();
    }

    public void removeUserByEmail(String email) {
        User u = findUserByEmail(email);
        userRepo.delete(u.getId());
        notifyObservers();
    }

    public User addUser(User user) {
        User u = userRepo.save(user);
        notifyObservers();
        return u;
    }

    public Page<Duck> getDucksPage(String type, int page, int size) { return duckRepo.findPage(type, page, size); }

    public int numberOfCommunities() { return getCommunities().size(); }

    public List<List<User>> getCommunities() {
        List<User> all = userRepo.findAll();
        Map<Long, User> byId = all.stream().collect(Collectors.toMap(User::getId, u -> u));
        Set<Long> visited = new HashSet<>();
        List<List<User>> components = new ArrayList<>();
        for (User u : all) { if (!visited.contains(u.getId())) components.add(bfsCollect(u.getId(), visited, byId)); }
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
                for (Long fid : friendRepo.getFriendIds(cur)) { if (!visited.contains(fid)) { visited.add(fid); q.add(fid); } }
            }
        }
        return comp;
    }

    public List<User> mostSociableCommunity() {
        List<List<User>> comps = getCommunities();
        return comps.stream().max(Comparator.comparingInt(List::size)).orElse(Collections.emptyList());
    }
}