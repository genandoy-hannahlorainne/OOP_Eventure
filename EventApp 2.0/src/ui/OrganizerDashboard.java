package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class OrganizerDashboard {
    private int organizerID;

    public void show(Stage stage, int organizerID) {
        this.organizerID = organizerID;

        // --- Top Navigation Bar ---
        HBox navBar = new HBox(20);
        navBar.setPadding(new Insets(10));
        navBar.setStyle("-fx-background-color: #2c3e50;");
        navBar.setAlignment(Pos.CENTER_LEFT);

        Button eventsBtn = new Button("Events");
        Button attendeesBtn = new Button("Attendees");
        Button myEventsBtn = new Button("My Events");
        Button notificationsBtn = new Button("Notifications");
        Button createEventBtn = new Button("Create Event");
        Button profileBtn = new Button("Profile");

        navBar.getChildren().addAll(eventsBtn, attendeesBtn, myEventsBtn, notificationsBtn, createEventBtn, profileBtn);

        // --- Welcome and User Info ---
        Label welcomeLabel = new Label("Welcome, Organizer #" + organizerID);
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox header = new VBox(5, welcomeLabel);
        header.setPadding(new Insets(10));

     // --- Events Table ---
        TableView<Event> eventsTable = new TableView<>();

        TableColumn<Event, Integer> idCol = new TableColumn<>("Event ID");
        idCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("eventID"));

        TableColumn<Event, String> nameCol = new TableColumn<>("Event Name");
        nameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("eventName"));

        TableColumn<Event, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("startDate"));

        TableColumn<Event, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("endDate"));

        eventsTable.getColumns().addAll(idCol, nameCol, startDateCol, endDateCol);
        eventsTable.setPrefWidth(500);

        // --- Fetch events from database ---
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT eventID, name, startDate, endDate FROM Event WHERE organizerID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, organizerID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                eventsTable.getItems().add(new Event(
                        rs.getInt("eventID"),
                        rs.getString("name"),
                        rs.getString("startDate"),
                        rs.getString("endDate")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // --- Notifications Panel (Right Column Top) ---
        VBox notificationsBox = new VBox(10);
        notificationsBox.setPadding(new Insets(10));
        notificationsBox.setStyle("-fx-border-color: gray; -fx-border-width: 1;");
        Label notifLabel = new Label("Notifications");
        notifLabel.setStyle("-fx-font-weight: bold;");
        ListView<String> notificationsList = new ListView<>();
        notificationsList.getItems().addAll("No new notifications.");
        notificationsBox.getChildren().addAll(notifLabel, notificationsList);

        // --- Analytics Panel (Right Column Bottom) ---
        VBox analyticsBox = new VBox(10);
        analyticsBox.setPadding(new Insets(10));
        analyticsBox.setStyle("-fx-border-color: gray; -fx-border-width: 1;");
        Label analyticsLabel = new Label("Event Analytics");
        analyticsLabel.setStyle("-fx-font-weight: bold;");
        analyticsBox.getChildren().addAll(analyticsLabel, new Label("Total Events: " + eventsTable.getItems().size()));

        VBox rightColumn = new VBox(20, notificationsBox, analyticsBox);
        rightColumn.setPrefWidth(300);

        // --- Grid Layout ---
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(20);
        grid.add(eventsTable, 0, 0);
        grid.add(rightColumn, 1, 0);

        // --- Main Layout ---
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(navBar);
        mainLayout.setCenter(grid);
        mainLayout.setLeft(header);

        Scene scene = new Scene(mainLayout, 900, 600);
        stage.setTitle("Organizer Dashboard");
        stage.setScene(scene);
        stage.show();

        // --- Button Actions ---
        createEventBtn.setOnAction(e -> {
            CreateEventPage page = new CreateEventPage();
            page.show(new Stage(), organizerID);
        });

        attendeesBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Event ID");
            dialog.setHeaderText("Enter Event ID");
            dialog.setContentText("Please enter the event ID:");
            dialog.showAndWait().ifPresent(eventID -> {
                try {
                    int id = Integer.parseInt(eventID);
                    ViewAttendeesPage page = new ViewAttendeesPage();
                    page.show(new Stage(), id);
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, "Invalid Event ID").showAndWait();
                }
            });
        });

        myEventsBtn.setOnAction(e -> {
            EventListPage myEventsPage = new EventListPage(true); // Show only my events
            myEventsPage.show(new Stage(), organizerID);
        });
        
        eventsBtn.setOnAction(e -> {
            EventListPage eventsPage = new EventListPage(false); // Show all events
            eventsPage.show(new Stage(), organizerID);
        });

        profileBtn.setOnAction(e -> {
            ProfilePage profilePage = new ProfilePage();
            profilePage.show(new Stage(), organizerID);
        });

        // TODO: notificationsBtn action
    }
    
    public static class Event {
        private int eventID;
        private String eventName;
        private String startDate;
        private String endDate;

        public Event(int eventID, String eventName, String startDate, String endDate) {
            this.eventID = eventID;
            this.eventName = eventName;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public int getEventID() { return eventID; }
        public String getEventName() { return eventName; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
    }

    public void show(Stage stage) {
        show(stage, 1);
    }
}
