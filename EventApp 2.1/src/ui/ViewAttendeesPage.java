package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewAttendeesPage {

    public void show(Stage stage, int eventID) {
        // Title
        Label title = new Label("Attendees for Event " + eventID);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Table for attendees
        TableView<Attendee> attendeesTable = new TableView<>();
        TableColumn<Attendee, String> nameColumn = new TableColumn<>("Name");
        TableColumn<Attendee, String> emailColumn = new TableColumn<>("Email");

        attendeesTable.getColumns().addAll(nameColumn, emailColumn);

        // Fetch attendees from the database
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT u.name, u.email FROM Registration r " +
                         "JOIN [User] u ON r.userID = u.userID WHERE r.eventID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, eventID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String email = rs.getString("email");
                attendeesTable.getItems().add(new Attendee(name, email));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Layout
        VBox layout = new VBox(15, title, attendeesTable);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(layout, 500, 400);
        stage.setTitle("View Attendees");
        stage.setScene(scene);
        stage.show();
    }

    // Attendee class to hold data for each attendee
    public static class Attendee {
        private String name;
        private String email;

        public Attendee(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }
}