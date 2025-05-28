package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EditEventPage {

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void show(Stage owner, MyEventsPage.Event event, Runnable onUpdate) {
        Stage stage = new Stage();
        stage.setTitle("Edit Event - " + event.getName());

        TextField nameField = new TextField(event.getName());
        DatePicker startDatePicker = new DatePicker(LocalDate.parse(event.getStartDate()));
        DatePicker endDatePicker = new DatePicker(LocalDate.parse(event.getEndDate()));

        // Container for sessions
        VBox sessionBox = new VBox(10);
        List<SessionRow> sessionRows = new ArrayList<>();

        // Load sessions with all details
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT sessionID, title, startTime, endTime, location, description FROM Session WHERE eventID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, event.getEventID());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int sessionID = rs.getInt("sessionID");
                String title = rs.getString("title");
                String startTime = rs.getString("startTime");  // assuming stored as datetime string
                String endTime = rs.getString("endTime");
                String location = rs.getString("location");
                String description = rs.getString("description");

                SessionRow row = new SessionRow(sessionID, title, startTime, endTime, location, description);
                sessionBox.getChildren().add(row.getLayout());
                sessionRows.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Add session button
        Button addSessionBtn = new Button("Add New Session");
        addSessionBtn.setOnAction(e -> {
            SessionRow newRow = new SessionRow(-1, "", "", "", "", "");
            sessionBox.getChildren().add(newRow.getLayout());
            sessionRows.add(newRow);
        });

        Button saveBtn = new Button("Save All Changes");
        saveBtn.setOnAction(e -> {
            String newName = nameField.getText();
            LocalDate newStartDate = startDatePicker.getValue();
            LocalDate newEndDate = endDatePicker.getValue();

            if (newName.isBlank()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Event name cannot be empty.");
                return;
            }
            if (newStartDate == null || newEndDate == null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Start and End date must be selected.");
                return;
            }
            if (newEndDate.isBefore(newStartDate)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "End date cannot be before start date.");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);

                // Update event info
                String updateEvent = "UPDATE Event SET name = ?, startDate = ?, endDate = ? WHERE eventID = ?";
                PreparedStatement stmt = conn.prepareStatement(updateEvent);
                stmt.setString(1, newName);
                stmt.setString(2, newStartDate.toString());
                stmt.setString(3, newEndDate.toString());
                stmt.setInt(4, event.getEventID());
                stmt.executeUpdate();

                // Update/Insert sessions
                for (SessionRow row : sessionRows) {
                    // Validate required session fields (e.g., title)
                    if (row.getTitle().isBlank()) continue;

                    if (row.getSessionID() == -1) {
                        // New session insert
                        String insertSql = "INSERT INTO Session (title, startTime, endTime, location, description, eventID) VALUES (?, ?, ?, ?, ?, ?)";
                        PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                        insertStmt.setString(1, row.getTitle());
                        insertStmt.setString(2, row.getStartTime());
                        insertStmt.setString(3, row.getEndTime());
                        insertStmt.setString(4, row.getLocation());
                        insertStmt.setString(5, row.getDescription());
                        insertStmt.setInt(6, event.getEventID());
                        insertStmt.executeUpdate();

                    } else {
                        // Update existing session
                        String updateSql = "UPDATE Session SET title = ?, startTime = ?, endTime = ?, location = ?, description = ? WHERE sessionID = ?";
                        PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                        updateStmt.setString(1, row.getTitle());
                        updateStmt.setString(2, row.getStartTime());
                        updateStmt.setString(3, row.getEndTime());
                        updateStmt.setString(4, row.getLocation());
                        updateStmt.setString(5, row.getDescription());
                        updateStmt.setInt(6, row.getSessionID());
                        updateStmt.executeUpdate();
                    }
                }

                conn.commit();
                stage.close();
                if (onUpdate != null) onUpdate.run();

            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save changes: " + ex.getMessage());
            }
        });

        VBox layout = new VBox(10,
                new Label("Event Name:"), nameField,
                new Label("Start Date:"), startDatePicker,
                new Label("End Date:"), endDatePicker,
                new Label("Sessions:"), sessionBox, addSessionBtn, saveBtn
        );
        layout.setPadding(new Insets(15));

        Scene scene = new Scene(layout, 600, 600);
        stage.setScene(scene);
        stage.initOwner(owner);
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static class SessionRow {
        private final int sessionID;

        private final TextField titleField;
        private final TextField startTimeField;   // e.g. "2025-05-19 14:00"
        private final TextField endTimeField;
        private final TextField locationField;
        private final TextArea descriptionArea;

        public SessionRow(int sessionID, String title, String startTime, String endTime, String location, String description) {
            this.sessionID = sessionID;
            this.titleField = new TextField(title);
            this.startTimeField = new TextField(startTime);
            this.endTimeField = new TextField(endTime);
            this.locationField = new TextField(location);
            this.descriptionArea = new TextArea(description);
            descriptionArea.setPrefRowCount(2);
            descriptionArea.setWrapText(true);
        }

        public int getSessionID() {
            return sessionID;
        }

        public String getTitle() {
            return titleField.getText().trim();
        }

        public String getStartTime() {
            return startTimeField.getText().trim();
        }

        public String getEndTime() {
            return endTimeField.getText().trim();
        }

        public String getLocation() {
            return locationField.getText().trim();
        }

        public String getDescription() {
            return descriptionArea.getText().trim();
        }

        public VBox getLayout() {
            VBox box = new VBox(5);
            box.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: 5;");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(5);

            grid.add(new Label("Title:"), 0, 0);
            grid.add(titleField, 1, 0);

            grid.add(new Label("Start Time (yyyy-MM-dd HH:mm):"), 0, 1);
            grid.add(startTimeField, 1, 1);

            grid.add(new Label("End Time (yyyy-MM-dd HH:mm):"), 0, 2);
            grid.add(endTimeField, 1, 2);

            grid.add(new Label("Location:"), 0, 3);
            grid.add(locationField, 1, 3);

            grid.add(new Label("Description:"), 0, 4);
            grid.add(descriptionArea, 1, 4);

            box.getChildren().add(grid);
            return box;
        }
    }
}
