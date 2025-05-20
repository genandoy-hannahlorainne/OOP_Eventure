package ui;

import db.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CalendarPage {

    public void show(Stage stage, int attendeeID) {
        // List to hold registered events
        ListView<String> eventListView = new ListView<>();
        ObservableList<String> eventNames = FXCollections.observableArrayList();
        eventListView.setItems(eventNames);

        Label label = new Label("Your Registered Events:");
        DatePicker calendar = new DatePicker();
        calendar.setEditable(false);

        // Fetch registered events from DB
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT E.name, E.startDate
                FROM Event E
                JOIN Registration R ON E.eventID = R.eventID
                WHERE R.userID = ?
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, attendeeID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String date = rs.getString("startDate");
                eventNames.add(name + " - " + date); // Save both name and date in list item
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // When user selects an event, highlight the date in calendar
        eventListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.contains(" - ")) {
                String[] parts = newVal.split(" - ");
                if (parts.length == 2) {
                    try {
                        calendar.setValue(java.time.LocalDate.parse(parts[1]));
                    } catch (Exception e) {
                        System.err.println("Invalid date format: " + parts[1]);
                    }
                }
            }
        });

        VBox layout = new VBox(10, label, eventListView, new Label("Event Date:"), calendar);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 400, 500);
        stage.setScene(scene);
        stage.setTitle("Calendar");
        stage.show();
    }
}