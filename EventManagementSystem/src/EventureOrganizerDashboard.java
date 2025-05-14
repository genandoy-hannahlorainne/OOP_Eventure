import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventureOrganizerDashboard extends Application {

    private final int organizerId = 2; // example organizer ID

    // GUI Entry Point
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Welcome to EVENTURE");
        title.setFont(new Font("Arial", 24));

        // Created Events Panel
        VBox eventBox = new VBox(10);
        eventBox.getChildren().add(new Label("My Created Events"));
        List<Event> events = fetchEvents();
        for (Event event : events) {
            VBox eventCard = new VBox();
            eventCard.setStyle("-fx-background-color: #f1d74f; -fx-padding: 10;");
            eventCard.getChildren().addAll(
                new Label(event.name),
                new Label(event.date + " - " + event.location),
                new Label("Registrations: " + event.registrations + "/" + event.maxCapacity),
                new HBox(10, new Button("VIEW"), new Button("EDIT"))
            );
            eventBox.getChildren().add(eventCard);
        }

        // Notification Panel
        VBox notifBox = new VBox(10);
        notifBox.getChildren().add(new Label("Notification"));
        List<Notification> notifications = fetchNotifications();
        for (Notification notif : notifications) {
            VBox notifCard = new VBox();
            notifCard.setStyle("-fx-background-color: #f1d74f; -fx-padding: 8;");
            notifCard.getChildren().addAll(
                new Label(notif.title),
                new Label(notif.message + " - " + notif.time)
            );
            notifBox.getChildren().add(notifCard);
        }

        // Analytics Panel
        VBox analytics = new VBox(5);
        analytics.getChildren().add(new Label("Event Analytics"));
        analytics.setStyle("-fx-background-color: #f1d74f; -fx-padding: 10;");
        analytics.getChildren().addAll(
            new Label("Total Events: " + events.size()),
            new Label("Total Registration: " + events.stream().mapToInt(e -> e.registrations).sum()),
            new Label("Average Attendance Rate: 87%")
        );

        HBox dashboard = new HBox(50, eventBox, notifBox, analytics);
        root.getChildren().addAll(title, dashboard);

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("Organizer Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:sqlserver://localhost:1433;databaseName=EventManagementSystem;encrypt=true;trustServerCertificate=true",
            "LMS_Admin",
            "moks123"
        );
    }

    private List<Event> fetchEvents() {
        List<Event> events = new ArrayList<>();
        // Modify query to fetch all events, not just the ones by a specific organizer
        String query = "SELECT E.eventID, E.name, E.location, E.startDate, " +
                       "(SELECT COUNT(*) FROM Registration R WHERE R.eventID = E.eventID) as regCount " +
                       "FROM Event E";  // No WHERE clause, fetch all events
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                events.add(new Event(
                    rs.getInt("eventID"),
                    rs.getString("name"),
                    rs.getString("location"),
                    rs.getDate("startDate").toString(),
                    rs.getInt("regCount"),
                    100 // Default maxCapacity, can adjust based on your data
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    private List<Notification> fetchNotifications() {
        List<Notification> notifs = new ArrayList<>();
        String query = "SELECT title, message, createdAt FROM Notification WHERE userID = ? ORDER BY createdAt DESC";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, organizerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notifs.add(new Notification(
                    rs.getString("title"),
                    rs.getString("message"),
                    rs.getTimestamp("createdAt").toString()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifs;
    }

    // Nested static classes (since we're in a single file)
    private static class Event {
        int eventID;
        String name;
        String location;
        String date;
        int registrations;
        int maxCapacity;

        public Event(int eventID, String name, String location, String date, int registrations, int maxCapacity) {
            this.eventID = eventID;
            this.name = name;
            this.location = location;
            this.date = date;
            this.registrations = registrations;
            this.maxCapacity = maxCapacity;
        }
    }

    private static class Notification {
        String title;
        String message;
        String time;

        public Notification(String title, String message, String time) {
            this.title = title;
            this.message = message;
            this.time = time;
        }
    }
}
