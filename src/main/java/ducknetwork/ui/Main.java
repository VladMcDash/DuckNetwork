package ducknetwork.ui;

import ducknetwork.domain.*;
import ducknetwork.exceptions.DomainExceptions;
import ducknetwork.repository.Repo;
import ducknetwork.service.NetworkService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Console UI extended with Lab3 functionality (cards + events).
 */
public class Main {

    private static final Repo repo = Repo.getInstance();

    private static final NetworkService service = new NetworkService();
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        //populateDemoData();
        repo.loadFromDatabase();
        try (var conn = ducknetwork.persistence.Database.getConnection()) {
            System.out.println("Conexiunea functioneaza!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        printHelp();
        while (true) {
            System.out.print("> ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;
            String[] tokens = line.split("\\s+");
            String cmd = tokens[0].toLowerCase();
            try {
                switch (cmd) {
                    case "help": printHelp(); break;
                    case "addperson": addPerson(); break;
                    case "addduck": addDuck(); break;
                    case "removeuser": removeUser(); break;
                    case "addfriend": addFriend(); break;
                    case "removefriend": removeFriend(); break;
                    case "list": listUsers(); break;
                    case "communities": showCommunities(); break;
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
            } catch (DomainExceptions.ValidationException ve) {
                System.out.println("Validation: " + ve.getMessage());
            } catch (DomainExceptions.UserNotFoundException unfe) {
                System.out.println("Not found: " + unfe.getMessage());
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private static void printHelp() {
        System.out.println("Commands:");
        System.out.println(" addperson, addduck, removeuser, addfriend, removefriend, list, communities, mostsociable");
        System.out.println(" createcard, listcards, addducktocard, removecard");
        System.out.println(" createevent, createrace, listevents, subscribeevent <eventId> <userId>, unsubscribeevent <eventId> <userId>, runrace <eventId> <M>");
        System.out.println(" help, exit");
    }

    private static void addPerson() {
        System.out.print("username: "); String username = sc.nextLine().trim();
        System.out.print("email: "); String email = sc.nextLine().trim();
        System.out.print("first name: "); String first = sc.nextLine().trim();
        System.out.print("last name: "); String last = sc.nextLine().trim();
        System.out.print("birthdate (yyyy-mm-dd): "); String bd = sc.nextLine().trim();
        LocalDate birth = bd.isEmpty() ? LocalDate.now() : LocalDate.parse(bd);
        System.out.print("occupation: "); String occ = sc.nextLine().trim();
        System.out.print("empathy (int): "); int empathy = Integer.parseInt(sc.nextLine().trim());
        Person p = new Person(null, username, email, "", first, last, birth, occ, empathy);
        repo.addUser(p);
        System.out.println("Added: " + p);
    }

    private static void addDuck() {
        System.out.print("username: "); String username = sc.nextLine().trim();
        System.out.print("email: "); String email = sc.nextLine().trim();
        System.out.print("type (SWIMMING, FLYING, FLYING_AND_SWIMMING): "); String t = sc.nextLine().trim().toUpperCase();
        System.out.print("speed (double): "); double speed = Double.parseDouble(sc.nextLine().trim());
        System.out.print("endurance (double): "); double endurance = Double.parseDouble(sc.nextLine().trim());
        Duck duck;
        switch (t) {
            case "SWIMMING": duck = new SwimmingDuck(null, username, email, "", speed, endurance); break;
            case "FLYING": duck = new FlyingDuck(null, username, email, "", speed, endurance); break;
            case "FLYING_AND_SWIMMING": duck = new FlyingAndSwimmingDuck(null, username, email, "", speed, endurance); break;
            default: System.out.println("Unknown type"); return;
        }
        repo.addUser(duck);
        System.out.println("Added: " + duck);
    }

    private static void removeUser() {
        System.out.print("id: "); Long id = Long.parseLong(sc.nextLine().trim());
        repo.removeUser(id);
        System.out.println("Removed user " + id);
    }

    private static void addFriend() {
        System.out.print("id1: "); Long a = Long.parseLong(sc.nextLine().trim());
        System.out.print("id2: "); Long b = Long.parseLong(sc.nextLine().trim());
        service.addFriend(a, b);
        System.out.println("Friendship added");
    }

    private static void removeFriend() {
        System.out.print("id1: "); Long a = Long.parseLong(sc.nextLine().trim());
        System.out.print("id2: "); Long b = Long.parseLong(sc.nextLine().trim());
        service.removeFriend(a, b);
        System.out.println("Friend removed");
    }

    private static void listUsers() {
        List<User> all = repo.listAllUsers();
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
        System.out.print("Card name: "); String name = sc.nextLine().trim();
        Card c = service.createCard(name);
        System.out.println("Created card: " + c);
    }

    private static void listCards() {
        List<Card> cards = service.listCards();
        if (cards.isEmpty()) System.out.println("(no cards)");
        else cards.forEach(System.out::println);
    }

    private static void addDuckToCard() {
        System.out.print("duckId: "); Long did = Long.parseLong(sc.nextLine().trim());
        System.out.print("cardId: "); Long cid = Long.parseLong(sc.nextLine().trim());
        service.addDuckToCard(did, cid);
        System.out.println("Duck added to card");
    }

    private static void removeCard() {
        System.out.print("cardId: "); Long cid = Long.parseLong(sc.nextLine().trim());
        service.removeCard(cid);
        System.out.println("Card removed");
    }

    private static void createEvent() {
        System.out.print("Event name: "); String name = sc.nextLine().trim();
        Event e = service.createEvent(name);
        System.out.println("Created event: " + e);
    }

    private static void createRaceEvent() {
        System.out.print("Race name: "); String name = sc.nextLine().trim();
        System.out.print("Buoys (comma-separated distances, e.g. 10.0,20.0): "); String b = sc.nextLine().trim();
        List<Double> buoys = Arrays.stream(b.split(",")).map(String::trim).filter(s->!s.isEmpty()).map(Double::parseDouble).toList();
        RaceEvent re = service.createRaceEvent(name, buoys);
        System.out.println("Created race event: " + re);
    }

    private static void listEvents() {
        List<Event> evs = repo.listEvents();
        if (evs.isEmpty()) System.out.println("(no events)");
        else evs.forEach(System.out::println);
    }

    private static void subscribeEvent() {
        System.out.print("eventId: "); Long eid = Long.parseLong(sc.nextLine().trim());
        System.out.print("userId: "); Long uid = Long.parseLong(sc.nextLine().trim());
        service.subscribeToEvent(eid, uid);
        System.out.println("Subscribed");
    }

    private static void unsubscribeEvent() {
        System.out.print("eventId: "); Long eid = Long.parseLong(sc.nextLine().trim());
        System.out.print("userId: "); Long uid = Long.parseLong(sc.nextLine().trim());
        service.unsubscribeFromEvent(eid, uid);
        System.out.println("Unsubscribed");
    }

    private static void runRace() {
        System.out.print("eventId: "); Long eid = Long.parseLong(sc.nextLine().trim());
        System.out.print("M (number of lanes): "); int M = Integer.parseInt(sc.nextLine().trim());
        Map<Duck, Double> results = service.runRace(eid, M);
        System.out.println("Race finished. Results:");
        results.forEach((d,t) -> System.out.printf("%s -> %.3f s%n", d.getUsername(), t));
    }
    private static void populateDemoData() {
        Person p1 = new Person(null, "ana.p", "ana@mail.com", "",
                "Ana", "Popescu", LocalDate.of(2000, 5, 3), "Student", 8);
        Person p2 = new Person(null, "marius.c", "marius@mail.com", "",
                "Marius", "Cojocaru", LocalDate.of(1999, 11, 12), "Programator", 6);
        Person p3 = new Person(null, "diana.t", "diana@mail.com", "",
                "Diana", "Toma", LocalDate.of(2001, 7, 21), "Designer", 9);
        repo.addUser(p1);
        repo.addUser(p2);
        repo.addUser(p3);

        Duck d1 = new SwimmingDuck(null, "ducky", "duck1@mail.com", "", 10.0, 8.0);
        Duck d2 = new FlyingDuck(null, "skyduck", "duck2@mail.com", "", 20.0, 5.0);
        Duck d3 = new FlyingAndSwimmingDuck(null, "hybrid", "duck3@mail.com", "", 15.0, 9.0);
        repo.addUser(d1);
        repo.addUser(d2);
        repo.addUser(d3);

        service.addFriend(p1.getId(), p2.getId());
        service.addFriend(p2.getId(), p3.getId());
        service.addFriend(p1.getId(), d1.getId());
        service.addFriend(d1.getId(), d2.getId());
        service.addFriend(d2.getId(), d3.getId());

        Card swimMasters = service.createCard("SwimMasters");
        Card skyFlyers = service.createCard("SkyFlyers");

        service.addDuckToCard(d1.getId(), swimMasters.getId());
        service.addDuckToCard(d3.getId(), swimMasters.getId());
        service.addDuckToCard(d2.getId(), skyFlyers.getId());

        Event event1 = service.createEvent("Duck Parade");
        RaceEvent race = service.createRaceEvent("Annual Swim Race", List.of(10.0, 20.0, 30.0));

        service.subscribeToEvent(event1.getId(), p1.getId());
        service.subscribeToEvent(event1.getId(), d1.getId());
        service.subscribeToEvent(race.getId(), d1.getId());
        service.subscribeToEvent(race.getId(), d3.getId());

    }
}
