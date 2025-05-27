package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CreateEventPage {

    private Stage stage;
    private int organizerID;

    // Event fields
    private TextField eventNameField = new TextField();
    private TextArea eventDescriptionArea = new TextArea();
    private DatePicker eventStartDatePicker = new DatePicker();
    private DatePicker eventEndDatePicker = new DatePicker();
    private TextField eventLocationField = new TextField();

    // Session count
    private Spinner<Integer> sessionCountSpinner = new Spinner<>(1, 10, 1);

    // Container to hold session forms dynamically
    private VBox sessionsContainer = new VBox(15);

    // Keep session inputs in a list for easy access
    private final List<SessionForm> sessionForms = new ArrayList<>();

    // Time formatter for parsing time inputs
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public void show(Stage stage, int organizerID) {
        this.stage = stage;
        this.organizerID = organizerID;
        showInitialForm();
    }

    private void showInitialForm() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        // Event fields
        grid.add(new Label("Event Name:"), 0, 0);
        grid.add(eventNameField, 1, 0);

        grid.add(new Label("Description:"), 0, 1);
        eventDescriptionArea.setPrefRowCount(3);
        grid.add(eventDescriptionArea, 1, 1);

        grid.add(new Label("Start Date:"), 0, 2);
        grid.add(eventStartDatePicker, 1, 2);

        grid.add(new Label("End Date:"), 0, 3);
        grid.add(eventEndDatePicker, 1, 3);

        grid.add(new Label("Location:"), 0, 4);
        grid.add(eventLocationField, 1, 4);

        // Session count spinner
        grid.add(new Label("Number of Sessions:"), 0, 5);
        grid.add(sessionCountSpinner, 1, 5);

        Button generateButton = new Button("Add Session Details");
        generateButton.setOnAction(e -> generateSessionForms());

        VBox vbox = new VBox(15, grid, generateButton);
        vbox.setPadding(new Insets(20));

        stage.setScene(new Scene(vbox));
        stage.setTitle("Create Event - Step 1");
        stage.show();
    }

    private void generateSessionForms() {
        sessionsContainer.getChildren().clear();
        sessionForms.clear();

        int count = sessionCountSpinner.getValue();

        for (int i = 1; i <= count; i++) {
            SessionForm form = new SessionForm(i);
            sessionForms.add(form);
            sessionsContainer.getChildren().add(form.getPane());
        }

        Button submitButton = new Button("Create Event and Sessions");
        submitButton.setOnAction(e -> saveEventAndSessions());

        VBox mainBox = new VBox(15);
        mainBox.setPadding(new Insets(20));

        // Add event fields (reuse from above for continuity)
        GridPane eventGrid = new GridPane();
        eventGrid.setPadding(new Insets(20));
        eventGrid.setHgap(10);
        eventGrid.setVgap(10);

        eventGrid.add(new Label("Event Name:"), 0, 0);
        eventGrid.add(eventNameField, 1, 0);

        eventGrid.add(new Label("Description:"), 0, 1);
        eventGrid.add(eventDescriptionArea, 1, 1);

        eventGrid.add(new Label("Start Date:"), 0, 2);
        eventGrid.add(eventStartDatePicker, 1, 2);

        eventGrid.add(new Label("End Date:"), 0, 3);
        eventGrid.add(eventEndDatePicker, 1, 3);

        eventGrid.add(new Label("Location:"), 0, 4);
        eventGrid.add(eventLocationField, 1, 4);

        mainBox.getChildren().addAll(
                new Label("Event Details:"), eventGrid,
                new Separator(),
                new Label("Session Details:"), sessionsContainer,
                submitButton
        );

        stage.setScene(new Scene(new ScrollPane(mainBox), 700, 600));
        stage.setTitle("Create Event with Sessions - Step 2");
        stage.show();
    }

    private void saveEventAndSessions() {
        // Validate event data
        String eventName = eventNameField.getText();
        String eventDescription = eventDescriptionArea.getText();
        LocalDate eventStartDate = eventStartDatePicker.getValue();
        LocalDate eventEndDate = eventEndDatePicker.getValue();
        String eventLocation = eventLocationField.getText();

        if (eventName.isEmpty() || eventDescription.isEmpty() || eventStartDate == null || eventEndDate == null || eventLocation.isEmpty()) {
            showAlert("Validation Error", "Please fill all event fields.");
            return;
        }

        // Validate sessions
        for (SessionForm sf : sessionForms) {
            if (!sf.isValid()) {
                showAlert("Validation Error", "Please fill all fields for Session #" + sf.getSessionNumber());
                return;
            }
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Insert event
            String eventSQL = "INSERT INTO Event (name, description, startDate, endDate, location, organizerID) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement eventStmt = conn.prepareStatement(eventSQL, Statement.RETURN_GENERATED_KEYS);
            eventStmt.setString(1, eventName);
            eventStmt.setString(2, eventDescription);
            eventStmt.setDate(3, Date.valueOf(eventStartDate));
            eventStmt.setDate(4, Date.valueOf(eventEndDate));
            eventStmt.setString(5, eventLocation);
            eventStmt.setInt(6, organizerID);

            int affected = eventStmt.executeUpdate();
            if (affected == 0) {
                conn.rollback();
                showAlert("Database Error", "Failed to insert event.");
                return;
            }

            ResultSet keys = eventStmt.getGeneratedKeys();
            if (!keys.next()) {
                conn.rollback();
                showAlert("Database Error", "Failed to get event ID.");
                return;
            }
            int eventID = keys.getInt(1);

            // Insert sessions
            String sessionSQL = "INSERT INTO Session (eventID, title, description, location, startTime, endTime, speaker_name) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement sessionStmt = conn.prepareStatement(sessionSQL);

            for (SessionForm sf : sessionForms) {
                sessionStmt.setInt(1, eventID);
                sessionStmt.setString(2, sf.getTitle());
                sessionStmt.setString(3, sf.getDescription());
                sessionStmt.setString(4, sf.getLocation());

                // Parse times
                Time startTime = Time.valueOf(LocalTime.parse(sf.getStartTime(), timeFormatter));
                Time endTime = Time.valueOf(LocalTime.parse(sf.getEndTime(), timeFormatter));

                sessionStmt.setTime(5, startTime);
                sessionStmt.setTime(6, endTime);
                sessionStmt.setString(7, sf.getSpeakerName());

                sessionStmt.addBatch();
            }

            sessionStmt.executeBatch();

	         // Insert notification
	         String notifSQL = "INSERT INTO Notification (name, title, message, createdAt, isRead, notificationType, userID) " +
	                           "VALUES (?, ?, ?, GETDATE(), 0, ?, ?)";
	         PreparedStatement notifStmt = conn.prepareStatement(notifSQL);
	         notifStmt.setString(1, "Event Created");
	         notifStmt.setString(2, "New Event: " + eventName);
	         notifStmt.setString(3, "You have successfully created the event \"" + eventName + "\" with " + sessionForms.size() + " sessions.");
	         notifStmt.setString(4, "Organizer");
	         notifStmt.setInt(5, organizerID);
	         notifStmt.executeUpdate();
	
	         conn.commit();
	
	         showAlert("Success", "Event and sessions created successfully!");
	         stage.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Database Error", "Error saving event and sessions: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Invalid time format. Please use HH:mm.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class to hold session form controls for each session
    private static class SessionForm {
        private final int sessionNumber;
        private final TextField titleField = new TextField();
        private final TextArea descriptionArea = new TextArea();
        private final TextField locationField = new TextField();
        private final TextField speakerNameField = new TextField();
        private final TextField startTimeField = new TextField();
        private final TextField endTimeField = new TextField();

        public SessionForm(int number) {
            this.sessionNumber = number;
            descriptionArea.setPrefRowCount(2);
            startTimeField.setPromptText("HH:mm");
            endTimeField.setPromptText("HH:mm");
        }

        public Pane getPane() {
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(5);
            grid.setPadding(new Insets(10));
            grid.setStyle("-fx-border-color: gray; -fx-border-radius: 5; -fx-border-width: 1;");

            grid.add(new Label("Session #" + sessionNumber + " Title:"), 0, 0);
            grid.add(titleField, 1, 0);

            grid.add(new Label("Description:"), 0, 1);
            grid.add(descriptionArea, 1, 1);

            grid.add(new Label("Location:"), 0, 2);
            grid.add(locationField, 1, 2);

            grid.add(new Label("Speaker Name:"), 0, 3);
            grid.add(speakerNameField, 1, 3);

            grid.add(new Label("Start Time (HH:mm):"), 0, 4);
            grid.add(startTimeField, 1, 4);

            grid.add(new Label("End Time (HH:mm):"), 0, 5);
            grid.add(endTimeField, 1, 5);

            return grid;
        }

        public boolean isValid() {
            return !titleField.getText().isEmpty() &&
                   !descriptionArea.getText().isEmpty() &&
                   !locationField.getText().isEmpty() &&
                   !speakerNameField.getText().isEmpty() &&
                   !startTimeField.getText().isEmpty() &&
                   !endTimeField.getText().isEmpty();
        }

        public int getSessionNumber() {
            return sessionNumber;
        }

        public String getTitle() {
            return titleField.getText();
        }

        public String getDescription() {
            return descriptionArea.getText();
        }

        public String getLocation() {
            return locationField.getText();
        }

        public String getSpeakerName() {
            return speakerNameField.getText();
        }

        public String getStartTime() {
            return startTimeField.getText();
        }

        public String getEndTime() {
            return endTimeField.getText();
        }
    }
}
