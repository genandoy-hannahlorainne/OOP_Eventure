package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewEventsPage {

    public void show(Stage stage, int organizerID) {
        Label title = new Label("Your Events");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<Event> eventsTable = new TableView<>();

        TableColumn<Event, Integer> idColumn = new TableColumn<>("Event ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("eventID"));

        TableColumn<Event, String> nameColumn = new TableColumn<>("Event Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("eventName"));

        TableColumn<Event, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("eventDate"));

        eventsTable.getColumns().addAll(idColumn, nameColumn, dateColumn);
        idColumn.setPrefWidth(100);
        nameColumn.setPrefWidth(200);
        dateColumn.setPrefWidth(150);

        // Fetch events from database
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT eventID, eventName, eventDate FROM Event WHERE organizerID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, organizerID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int eventID = rs.getInt("eventID");
                String eventName = rs.getString("eventName");
                String eventDate = rs.getString("eventDate");
                eventsTable.getItems().add(new Event(eventID, eventName, eventDate));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Failed to load events");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }

        VBox layout = new VBox(15, title, eventsTable);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(layout, 500, 400);
        stage.setTitle("View Events");
        stage.setScene(scene);
        stage.show();
    }

    // Event class
    public static class Event {
        private int eventID;
        private String eventName;
        private String eventDate;

        public Event(int eventID, String eventName, String eventDate) {
            this.eventID = eventID;
            this.eventName = eventName;
            this.eventDate = eventDate;
        }

        public int getEventID() {
            return eventID;
        }

        public String getEventName() {
            return eventName;
        }

        public String getEventDate() {
            return eventDate;
        }
    }
}