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

public class EventDetailsPage {

    public void show(Stage stage, int eventID) {
        // Title
        Label title = new Label("Event Details");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Event details labels
        Label nameLabel = new Label("Event Name:");
        Label descriptionLabel = new Label("Event Description:");
        Label startDateLabel = new Label("Start Date:");
        Label endDateLabel = new Label("End Date:");
        Label locationLabel = new Label("Location:");

        // Fetch event details from the database
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Event WHERE eventID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, eventID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Display current event details
                nameLabel.setText("Event Name: " + rs.getString("name"));
                descriptionLabel.setText("Event Description: " + rs.getString("description"));
                startDateLabel.setText("Start Date: " + rs.getDate("startDate"));
                endDateLabel.setText("End Date: " + rs.getDate("endDate"));
                locationLabel.setText("Location: " + rs.getString("location"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Editable fields (Optional, if the organizer wants to update the event)
        TextField editName = new TextField();
        TextArea editDescription = new TextArea();
        DatePicker editStartDate = new DatePicker();
        DatePicker editEndDate = new DatePicker();
        TextField editLocation = new TextField();

        // Pre-fill the editable fields with the current event data
        editName.setPromptText("Event Name");
        editDescription.setPromptText("Event Description");
        editStartDate.setPromptText("Start Date");
        editEndDate.setPromptText("End Date");
        editLocation.setPromptText("Location");

        // Save Changes Button (For updating the event)
        Button saveChangesButton = new Button("Save Changes");
        saveChangesButton.setOnAction(e -> {
            updateEvent(eventID, editName.getText(), editDescription.getText(),
                    editStartDate.getValue().toString(), editEndDate.getValue().toString(),
                    editLocation.getText());
        });

        // Layout
        VBox layout = new VBox(15, title, nameLabel, descriptionLabel, startDateLabel, endDateLabel, locationLabel,
                editName, editDescription, editStartDate, editEndDate, editLocation, saveChangesButton);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(layout, 500, 500);
        stage.setTitle("Event Details");
        stage.setScene(scene);
        stage.show();
    }

    // Method to update event details in the database
    private void updateEvent(int eventID, String name, String description, String startDate, String endDate, String location) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE Event SET name = ?, description = ?, startDate = ?, endDate = ?, location = ? WHERE eventID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, startDate);
            stmt.setString(4, endDate);
            stmt.setString(5, location);
            stmt.setInt(6, eventID);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Event updated successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}