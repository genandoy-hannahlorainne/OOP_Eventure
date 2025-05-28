package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class EditSessionPage {
    public void show(Stage stage, int sessionID) {
        stage.setTitle("Edit Session");

        Label titleLabel = new Label("Title:");
        TextField titleField = new TextField();

        Label speakerLabel = new Label("Speaker:");
        TextField speakerField = new TextField();

        Label startLabel = new Label("Start Time:");
        TextField startField = new TextField();

        Label endLabel = new Label("End Time:");
        TextField endField = new TextField();

        Button updateBtn = new Button("Update Session");

        VBox layout = new VBox(10,
                titleLabel, titleField,
                speakerLabel, speakerField,
                startLabel, startField,
                endLabel, endField,
                updateBtn
        );
        layout.setPadding(new Insets(15));

        Scene scene = new Scene(layout, 400, 350);
        stage.setScene(scene);
        stage.show();

        // Load existing session details
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Session WHERE sessionID = ?");
            stmt.setInt(1, sessionID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                titleField.setText(rs.getString("title"));
                speakerField.setText(rs.getString("speaker"));
                startField.setText(rs.getString("startTime"));
                endField.setText(rs.getString("endTime"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Update session on button click
        updateBtn.setOnAction(e -> {
            String newTitle = titleField.getText();
            String newSpeaker = speakerField.getText();
            String newStart = startField.getText();
            String newEnd = endField.getText();

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE Session SET title = ?, speaker = ?, startTime = ?, endTime = ? WHERE sessionID = ?");
                stmt.setString(1, newTitle);
                stmt.setString(2, newSpeaker);
                stmt.setString(3, newStart);
                stmt.setString(4, newEnd);
                stmt.setInt(5, sessionID);
                stmt.executeUpdate();

                stage.close(); // Close after update
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }
}









package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;

public class EditSessionPage {
    // UI color constants matching EditEventPage
    private static final String BLUE_COLOR = "#97A9D1";
    private static final String WHITE_COLOR = "#FFFFFF";
    private static final String DARK_TEXT = "#333333";
    private static final String SUCCESS_COLOR = "#4CAF50";
    
    public void show(Stage owner, int sessionID, Runnable onUpdate) {
        Stage stage = new Stage();
        stage.setTitle("Eventure - Edit Session");
        
        // Main layout
        StackPane contentWrapper = new StackPane();
        contentWrapper.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        contentWrapper.setPadding(new Insets(20));
        
        VBox contentPane = new VBox(25);
        contentPane.setPadding(new Insets(30));
        contentPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                            "-fx-background-radius: 15px;");
        
        // Page Header
        VBox headerSection = createHeaderSection();
        
        // Form Section
        VBox formSection = createFormSection(sessionID, onUpdate, stage);
        
        contentPane.getChildren().addAll(headerSection, formSection);
        contentWrapper.getChildren().add(contentPane);
        
        Scene scene = new Scene(contentWrapper, 800, 600);
        stage.setScene(scene);
        stage.initOwner(owner);
        stage.show();
    }
    
    private VBox createHeaderSection() {
        VBox headerSection = new VBox(10);
        
        // Main title
        Label titleLabel = new Label("Edit Session");
        titleLabel.setStyle("-fx-font-size: 28px;" +
                           "-fx-font-weight: bold;" +
                           "-fx-text-fill: " + DARK_TEXT + ";");
        
        // Subtitle
        Label subtitleLabel = new Label("Modify session details");
        subtitleLabel.setStyle("-fx-font-size: 16px;" +
                              "-fx-text-fill: #666666;" +
                              "-fx-padding: 0 0 5px 0;");
        
        headerSection.getChildren().addAll(titleLabel, subtitleLabel);
        return headerSection;
    }
    
    private VBox createFormSection(int sessionID, Runnable onUpdate, Stage stage) {
        VBox formSection = new VBox(20);
        
        // Main form container with blue background
        VBox formContainer = new VBox(20);
        formContainer.setPadding(new Insets(25));
        formContainer.setStyle("-fx-background-color: " + BLUE_COLOR + ";" +
                              "-fx-background-radius: 12px;");
        
        // Session Details Section
        VBox sessionDetailsSection = createSessionDetailsSection();
        
        // Action Buttons
        HBox buttonSection = createButtonSection(sessionID, onUpdate, stage, sessionDetailsSection);
        
        formContainer.getChildren().addAll(sessionDetailsSection, buttonSection);
        formSection.getChildren().add(formContainer);
        
        // Load existing session data
        loadSessionData(sessionID, sessionDetailsSection);
        
        return formSection;
    }
    
    private VBox createSessionDetailsSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                        "-fx-background-radius: 10px;");
        
        Label sectionTitle = new Label("Session Information");
        sectionTitle.setStyle("-fx-font-size: 18px;" +
                             "-fx-font-weight: bold;" +
                             "-fx-text-fill: " + DARK_TEXT + ";");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 0, 0));
        
        // Session Title
        Label titleLabel = new Label("Session Title:");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        TextField titleField = new TextField();
        titleField.setStyle("-fx-padding: 8px; -fx-font-size: 14px; -fx-background-radius: 5px;");
        titleField.setPrefWidth(350);
        titleField.setId("titleField");
        
        // Speaker
        Label speakerLabel = new Label("Speaker:");
        speakerLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        TextField speakerField = new TextField();
        speakerField.setStyle("-fx-padding: 8px; -fx-font-size: 14px; -fx-background-radius: 5px;");
        speakerField.setPrefWidth(350);
        speakerField.setId("speakerField");
        
        // Start Time
        Label startLabel = new Label("Start Time (yyyy-MM-dd HH:mm):");
        startLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        TextField startField = new TextField();
        startField.setStyle("-fx-padding: 8px; -fx-font-size: 14px; -fx-background-radius: 5px;");
        startField.setPrefWidth(350);
        startField.setPromptText("e.g., 2024-12-25 14:30");
        startField.setId("startField");
        
        // End Time
        Label endLabel = new Label("End Time (yyyy-MM-dd HH:mm):");
        endLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        TextField endField = new TextField();
        endField.setStyle("-fx-padding: 8px; -fx-font-size: 14px; -fx-background-radius: 5px;");
        endField.setPrefWidth(350);
        endField.setPromptText("e.g., 2024-12-25 16:00");
        endField.setId("endField");
        
        // Location
        Label locationLabel = new Label("Location:");
        locationLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        TextField locationField = new TextField();
        locationField.setStyle("-fx-padding: 8px; -fx-font-size: 14px; -fx-background-radius: 5px;");
        locationField.setPrefWidth(350);
        locationField.setId("locationField");
        
        // Description
        Label descriptionLabel = new Label("Description:");
        descriptionLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setStyle("-fx-padding: 8px; -fx-font-size: 14px; -fx-background-radius: 5px;");
        descriptionArea.setPrefWidth(350);
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);
        descriptionArea.setId("descriptionArea");
        
        grid.add(titleLabel, 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(speakerLabel, 0, 1);
        grid.add(speakerField, 1, 1);
        grid.add(startLabel, 0, 2);
        grid.add(startField, 1, 2);
        grid.add(endLabel, 0, 3);
        grid.add(endField, 1, 3);
        grid.add(locationLabel, 0, 4);
        grid.add(locationField, 1, 4);
        grid.add(descriptionLabel, 0, 5);
        grid.add(descriptionArea, 1, 5);
        
        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }
    
    private HBox createButtonSection(int sessionID, Runnable onUpdate, Stage stage, VBox sessionDetailsSection) {
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
        
        Button saveBtn = new Button("Save Changes");
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
            TextField titleField = (TextField) sessionDetailsSection.lookup("#titleField");
            TextField speakerField = (TextField) sessionDetailsSection.lookup("#speakerField");
            TextField startField = (TextField) sessionDetailsSection.lookup("#startField");
            TextField endField = (TextField) sessionDetailsSection.lookup("#endField");
            TextField locationField = (TextField) sessionDetailsSection.lookup("#locationField");
            TextArea descriptionArea = (TextArea) sessionDetailsSection.lookup("#descriptionArea");
            
            String title = titleField.getText().trim();
            String speaker = speakerField.getText().trim();
            String startTime = startField.getText().trim();
            String endTime = endField.getText().trim();
            String location = locationField.getText().trim();
            String description = descriptionArea.getText().trim();
            
            // Validation
            if (title.isBlank()) {
                showStyledAlert(Alert.AlertType.ERROR, "Validation Error", "Session title cannot be empty.");
                return;
            }
            
            if (startTime.isBlank() || endTime.isBlank()) {
                showStyledAlert(Alert.AlertType.ERROR, "Validation Error", "Start and End time must be provided.");
                return;
            }
            
            // Update session in database
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "UPDATE Session SET title = ?, speaker = ?, startTime = ?, endTime = ?, location = ?, description = ? WHERE sessionID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, title);
                stmt.setString(2, speaker);
                stmt.setString(3, startTime);
                stmt.setString(4, endTime);
                stmt.setString(5, location);
                stmt.setString(6, description);
                stmt.setInt(7, sessionID);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    showStyledAlert(Alert.AlertType.INFORMATION, "Success", "Session updated successfully!");
                    stage.close();
                    if (onUpdate != null) onUpdate.run();
                } else {
                    showStyledAlert(Alert.AlertType.ERROR, "Error", "Failed to update session. Session may not exist.");
                }
                
            } catch (SQLException ex) {
                ex.printStackTrace();
                showStyledAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save changes: " + ex.getMessage());
            }
        });
        
        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        return buttonBox;
    }
    
    private void loadSessionData(int sessionID, VBox sessionDetailsSection) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT title, speaker, startTime, endTime, location, description FROM Session WHERE sessionID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, sessionID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                TextField titleField = (TextField) sessionDetailsSection.lookup("#titleField");
                TextField speakerField = (TextField) sessionDetailsSection.lookup("#speakerField");
                TextField startField = (TextField) sessionDetailsSection.lookup("#startField");
                TextField endField = (TextField) sessionDetailsSection.lookup("#endField");
                TextField locationField = (TextField) sessionDetailsSection.lookup("#locationField");
                TextArea descriptionArea = (TextArea) sessionDetailsSection.lookup("#descriptionArea");
                
                titleField.setText(rs.getString("title") != null ? rs.getString("title") : "");
                speakerField.setText(rs.getString("speaker") != null ? rs.getString("speaker") : "");
                startField.setText(rs.getString("startTime") != null ? rs.getString("startTime") : "");
                endField.setText(rs.getString("endTime") != null ? rs.getString("endTime") : "");
                locationField.setText(rs.getString("location") != null ? rs.getString("location") : "");
                descriptionArea.setText(rs.getString("description") != null ? rs.getString("description") : "");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showStyledAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load session data: " + e.getMessage());
        }
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
}