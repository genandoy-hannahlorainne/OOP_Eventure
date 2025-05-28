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

        TableColumn<Event, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Event, String> startDateColumn = new TableColumn<>("Start Date");
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));

        TableColumn<Event, String> endDateColumn = new TableColumn<>("End Date");
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        TableColumn<Event, String> locationColumn = new TableColumn<>("Location");
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));

        eventsTable.getColumns().addAll(idColumn, nameColumn, startDateColumn, endDateColumn, locationColumn);

        idColumn.setPrefWidth(80);
        nameColumn.setPrefWidth(150);
        startDateColumn.setPrefWidth(100);
        endDateColumn.setPrefWidth(100);
        locationColumn.setPrefWidth(150);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT eventID, name, startDate, endDate, location FROM Event WHERE organizerID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, organizerID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int eventID = rs.getInt("eventID");
                String name = rs.getString("name");
                String startDate = rs.getString("startDate");
                String endDate = rs.getString("endDate");
                String location = rs.getString("location");

                eventsTable.getItems().add(new Event(eventID, name, startDate, endDate, location));
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

        Scene scene = new Scene(layout, 650, 400);
        stage.setTitle("View Events");
        stage.setScene(scene);
        stage.show();
    }

    // ✅ Updated Event class
    public static class Event {
        private final int eventID;
        private final String name;
        private final String startDate;
        private final String endDate;
        private final String location;

        public Event(int eventID, String name, String startDate, String endDate, String location) {
            this.eventID = eventID;
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
            this.location = location;
        }

        public int getEventID() {
            return eventID;
        }

        public String getName() {
            return name;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public String getLocation() {
            return location;
        }
    }
    
 
}
