package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class EventListPage {

    private boolean isMyEvents;

    public EventListPage(boolean isMyEvents) {
        this.isMyEvents = isMyEvents;
    }

    public void show(Stage stage, int organizerID) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label header = new Label(isMyEvents ? "My Events" : "All Events");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton upcomingBtn = new RadioButton("Upcoming Events");
        RadioButton pastBtn = new RadioButton("Past Events");
        upcomingBtn.setToggleGroup(toggleGroup);
        pastBtn.setToggleGroup(toggleGroup);
        upcomingBtn.setSelected(true);

        HBox toggleBox = new HBox(10, upcomingBtn, pastBtn);

        TableView<Event> eventTable = new TableView<>();
        eventTable.setPrefWidth(600);

        TableColumn<Event, Integer> idCol = new TableColumn<>("Event ID");
        idCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("eventID"));

        TableColumn<Event, String> nameCol = new TableColumn<>("Event Name");
        nameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("eventName"));

        TableColumn<Event, String> startCol = new TableColumn<>("Start Date");
        startCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("startDate"));

        TableColumn<Event, String> endCol = new TableColumn<>("End Date");
        endCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("endDate"));

        eventTable.getColumns().addAll(idCol, nameCol, startCol, endCol);

        // Load data
        loadEvents(eventTable, organizerID, true);

        // Switch between upcoming and past events
        upcomingBtn.setOnAction(e -> loadEvents(eventTable, organizerID, true));
        pastBtn.setOnAction(e -> loadEvents(eventTable, organizerID, false));

        layout.getChildren().addAll(header, toggleBox, eventTable);
        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.setTitle(isMyEvents ? "My Events" : "All Events");
        stage.show();
    }

    private void loadEvents(TableView<Event> table, int organizerID, boolean upcoming) {
        table.getItems().clear();
        String sql = "SELECT eventID, name, startDate, endDate FROM Event WHERE ";

        if (isMyEvents) {
            sql += "organizerID = ? AND ";
        }

        if (upcoming) {
            sql += "startDate >= GETDATE()";
        } else {
            sql += "startDate < GETDATE()";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (isMyEvents) {
                stmt.setInt(1, organizerID);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                table.getItems().add(new Event(
                        rs.getInt("eventID"),
                        rs.getString("name"),
                        rs.getString("startDate"),
                        rs.getString("endDate")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Reuse Event class here or import if in OrganizerDashboard
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
}
