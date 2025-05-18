package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class MyEventsPage {

    public void show(Stage stage, int attendeeID) {
        // TableView to display registered events
        TableView<Event> table = new TableView<>();

        TableColumn<Event, String> nameCol = new TableColumn<>("Event Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Event, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));

        TableColumn<Event, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        table.getColumns().addAll(nameCol, startDateCol, endDateCol);
        table.setPrefWidth(400);

        // Load events that the user has registered to
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT e.eventID, e.name, e.startDate, e.endDate
                FROM Event e
                JOIN Registration r ON e.eventID = r.eventID
                WHERE r.userID = ?
                """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, attendeeID);
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
            // Show alert if error happens
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load registered events: " + e.getMessage());
            alert.showAndWait();
        }

        Label title = new Label("My Registered Events (Attendee #" + attendeeID + ")");
        title.setStyle("-fx-font-size: 18px; -fx-padding: 10 0 20 0;");

        VBox layout = new VBox(10, title, table);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(layout, 450, 400);
        stage.setTitle("My Events");
        stage.setScene(scene);
        stage.show();
    }

    // Event model (similar to AttendeeDashboard.Event)
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