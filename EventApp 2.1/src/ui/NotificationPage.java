package ui;

import db.DBConnection;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class NotificationPage {
    public void show(Stage stage, int attendeeID) {
        Label header = new Label("Notifications for Attendee #" + attendeeID);
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ListView<String> notifList = new ListView<>();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT title, message, createdAt FROM Notification WHERE userID = ? ORDER BY createdAt DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, attendeeID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String title = rs.getString("title");
                String message = rs.getString("message");
                String createdAt = rs.getString("createdAt");

                String formatted = "[" + createdAt + "] " + title + ": " + message;
                notifList.getItems().add(formatted);
            }

            if (notifList.getItems().isEmpty()) {
                notifList.getItems().add("No notifications available.");
            }

        } catch (Exception e) {
            notifList.getItems().add("Error loading notifications.");
            e.printStackTrace();
        }

        VBox layout = new VBox(10, header, notifList);
        layout.setStyle("-fx-padding: 20;");

        Scene scene = new Scene(layout, 500, 400);
        stage.setTitle("Notifications");
        stage.setScene(scene);
        stage.show();
    }
}
