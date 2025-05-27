package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrganizerDashboard {
    private int organizerID;

    private TableView<Event> eventsTable;
    private ListView<String> notificationsList;
    private Label totalEventsLabel;

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
        Button switchAccountBtn = new Button("Switch Account");
        Button refreshBtn = new Button("Refresh");

        navBar.getChildren().addAll(eventsBtn, attendeesBtn, myEventsBtn, notificationsBtn, createEventBtn, profileBtn, switchAccountBtn, refreshBtn);

        // --- Welcome and User Info ---
        Label welcomeLabel = new Label("Welcome, Organizer #" + organizerID);
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox header = new VBox(5, welcomeLabel);
        header.setPadding(new Insets(10));

        // --- Events Table ---
        eventsTable = new TableView<>();

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

        // --- View Details Button ---
        Button viewDetailsBtn = new Button("View Details");
        viewDetailsBtn.setDisable(true);

        eventsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            viewDetailsBtn.setDisable(newSel == null);
        });

        viewDetailsBtn.setOnAction(e -> {
            Event selectedEvent = eventsTable.getSelectionModel().getSelectedItem();
            if (selectedEvent != null) {
                showEventDetails(selectedEvent);
            }
        });

        VBox eventsBox = new VBox(10, eventsTable, viewDetailsBtn);

        // --- Notifications Panel (only unread) ---
        notificationsList = new ListView<>();
        VBox notificationsBox = new VBox(10);
        notificationsBox.setPadding(new Insets(10));
        notificationsBox.setStyle("-fx-border-color: gray; -fx-border-width: 1;");
        Label notifLabel = new Label("Unread Notifications");
        notifLabel.setStyle("-fx-font-weight: bold;");
        notificationsBox.getChildren().addAll(notifLabel, notificationsList);

        // --- Analytics Panel ---
        totalEventsLabel = new Label();
        VBox analyticsBox = new VBox(10);
        analyticsBox.setPadding(new Insets(10));
        analyticsBox.setStyle("-fx-border-color: gray; -fx-border-width: 1;");
        Label analyticsLabel = new Label("Event Analytics");
        analyticsLabel.setStyle("-fx-font-weight: bold;");
        analyticsBox.getChildren().addAll(analyticsLabel, totalEventsLabel);

        VBox rightColumn = new VBox(20, notificationsBox, analyticsBox);
        rightColumn.setPrefWidth(300);

        // --- Grid Layout ---
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(20);
        grid.add(eventsBox, 0, 0);
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

        // --- Initial Data Load ---
        loadEvents();
        loadUnreadNotifications();

        // --- Button Actions ---
        refreshBtn.setOnAction(e -> {
            loadEvents();
            loadUnreadNotifications();
        });

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
                    page.show(new Stage(), id, true);
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, "Invalid Event ID").showAndWait();
                }
            });
        });

        myEventsBtn.setOnAction(e -> {
            OrganizerEventsPage page = new OrganizerEventsPage();
            page.show(new Stage(), organizerID);
        });
        eventsBtn.setOnAction(e -> {
            EventListPage eventsPage = new EventListPage(false);
            eventsPage.show(new Stage(), organizerID);
        });

        profileBtn.setOnAction(e -> {
            ProfilePage profilePage = new ProfilePage();
            profilePage.show(new Stage(), organizerID);
        });

        notificationsBtn.setOnAction(e -> {
            // Open notification page to see both read and unread, with filter
            OrganizerNotificationPage notifPage = new OrganizerNotificationPage();
            notifPage.show(new Stage(), organizerID);
        });

        switchAccountBtn.setOnAction(e -> {
            stage.close();
            Stage loginStage = new Stage();
            new LoginPage(loginStage);
        });
    }

    private void loadEvents() {
        eventsTable.getItems().clear();

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

        totalEventsLabel.setText("Total Events: " + eventsTable.getItems().size());
    }

    private void loadUnreadNotifications() {
        notificationsList.getItems().clear();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT title, message, createdAt
                FROM Notification
                WHERE userID = ? AND notificationType = 'Organizer' AND isRead = 0
                ORDER BY createdAt DESC
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, organizerID);
            ResultSet rs = stmt.executeQuery();

            boolean hasNotif = false;
            while (rs.next()) {
                hasNotif = true;
                String title = rs.getString("title");
                String message = rs.getString("message");
                String timestamp = rs.getString("createdAt");

                notificationsList.getItems().add("[" + timestamp + "] " + title + ": " + message);
            }

            if (!hasNotif) {
                notificationsList.getItems().add("No new notifications.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            notificationsList.getItems().add("Error loading notifications.");
        }
    }

    private void showEventDetails(Event event) {
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Event Details - " + event.getEventName());

        Label idLabel = new Label("Event ID: " + event.getEventID());
        Label nameLabel = new Label("Name: " + event.getEventName());
        Label startLabel = new Label("Start Date: " + event.getStartDate());
        Label endLabel = new Label("End Date: " + event.getEndDate());

        VBox layout = new VBox(10, idLabel, nameLabel, startLabel, endLabel);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(layout, 300, 200);
        detailsStage.setScene(scene);
        detailsStage.show();
    }
}
