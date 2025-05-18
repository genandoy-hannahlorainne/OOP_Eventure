package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

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

        eventNameField.setPromptText("Event Name");
        eventDescriptionField.setPromptText("Event Description");
        locationField.setPromptText("Event Location");
        eventDescriptionField.setPrefRowCount(4);

        Button createEventButton = new Button("Create Event");
        createEventButton.setOnAction(e -> {
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

            boolean success = createEventWithNotifications(
                    organizerID,
                    eventNameField.getText(),
                    eventDescriptionField.getText(),
                    startDatePicker.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    endDatePicker.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    locationField.getText()
            );

            if (success) {
                showAlert("Success", "Event Created", "Your event has been created successfully.");
                stage.close();
            }
        });

        VBox layout = new VBox(15,
                title,
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
    
    private boolean createEventWithNotifications(int organizerID, String name, String description,
    											String startDate, String endDate, String location) {
		try (Connection conn = DBConnection.getConnection()) {
			conn.setAutoCommit(false); // start transaction
			
			// Insert the event
			String sqlInsertEvent = "INSERT INTO [EventManagementSystem].[dbo].[Event] " +
							"(name, description, startDate, endDate, location, organizerID) VALUES (?, ?, ?, ?, ?, ?)";
			PreparedStatement stmtEvent = conn.prepareStatement(sqlInsertEvent, PreparedStatement.RETURN_GENERATED_KEYS);
			stmtEvent.setString(1, name);
			stmtEvent.setString(2, description);
			stmtEvent.setString(3, startDate);
			stmtEvent.setString(4, endDate);
			stmtEvent.setString(5, location);
			stmtEvent.setInt(6, organizerID);
			
			int rowsInserted = stmtEvent.executeUpdate();
			if (rowsInserted == 0) {
				conn.rollback();
				showAlert("Error", "Database Error", "Failed to create event.");
				return false;
			}
			
			ResultSet generatedKeys = stmtEvent.getGeneratedKeys();
			int eventID = -1;
			if (generatedKeys.next()) {
				eventID = generatedKeys.getInt(1);
			} else {
				conn.rollback();
				showAlert("Error", "Database Error", "Failed to retrieve event ID.");
				return false;
			}
			
			// Insert notification for organizer
			String sqlNotifOrganizer = "INSERT INTO [EventManagementSystem].[dbo].[Notification] " +
					"(name, title, message, createdAt, isRead, notificationType, userID) " +
					"VALUES (?, ?, ?, GETDATE(), 0, ?, ?)";
			PreparedStatement notifOrgStmt = conn.prepareStatement(sqlNotifOrganizer);
			notifOrgStmt.setString(1, "Event Created");
			notifOrgStmt.setString(2, "New Event Created");
			notifOrgStmt.setString(3, "Your event '" + name + "' has been successfully created.");
			notifOrgStmt.setString(4, "Organizer");
			notifOrgStmt.setInt(5, organizerID);
			notifOrgStmt.executeUpdate();
			
			// Notify all registered attendees of this event (only if any exist)
			String sqlAttendees = "SELECT userID FROM [EventManagementSystem].[dbo].[Registration] " +
					"WHERE eventID = ? AND registrationStatus = 'Confirmed'";
			PreparedStatement attendeesStmt = conn.prepareStatement(sqlAttendees);
			attendeesStmt.setInt(1, eventID);
			ResultSet rsAttendees = attendeesStmt.executeQuery();
			
			String sqlNotifAttendee = "INSERT INTO [EventManagementSystem].[dbo].[Notification] " +
					"(name, title, message, createdAt, isRead, notificationType, userID) " +
					"VALUES (?, ?, ?, GETDATE(), 0, ?, ?)";
			PreparedStatement notifAttStmt = conn.prepareStatement(sqlNotifAttendee);
			
			while (rsAttendees.next()) {
				int attendeeID = rsAttendees.getInt("userID");
				notifAttStmt.setString(1, "Event Update");
				notifAttStmt.setString(2, "New Event Available");
				notifAttStmt.setString(3, "A new event '" + name + "' is now available.");
				notifAttStmt.setString(4, "Attendee");
				notifAttStmt.setInt(5, attendeeID);
				notifAttStmt.addBatch();
			}
			notifAttStmt.executeBatch();
			
			conn.commit(); // commit all changes
			
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			showAlert("Error", "Database Error", "Could not create event or notifications: " + e.getMessage());
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
