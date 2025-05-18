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
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CreateEventPage {

    public void show(Stage stage) {
        // Default method for backward compatibility
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Organizer ID");
        dialog.setHeaderText("Enter Organizer ID");
        dialog.setContentText("Please enter your organizer ID:");

        dialog.showAndWait().ifPresent(organizerID -> {
            try {
                int id = Integer.parseInt(organizerID);
                show(stage, id);
            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Invalid Input");
                alert.setContentText("Please enter a valid number for Organizer ID.");
                alert.showAndWait();
            }
        });
    }

    public void show(Stage stage, int organizerID) {
        // Title
        Label title = new Label("Create New Event");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Form for creating an event
        TextField eventNameField = new TextField();
        TextArea eventDescriptionField = new TextArea();
        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        DatePicker endDatePicker = new DatePicker(LocalDate.now().plusDays(1));
        TextField locationField = new TextField();

        // Placeholder for the fields
        eventNameField.setPromptText("Event Name");
        eventDescriptionField.setPromptText("Event Description");
        locationField.setPromptText("Event Location");
        
        // Set max height for description to avoid too tall forms
        eventDescriptionField.setPrefRowCount(4);

        // Create Event Button
        Button createEventButton = new Button("Create Event");
        createEventButton.setOnAction(e -> {
            // Validate input
            if (eventNameField.getText().trim().isEmpty()) {
                showAlert("Error", "Missing Information", "Event name is required.");
                return;
            }
            
            if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
                showAlert("Error", "Missing Information", "Both start and end dates are required.");
                return;
            }
            
            if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
                showAlert("Error", "Invalid Dates", "End date cannot be before start date.");
                return;
            }
            
            // Call create event method
            boolean success = createEvent(organizerID, eventNameField.getText(), 
                    eventDescriptionField.getText(),
                    startDatePicker.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE), 
                    endDatePicker.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    locationField.getText());
                    
            if (success) {
                showAlert("Success", "Event Created", "Your event has been created successfully.");
                stage.close(); // Close after creation
            }
        });

        // Layout
        VBox layout = new VBox(15, title, 
                new Label("Event Name:"), eventNameField, 
                new Label("Event Description:"), eventDescriptionField, 
                new Label("Start Date:"), startDatePicker, 
                new Label("End Date:"), endDatePicker, 
                new Label("Location:"), locationField, 
                createEventButton);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(layout, 500, 500);
        stage.setTitle("Create Event");
        stage.setScene(scene);
        stage.show();
    }

    // Method to create an event in the database
    private boolean createEvent(int organizerID, String name, String description, String startDate, String endDate, String location) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO Event (name, description, startDate, endDate, location, organizerID) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, startDate);
            stmt.setString(4, endDate);
            stmt.setString(5, location);
            stmt.setInt(6, organizerID);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database Error", "Could not create event: " + e.getMessage());
            return false;
        }
    }
    
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}