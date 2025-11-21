package ducknetwork.ui;

import ducknetwork.domain.*;
// Am eliminat importul pentru ducknetwork.repository.Repo
import ducknetwork.service.NetworkService;
import ducknetwork.persistence.Database; // Folosim noul Database Singleton
import ducknetwork.exceptions.DomainExceptions;

import java.time.LocalDate;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Optional;

public class Main {

    private static final NetworkService service = new NetworkService();
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try {
            Database.getInstance().getConnection();
            System.out.println("Conexiunea la BD functioneaza");
        } catch (RuntimeException | SQLException e) {
            System.err.println("Eroare la conectarea la baza de date: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nOprire aplicație: Inchidere conexiune DB...");
            Database.getInstance().closeConnection();
            System.out.println("Conexiune DB inchisa.");
        }));

        printHelp();

        while (true) {
            System.out.print("> ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            String cmd = line.toLowerCase();

            try {
                switch (cmd) {
                    case "help": printHelp(); break;
                    case "addperson": addPerson(); break;
                    case "addduck": addDuck(); break;
                    case "removeuser": removeUser(); break;
                    case "addfriend": addFriend(); break;
                    case "removefriend": removeFriend(); break;
                    case "list": listUsers(); break;
                    case "mostsociable": showMostSociable(); break;
                    case "createcard": createCard(); break;
                    case "listcards": listCards(); break;
                    case "addducktocard": addDuckToCard(); break;
                    case "removecard": removeCard(); break;
                    case "createevent": createEvent(); break;
                    case "createrace": createRaceEvent(); break;
                    case "listevents": listEvents(); break;
                    case "subscribeevent": subscribeEvent(); break;
                    case "unsubscribeevent": unsubscribeEvent(); break;
                    case "runrace": runRace(); break;
                    case "exit": System.out.println("bye"); return;
                    default: System.out.println("Unknown command. Type 'help'"); break;
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private static void printHelp() {
        System.out.println("\n=== Meniu ===");
        System.out.println(" addperson");
        System.out.println(" addduck");
        System.out.println(" removeuser");
        System.out.println(" addfriend");
        System.out.println(" removefriend");
        System.out.println(" list");
        System.out.println(" mostsociable");
        System.out.println(" createcard");
        System.out.println(" listcards");
        System.out.println(" addducktocard");
        System.out.println(" removecard");
        System.out.println(" createevent");
        System.out.println(" createrace");
        System.out.println(" listevents");
        System.out.println(" subscribeevent");
        System.out.println(" unsubscribeevent");
        System.out.println(" runrace");
        System.out.println(" exit");
        System.out.println("==============\n");
    }

    private static void addPerson() {
        System.out.print("username: "); String username = sc.nextLine().trim();
        System.out.print("email: "); String email = sc.nextLine().trim();
        System.out.print("password: "); String pass = sc.nextLine().trim();
        System.out.print("first name: "); String first = sc.nextLine().trim();
        System.out.print("last name: "); String last = sc.nextLine().trim();
        System.out.print("birthdate (yyyy-mm-dd): "); String bd = sc.nextLine().trim();
        LocalDate birth = bd.isEmpty() ? null : LocalDate.parse(bd);
        System.out.print("occupation: "); String occ = sc.nextLine().trim();
        System.out.print("empathy (int): "); int empathy = Integer.parseInt(sc.nextLine().trim());

        Person p = new Person(null, username, email, pass, first, last, birth, occ, empathy);
        service.addUser(p);

        System.out.println("Added: " + p);
    }

    private static void addDuck() {
        System.out.print("username: "); String username = sc.nextLine().trim();
        System.out.print("email: "); String email = sc.nextLine().trim();
        System.out.print("password: "); String pass = sc.nextLine().trim();
        System.out.print("type (SWIMMING, FLYING, FLYING_AND_SWIMMING): ");
        String t = sc.nextLine().trim().toUpperCase();
        System.out.print("speed (double): "); double speed = Double.parseDouble(sc.nextLine().trim());
        System.out.print("endurance (double): "); double endurance = Double.parseDouble(sc.nextLine().trim());

        Duck duck;
        switch (t) {
            case "SWIMMING": duck = new SwimmingDuck(null, username, email, pass, speed, endurance); break;
            case "FLYING": duck = new FlyingDuck(null, username, email, pass, speed, endurance); break;
            case "FLYING_AND_SWIMMING": duck = new FlyingAndSwimmingDuck(null, username, email, pass, speed, endurance); break;
            default: System.out.println("Unknown type"); return;
        }

        service.addUser(duck);
        System.out.println("Added: " + duck);
    }

    private static void removeUser() {
        System.out.print("id: ");
        Long id = Long.parseLong(sc.nextLine().trim());
        service.removeUser(id);
        System.out.println("Removed user " + id);
    }

    private static void addFriend() {
        System.out.print("id1: "); Long a = Long.parseLong(sc.nextLine().trim());
        System.out.print("id2: "); Long b = Long.parseLong(sc.nextLine().trim());
        service.addFriend(a, b);
        System.out.println("Friendship added.");
    }

    private static void removeFriend() {
        System.out.print("id1: "); Long a = Long.parseLong(sc.nextLine().trim());
        System.out.print("id2: "); Long b = Long.parseLong(sc.nextLine().trim());
        service.removeFriend(a, b);
        System.out.println("Friendship removed.");
    }

    private static void listUsers() {
        List<User> all = service.listAllUsers();
        if (all.isEmpty()) System.out.println("(no users)");
        else all.forEach(System.out::println);
    }

    private static void showCommunities() {
        System.out.println("Number of communities: " + service.numberOfCommunities());
    }

    private static void showMostSociable() {
        List<User> best = service.mostSociableCommunity();
        System.out.println("Most sociable community (size " + best.size() + "):");
        best.forEach(System.out::println);
    }

    private static void createCard() {
        System.out.print("Card name: ");
        String name = sc.nextLine().trim();
        Card c = service.createCard(name);
        System.out.println("Created card: " + c);
    }

    private static void listCards() {
        List<Card> cards = service.listCards();
        if (cards.isEmpty()) System.out.println("(no cards)");
        else cards.forEach(System.out::println);
    }

    private static void addDuckToCard() {
        System.out.print("duckId: ");
        Long did = Long.parseLong(sc.nextLine());
        System.out.print("cardId: ");
        Long cid = Long.parseLong(sc.nextLine());
        service.addDuckToCard(did, cid);
        System.out.println("Duck added to card.");
    }

    private static void removeCard() {
        System.out.print("cardId: ");
        Long id = Long.parseLong(sc.nextLine().trim());
        service.removeCard(id);
        System.out.println("Card removed.");
    }

    private static void createEvent() {
        System.out.print("Event name: ");
        String name = sc.nextLine().trim();
        Event e = service.createEvent(name);
        System.out.println("Created event: " + e);
    }

    private static void createRaceEvent() {
        System.out.print("Race name: ");
        String name = sc.nextLine().trim();
        System.out.print("Buoys (comma-separated): ");
        String b = sc.nextLine().trim();

        List<Double> buoys = Arrays.stream(b.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Double::parseDouble)
                .toList();

        RaceEvent re = service.createRaceEvent(name, buoys);
        System.out.println("Created race event: " + re);
    }

    private static void listEvents() {
        List<Event> evs = service.listEvents();
        if (evs.isEmpty()) System.out.println("(no events)");
        else evs.forEach(System.out::println);
    }

    private static void subscribeEvent() {
        System.out.print("eventId: "); Long eid = Long.parseLong(sc.nextLine());
        System.out.print("userId: "); Long uid = Long.parseLong(sc.nextLine());
        service.subscribeToEvent(eid, uid);
        System.out.println("Subscribed.");
    }

    private static void unsubscribeEvent() {
        System.out.print("eventId: "); Long eid = Long.parseLong(sc.nextLine());
        System.out.print("userId: "); Long uid = Long.parseLong(sc.nextLine());
        service.unsubscribeFromEvent(eid, uid);
        System.out.println("Unsubscribed.");
    }

    private static void runRace() {
        System.out.print("eventId: "); Long eid = Long.parseLong(sc.nextLine());
        System.out.print("M (lanes): "); int M = Integer.parseInt(sc.nextLine());

        Map<Duck, Double> results = service.runRace(eid, M);
        System.out.println("Race finished. Results:");

        results.forEach((d,t) ->
                System.out.printf("%s -> %.3f s%n", d.getUsername(), t)
        );
    }
}