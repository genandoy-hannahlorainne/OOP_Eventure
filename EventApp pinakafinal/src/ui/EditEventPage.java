package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EditEventPage {
    // UI color constants matching MyEventsPage
    private static final String BLUE_COLOR = "#97A9D1";
    private static final String YELLOW_COLOR = "#F1D747";
    private static final String WHITE_COLOR = "#FFFFFF";
    private static final String DARK_TEXT = "#333333";
    private static final String SUCCESS_COLOR = "#4CAF50";
    private static final String DANGER_COLOR = "#F44336";
    
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public void show(Stage owner, MyEventsPage.Event event, Runnable onUpdate) {
        Stage stage = new Stage();
        stage.setTitle("Eventure - Edit Event");
        
        // Main layout - simplified without navigation
        StackPane contentWrapper = new StackPane();
        contentWrapper.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        contentWrapper.setPadding(new Insets(20));
        
        VBox contentPane = new VBox(25);
        contentPane.setPadding(new Insets(30));
        contentPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                            "-fx-background-radius: 15px;");
        
        // Page Header
        VBox headerSection = createHeaderSection(event.getName());
        
        // Form Section
        VBox formSection = createFormSection(event, onUpdate, stage);
        
        contentPane.getChildren().addAll(headerSection, formSection);
        contentWrapper.getChildren().add(contentPane);
        
        Scene scene = new Scene(contentWrapper, 1200, 800);
        stage.setScene(scene);
        stage.initOwner(owner);
        stage.show();
    }
    
    private VBox createHeaderSection(String eventName) {
        VBox headerSection = new VBox(10);
        
        // Main title
        Label titleLabel = new Label("Edit Event");
        titleLabel.setStyle("-fx-font-size: 28px;" +
                           "-fx-font-weight: bold;" +
                           "-fx-text-fill: " + DARK_TEXT + ";");
        
        // Event name subtitle
        Label eventLabel = new Label("Editing: " + eventName);
        eventLabel.setStyle("-fx-font-size: 16px;" +
                           "-fx-text-fill: #666666;" +
                           "-fx-padding: 0 0 5px 0;");
        
        headerSection.getChildren().addAll(titleLabel, eventLabel);
        return headerSection;
    }
    
    private VBox createFormSection(MyEventsPage.Event event, Runnable onUpdate, Stage stage) {
        VBox formSection = new VBox(20);
        
        // Main form container with blue background
        VBox formContainer = new VBox(20);
        formContainer.setPadding(new Insets(25));
        formContainer.setStyle("-fx-background-color: " + BLUE_COLOR + ";" +
                              "-fx-background-radius: 12px;");
        
        // Event Details Section
        VBox eventDetailsSection = createEventDetailsSection(event);
        
        // Sessions Section
        VBox sessionsSection = createSessionsSection(event);
        
        // Action Buttons
        HBox buttonSection = createButtonSection(event, onUpdate, stage, eventDetailsSection, sessionsSection);
        
        formContainer.getChildren().addAll(eventDetailsSection, sessionsSection, buttonSection);
        formSection.getChildren().add(formContainer);
        
        return formSection;
    }
    
    private VBox createEventDetailsSection(MyEventsPage.Event event) {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                        "-fx-background-radius: 10px;");
        
        Label sectionTitle = new Label("Event Information");
        sectionTitle.setStyle("-fx-font-size: 18px;" +
                             "-fx-font-weight: bold;" +
                             "-fx-text-fill: " + DARK_TEXT + ";");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 0, 0));
        
        // Event Name
        Label nameLabel = new Label("Event Name:");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        TextField nameField = new TextField(event.getName());
        nameField.setStyle("-fx-padding: 8px; -fx-font-size: 14px; -fx-background-radius: 5px;");
        nameField.setPrefWidth(300);
        
        // Start Date
        Label startLabel = new Label("Start Date:");
        startLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        DatePicker startDatePicker = new DatePicker(LocalDate.parse(event.getStartDate()));
        startDatePicker.setStyle("-fx-padding: 8px; -fx-font-size: 14px;");
        startDatePicker.setPrefWidth(300);
        
        // End Date
        Label endLabel = new Label("End Date:");
        endLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        DatePicker endDatePicker = new DatePicker(LocalDate.parse(event.getEndDate()));
        endDatePicker.setStyle("-fx-padding: 8px; -fx-font-size: 14px;");
        endDatePicker.setPrefWidth(300);
        
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(startLabel, 0, 1);
        grid.add(startDatePicker, 1, 1);
        grid.add(endLabel, 0, 2);
        grid.add(endDatePicker, 1, 2);
        
        // Store references for later use
        nameField.setId("nameField");
        startDatePicker.setId("startDatePicker");
        endDatePicker.setId("endDatePicker");
        
        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }
    
    private VBox createSessionsSection(MyEventsPage.Event event) {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                        "-fx-background-radius: 10px;");
        
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label sectionTitle = new Label("Event Sessions");
        sectionTitle.setStyle("-fx-font-size: 18px;" +
                             "-fx-font-weight: bold;" +
                             "-fx-text-fill: " + DARK_TEXT + ";");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addSessionBtn = new Button("+ Add New Session");
        addSessionBtn.setStyle(
            "-fx-background-color: " + SUCCESS_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 8px 15px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 6px;"
        );
        
        headerBox.getChildren().addAll(sectionTitle, spacer, addSessionBtn);
        
        // Container for sessions
        VBox sessionBox = new VBox(15);
        sessionBox.setId("sessionBox");
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
                String startTime = rs.getString("startTime");
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
        
        // Store reference for later use
        sessionBox.setUserData(sessionRows);
        
        addSessionBtn.setOnAction(e -> {
            SessionRow newRow = new SessionRow(-1, "", "", "", "", "");
            sessionBox.getChildren().add(newRow.getLayout());
            sessionRows.add(newRow);
        });
        
        ScrollPane scrollPane = new ScrollPane(sessionBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(300);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        section.getChildren().addAll(headerBox, scrollPane);
        return section;
    }
    
    @SuppressWarnings("unchecked")
    private HBox createButtonSection(MyEventsPage.Event event, Runnable onUpdate, Stage stage, 
                                   VBox eventDetailsSection, VBox sessionsSection) {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
            "-fx-background-color: #95A5A6;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 6px;"
        );
        cancelBtn.setOnAction(e -> stage.close());
        
        Button saveBtn = new Button("Save All Changes");
        saveBtn.setStyle(
            "-fx-background-color: " + SUCCESS_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 6px;"
        );
        
        saveBtn.setOnAction(e -> {
            // Get form data
            TextField nameField = (TextField) eventDetailsSection.lookup("#nameField");
            DatePicker startDatePicker = (DatePicker) eventDetailsSection.lookup("#startDatePicker");
            DatePicker endDatePicker = (DatePicker) eventDetailsSection.lookup("#endDatePicker");
            VBox sessionBox = (VBox) sessionsSection.lookup("#sessionBox");
            List<SessionRow> sessionRows = (List<SessionRow>) sessionBox.getUserData();
            
            String newName = nameField.getText();
            LocalDate newStartDate = startDatePicker.getValue();
            LocalDate newEndDate = endDatePicker.getValue();
            
            if (newName.isBlank()) {
                showStyledAlert(Alert.AlertType.ERROR, "Validation Error", "Event name cannot be empty.");
                return;
            }
            if (newStartDate == null || newEndDate == null) {
                showStyledAlert(Alert.AlertType.ERROR, "Validation Error", "Start and End date must be selected.");
                return;
            }
            if (newEndDate.isBefore(newStartDate)) {
                showStyledAlert(Alert.AlertType.ERROR, "Validation Error", "End date cannot be before start date.");
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
                
                showStyledAlert(Alert.AlertType.INFORMATION, "Success", "Event updated successfully!");
                stage.close();
                if (onUpdate != null) onUpdate.run();
                
            } catch (SQLException ex) {
                ex.printStackTrace();
                showStyledAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save changes: " + ex.getMessage());
            }
        });
        
        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        return buttonBox;
    }
    
    private void showStyledAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // Style the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";");
        
        alert.showAndWait();
    }
    
    private static class SessionRow {
        private final int sessionID;
        private final TextField titleField;
        private final TextField startTimeField;
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
            
            // Style the fields
            String fieldStyle = "-fx-padding: 8px; -fx-font-size: 12px; -fx-background-radius: 5px;";
            titleField.setStyle(fieldStyle);
            startTimeField.setStyle(fieldStyle);
            endTimeField.setStyle(fieldStyle);
            locationField.setStyle(fieldStyle);
            descriptionArea.setStyle(fieldStyle);
            
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
            VBox box = new VBox(10);
            box.setPadding(new Insets(15));
            box.setStyle("-fx-border-color: #E0E0E0;" +
                        "-fx-border-width: 1;" +
                        "-fx-background-color: #F8F9FA;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-radius: 8px;");
            
            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(10);
            
            // Labels styling
            String labelStyle = "-fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + "; -fx-font-size: 12px;";
            
            Label titleLabel = new Label("Session Title:");
            titleLabel.setStyle(labelStyle);
            grid.add(titleLabel, 0, 0);
            grid.add(titleField, 1, 0);
            
            Label startLabel = new Label("Start Time (yyyy-MM-dd HH:mm):");
            startLabel.setStyle(labelStyle);
            grid.add(startLabel, 0, 1);
            grid.add(startTimeField, 1, 1);
            
            Label endLabel = new Label("End Time (yyyy-MM-dd HH:mm):");
            endLabel.setStyle(labelStyle);
            grid.add(endLabel, 0, 2);
            grid.add(endTimeField, 1, 2);
            
            Label locationLabel = new Label("Location:");
            locationLabel.setStyle(labelStyle);
            grid.add(locationLabel, 0, 3);
            grid.add(locationField, 1, 3);
            
            Label descLabel = new Label("Description:");
            descLabel.setStyle(labelStyle);
            grid.add(descLabel, 0, 4);
            grid.add(descriptionArea, 1, 4);
            
            box.getChildren().add(grid);
            return box;
        }
    }
}