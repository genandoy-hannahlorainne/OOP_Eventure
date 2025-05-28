package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class AttendeeDashboard {
    private int attendeeID;

    public void show(Stage stage) {
        show(stage, 1); // Default attendeeID for testing
    }

    public void show(Stage stage, int attendeeID) {
        this.attendeeID = attendeeID;

        // --- Top Navigation Bar ---
        HBox navBar = new HBox(20);
        navBar.setPadding(new Insets(10));
        navBar.setStyle("-fx-background-color: #2c3e50;");
        navBar.setAlignment(Pos.CENTER_LEFT);

        Button eventsBtn = new Button("Switch Account");
        Button calendarBtn = new Button("Calendar");
        Button myEventsBtn = new Button("My Events");
        Button notificationsBtn = new Button("Notifications");
        Button profileBtn = new Button("Profile");

        navBar.getChildren().addAll(eventsBtn, calendarBtn, myEventsBtn, notificationsBtn, profileBtn);

        // --- Table of Events ---
        TableView<Event> eventTable = new TableView<>();
        TableColumn<Event, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));

        TableColumn<Event, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("startDate"));

        TableColumn<Event, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("endDate"));

        TableColumn<Event, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button registerBtn = new Button("Register");
            private final Button viewBtn = new Button("View Event");
            private final HBox pane = new HBox(10, registerBtn, viewBtn);

            {
                registerBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    registerToEvent(event.getEventID(), attendeeID);
                });

                viewBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Event Details");
                    alert.setHeaderText(event.getName());
                    alert.setContentText(
                        "Start Date: " + event.getStartDate() + "\n" +
                        "End Date: " + event.getEndDate() + "\n" +
                        "Event ID: " + event.getEventID()
                    );
                    alert.showAndWait();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });

        eventTable.getColumns().addAll(nameCol, startDateCol, endDateCol, actionCol);
        eventTable.setPrefWidth(600);

        // --- Load Events from DB ---
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT eventID, name, startDate, endDate FROM Event";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                eventTable.getItems().add(new Event(
                        rs.getInt("eventID"),
                        rs.getString("name"),
                        rs.getString("startDate"),
                        rs.getString("endDate")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        VBox layout = new VBox(15, navBar, eventTable);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(layout, 800, 500);
        stage.setTitle("Attendee Dashboard");
        stage.setScene(scene);
        stage.show();

        // --- Navigation Button Actions ---
        myEventsBtn.setOnAction(e -> {
            MyEventsPage myEventsPage = new MyEventsPage();
            myEventsPage.show(new Stage(), attendeeID);
        });

        profileBtn.setOnAction(e -> {
            ProfilePage profilePage = new ProfilePage();
            profilePage.show(new Stage(), attendeeID);
        });

        calendarBtn.setOnAction(e -> {
            CalendarPage calendarPage = new CalendarPage();
            calendarPage.show(new Stage(), attendeeID);
        });

        notificationsBtn.setOnAction(e -> {
            NotificationPage notificationPage = new NotificationPage();
            notificationPage.show(new Stage(), attendeeID);
        });

        eventsBtn.setOnAction(e -> {
            new LoginPage(new Stage()); // Show login page
            stage.close();              // Close attendee dashboard
        });
    }

    private void registerToEvent(int eventId, int attendeeId) {
        try (Connection conn = DBConnection.getConnection()) {
            // Step 1: Insert into Registration
            String sql = "INSERT INTO Registration (userID, eventID, registrationDate, registrationStatus) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, attendeeId);
            stmt.setInt(2, eventId);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setString(4, "Registered");
            stmt.executeUpdate();

            // Step 2: Insert a Notification for the user
            String notifSql = "INSERT INTO Notification (name, title, message, createdAt, notificationType, userID) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement notifStmt = conn.prepareStatement(notifSql);
            notifStmt.setString(1, "Event Registration");
            notifStmt.setString(2, "You're Registered!");
            notifStmt.setString(3, "You have successfully registered for event ID " + eventId + ".");
            notifStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            notifStmt.setString(5, "event");
            notifStmt.setInt(6, attendeeId);
            notifStmt.executeUpdate();

            // Optional: Show confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Registered successfully and notification sent!");
            alert.showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Registration failed: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // Event model class
    public static class Event {
        private final int eventID;
        private final String name;
        private final String startDate;
        private final String endDate;

        public Event(int eventID, String name, String startDate, String endDate) {
            this.eventID = eventID;
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public int getEventID() { return eventID; }
        public String getName() { return name; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
    }
}