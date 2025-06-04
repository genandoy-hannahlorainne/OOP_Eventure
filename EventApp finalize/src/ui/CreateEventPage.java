package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CreateEventPage {
    // UI color constants matching organizer dashboard
    private static final String BLUE_COLOR = "#97A9D1";  // Nav bar and main background color
    private static final String YELLOW_COLOR = "#F1D747"; // Event cards and form backgrounds
    private static final String WHITE_COLOR = "#FFFFFF";
    private static final String DARK_TEXT = "#333333";
    
    private Stage stage;
    private int organizerID;
    private Consumer<Void> onBackToDashboard; // Callback function for navigation
    private TextField eventNameField = new TextField();
    private TextArea eventDescriptionArea = new TextArea();
    private DatePicker eventStartDatePicker = new DatePicker();
    private DatePicker eventEndDatePicker = new DatePicker();
    private TextField eventLocationField = new TextField();
    private Spinner<Integer> sessionCountSpinner = new Spinner<>(1, 10, 1);
    private VBox sessionsContainer = new VBox(15);
    private final List<SessionForm> sessionForms = new ArrayList<>();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    // Modified constructor to accept a callback for navigation
    public void show(Stage stage, int organizerID, Consumer<Void> onBackToDashboard) {
        this.stage = stage;
        this.organizerID = organizerID;
        this.onBackToDashboard = onBackToDashboard;
        showInitialForm();
    }
    
    // Keep the original method for backward compatibility
    public void show(Stage stage, int organizerID) {
        this.stage = stage;
        this.organizerID = organizerID;
        this.onBackToDashboard = null; // No callback provided
        showInitialForm();
    }
    
    private void showInitialForm() {
        // Main layout with navigation
        BorderPane mainLayout = new BorderPane();
        
        // --- Top Navigation Bar ---
        HBox navBar = createNavBar();
        mainLayout.setTop(navBar);
        
        // --- Main Content ---
        StackPane contentWrapper = new StackPane();
        contentWrapper.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        contentWrapper.setPadding(new Insets(15));
        
        VBox contentPane = new VBox(20);
        contentPane.setPadding(new Insets(25));
        contentPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                            "-fx-background-radius: 10px;");
        
        // Title Section
        Label titleLabel = new Label("Create New Event");
        titleLabel.setStyle("-fx-background-color: " + YELLOW_COLOR + ";" +
                           "-fx-padding: 12px 18px;" +
                           "-fx-background-radius: 8px;" +
                           "-fx-font-weight: bold;" +
                           "-fx-font-size: 16px;");
        
        // Form Section
        VBox formSection = new VBox(15);
        formSection.setStyle("-fx-background-color: " + BLUE_COLOR + ";" +
                            "-fx-padding: 20px;" +
                            "-fx-background-radius: 8px;");
        
        Label formTitle = new Label("Event Details");
        formTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        // Form container with yellow background
        VBox formContainer = new VBox(15);
        formContainer.setStyle("-fx-background-color: " + YELLOW_COLOR + ";" +
                              "-fx-padding: 20px;" +
                              "-fx-background-radius: 8px;");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        
        // Style form fields
        styleTextField(eventNameField);
        styleTextArea(eventDescriptionArea);
        styleTextField(eventLocationField);
        styleDatePicker(eventStartDatePicker);
        styleDatePicker(eventEndDatePicker);
        styleSpinner(sessionCountSpinner);
        
        eventDescriptionArea.setPrefRowCount(3);
        
        // Form labels
        grid.add(createFormLabel("Event Name:"), 0, 0);
        grid.add(eventNameField, 1, 0);
        grid.add(createFormLabel("Description:"), 0, 1);
        grid.add(eventDescriptionArea, 1, 1);
        grid.add(createFormLabel("Start Date:"), 0, 2);
        grid.add(eventStartDatePicker, 1, 2);
        grid.add(createFormLabel("End Date:"), 0, 3);
        grid.add(eventEndDatePicker, 1, 3);
        grid.add(createFormLabel("Location:"), 0, 4);
        grid.add(eventLocationField, 1, 4);
        grid.add(createFormLabel("Number of Sessions:"), 0, 5);
        grid.add(sessionCountSpinner, 1, 5);
        
        // Make the text field column expandable
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);
        
        formContainer.getChildren().add(grid);
        formSection.getChildren().addAll(formTitle, formContainer);
        
        // Button Section
        HBox buttonSection = new HBox(15);
        buttonSection.setAlignment(Pos.CENTER);
        buttonSection.setPadding(new Insets(10, 0, 0, 0));
        
        Button generateButton = createStyledButton("Add Session Details");
        Button cancelButton = createStyledButton("Cancel");
        
        generateButton.setOnAction(e -> generateSessionForms());
        cancelButton.setOnAction(e -> handleBackToDashboard());
        
        buttonSection.getChildren().addAll(generateButton, cancelButton);
        
        contentPane.getChildren().addAll(titleLabel, formSection, buttonSection);
        contentWrapper.getChildren().add(contentPane);
        mainLayout.setCenter(contentWrapper);
        
        Scene scene = new Scene(mainLayout, 1000, 700);
        stage.setTitle("Eventure - Create Event");
        stage.setScene(scene);
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
        
        // Main layout with navigation
        BorderPane mainLayout = new BorderPane();
        
        // --- Top Navigation Bar ---
        HBox navBar = createNavBar();
        mainLayout.setTop(navBar);
        
        // --- Main Content ---
        StackPane contentWrapper = new StackPane();
        contentWrapper.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        contentWrapper.setPadding(new Insets(15));
        
        VBox contentPane = new VBox(20);
        contentPane.setPadding(new Insets(25));
        contentPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                            "-fx-background-radius: 10px;");
        
        // Title Section
        Label titleLabel = new Label("Create Event - Session Details");
        titleLabel.setStyle("-fx-background-color: " + YELLOW_COLOR + ";" +
                           "-fx-padding: 12px 18px;" +
                           "-fx-background-radius: 8px;" +
                           "-fx-font-weight: bold;" +
                           "-fx-font-size: 16px;");
        
        // Event Summary Section
        VBox eventSummarySection = createEventSummarySection();
        
        // Sessions Section
        VBox sessionsSection = new VBox(15);
        sessionsSection.setStyle("-fx-background-color: " + BLUE_COLOR + ";" +
                                "-fx-padding: 20px;" +
                                "-fx-background-radius: 8px;");
        
        Label sessionsTitle = new Label("Session Details");
        sessionsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        // Sessions container with scrolling
        ScrollPane sessionsScrollPane = new ScrollPane(sessionsContainer);
        sessionsScrollPane.setFitToWidth(true);
        sessionsScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        sessionsScrollPane.setPrefHeight(300);
        
        sessionsSection.getChildren().addAll(sessionsTitle, sessionsScrollPane);
        
        // Button Section
        HBox buttonSection = new HBox(15);
        buttonSection.setAlignment(Pos.CENTER);
        buttonSection.setPadding(new Insets(15, 0, 0, 0));
        
        Button submitButton = createStyledButton("Create Event and Sessions");
        Button backButton = createStyledButton("Back to Event Details");
        Button cancelButton = createStyledButton("Cancel");
        
        submitButton.setOnAction(e -> saveEventAndSessions());
        backButton.setOnAction(e -> showInitialForm());
        cancelButton.setOnAction(e -> handleBackToDashboard());
        
        buttonSection.getChildren().addAll(submitButton, backButton, cancelButton);
        
        contentPane.getChildren().addAll(titleLabel, eventSummarySection, sessionsSection, buttonSection);
        
        ScrollPane mainScrollPane = new ScrollPane(contentPane);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        contentWrapper.getChildren().add(mainScrollPane);
        mainLayout.setCenter(contentWrapper);
        
        Scene scene = new Scene(mainLayout, 1100, 800);
        stage.setTitle("Eventure - Create Event with Sessions");
        stage.setScene(scene);
        stage.show();
    }
    
    private VBox createEventSummarySection() {
        VBox summarySection = new VBox(10);
        summarySection.setStyle("-fx-background-color: " + BLUE_COLOR + ";" +
                               "-fx-padding: 15px;" +
                               "-fx-background-radius: 8px;");
        
        Label summaryTitle = new Label("Event Summary");
        summaryTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        VBox summaryContainer = new VBox(8);
        summaryContainer.setStyle("-fx-background-color: " + YELLOW_COLOR + ";" +
                                 "-fx-padding: 15px;" +
                                 "-fx-background-radius: 6px;");
        
        Label nameLabel = new Label("Event: " + eventNameField.getText());
        Label dateLabel = new Label("Date: " + eventStartDatePicker.getValue() + " to " + eventEndDatePicker.getValue());
        Label locationLabel = new Label("Location: " + eventLocationField.getText());
        Label sessionsLabel = new Label("Sessions: " + sessionCountSpinner.getValue());
        
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        dateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        locationLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        sessionsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        summaryContainer.getChildren().addAll(nameLabel, dateLabel, locationLabel, sessionsLabel);
        summarySection.getChildren().addAll(summaryTitle, summaryContainer);
        
        return summarySection;
    }
    
    private HBox createNavBar() {
        HBox navBar = new HBox();
        navBar.setPadding(new Insets(8, 15, 8, 15));
        navBar.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        navBar.setAlignment(Pos.CENTER_LEFT);
        
        // Logo section
        HBox logoSection = new HBox(8);
        logoSection.setAlignment(Pos.CENTER_LEFT);
        
        // Logo with circular white background
        try {
            StackPane logoContainer = new StackPane();
            Circle whiteCircle = new Circle(20);
            whiteCircle.setFill(Color.WHITE);
            
            ImageView logoView = new ImageView(new Image(new FileInputStream("resources/logo.png")));
            logoView.setFitHeight(32);
            logoView.setFitWidth(32);
            logoView.setPreserveRatio(true);
            
            logoContainer.getChildren().addAll(whiteCircle, logoView);
            logoSection.getChildren().add(logoContainer);
        } catch (Exception e) {
            StackPane logoContainer = new StackPane();
            Circle whiteCircle = new Circle(20);
            whiteCircle.setFill(Color.WHITE);
            
            Label logoPlaceholder = new Label("E");
            logoPlaceholder.setStyle("-fx-text-fill: " + BLUE_COLOR + "; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            logoContainer.getChildren().addAll(whiteCircle, logoPlaceholder);
            logoSection.getChildren().add(logoContainer);
        }
        
        // Title
        Label pageTitle = new Label("Create Event");
        pageTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Back to Dashboard button
        Button backToDashboardBtn = createNavButton("Back to Dashboard");
        backToDashboardBtn.setOnAction(e -> handleBackToDashboard());
        
        navBar.getChildren().addAll(logoSection, pageTitle, spacer, backToDashboardBtn);
        return navBar;
    }
    
    // New method to handle navigation back to dashboard
    private void handleBackToDashboard() {
        if (onBackToDashboard != null) {
            // Use the callback function if provided
            onBackToDashboard.accept(null);
        } else {
            // Fallback to the original behavior if no callback is provided
            OrganizerDashboard dashboard = new OrganizerDashboard();
            dashboard.show(stage, organizerID);
        }
    }
    
    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + WHITE_COLOR + ";" +
            "-fx-text-fill: #333333;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 6px 10px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 12px;"
        );
        button.setOnMouseEntered(e ->
            button.setStyle(
                "-fx-background-color: " + YELLOW_COLOR + ";" +
                "-fx-text-fill: #333333;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                "-fx-padding: 6px 10px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 12px;"
            )
        );
        button.setOnMouseExited(e ->
            button.setStyle(
                "-fx-background-color: " + WHITE_COLOR + ";" +
                "-fx-text-fill: #333333;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                "-fx-padding: 6px 10px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 12px;"
            )
        );
        return button;
    }
    
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + WHITE_COLOR + ";" +
            "-fx-text-fill: " + DARK_TEXT + ";" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 8px 16px;"
        );
        
        button.setOnMouseEntered(e ->
            button.setStyle(
                "-fx-background-color: " + BLUE_COLOR + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 8px 16px;"
            )
        );
        
        button.setOnMouseExited(e ->
            button.setStyle(
                "-fx-background-color: " + WHITE_COLOR + ";" +
                "-fx-text-fill: " + DARK_TEXT + ";" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 8px 16px;"
            )
        );
        return button;
    }
    
    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        return label;
    }
    
    private void styleTextField(TextField field) {
        field.setStyle(
            "-fx-background-color: " + WHITE_COLOR + ";" +
            "-fx-border-color: #cccccc;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;" +
            "-fx-padding: 8px;"
        );
    }
    
    private void styleTextArea(TextArea area) {
        area.setStyle(
            "-fx-background-color: " + WHITE_COLOR + ";" +
            "-fx-border-color: #cccccc;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;" +
            "-fx-padding: 8px;"
        );
    }
    
    private void styleDatePicker(DatePicker picker) {
        picker.setStyle(
            "-fx-background-color: " + WHITE_COLOR + ";" +
            "-fx-border-color: #cccccc;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;"
        );
    }
    
    private void styleSpinner(Spinner<Integer> spinner) {
        spinner.setStyle(
            "-fx-background-color: " + WHITE_COLOR + ";" +
            "-fx-border-color: #cccccc;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;"
        );
    }
    
    private void saveEventAndSessions() {
        String eventName = eventNameField.getText();
        String eventDescription = eventDescriptionArea.getText();
        LocalDate eventStartDate = eventStartDatePicker.getValue();
        LocalDate eventEndDate = eventEndDatePicker.getValue();
        String eventLocation = eventLocationField.getText();
        
        if (eventName.isEmpty() || eventDescription.isEmpty() || eventStartDate == null || eventEndDate == null || eventLocation.isEmpty()) {
            showAlert("Validation Error", "Please fill all event fields.");
            return;
        }
        
        for (SessionForm sf : sessionForms) {
            if (!sf.isValid()) {
                showAlert("Validation Error", "Please fill all fields for Session #" + sf.getSessionNumber());
                return;
            }
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            
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
            
            String sessionSQL = "INSERT INTO Session (eventID, title, description, location, startTime, endTime) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement sessionStmt = conn.prepareStatement(sessionSQL);
            
            for (SessionForm sf : sessionForms) {
                sessionStmt.setInt(1, eventID);
                sessionStmt.setString(2, sf.getTitle());
                sessionStmt.setString(3, sf.getDescription());
                sessionStmt.setString(4, sf.getLocation());
                Time startTime = Time.valueOf(LocalTime.parse(sf.getStartTime(), timeFormatter));
                Time endTime = Time.valueOf(LocalTime.parse(sf.getEndTime(), timeFormatter));
                sessionStmt.setTime(5, startTime);
                sessionStmt.setTime(6, endTime);
                sessionStmt.addBatch();
            }
            sessionStmt.executeBatch();
            
            String notifSQL = "INSERT INTO Notification (name, title, message, createdAt, isRead, notificationType, userID) VALUES (?, ?, ?, GETDATE(), 0, ?, ?)";
            PreparedStatement notifStmt = conn.prepareStatement(notifSQL);
            notifStmt.setString(1, "Event Created");
            notifStmt.setString(2, "New Event: " + eventName);
            notifStmt.setString(3, "You have successfully created the event \"" + eventName + "\" with " + sessionForms.size() + " sessions.");
            notifStmt.setString(4, "Organizer");
            notifStmt.setInt(5, organizerID);
            notifStmt.executeUpdate();
            
            conn.commit();
            showAlert("Success", "Event and sessions created successfully!");
            
            // Return to dashboard using the callback or fallback
            handleBackToDashboard();
            
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
        
        // Style the alert to match the theme
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";");
        
        alert.showAndWait();
    }
    
    // Updated SessionForm with styling
    private class SessionForm {
        private final int sessionNumber;
        private final TextField titleField = new TextField();
        private final TextArea descriptionArea = new TextArea();
        private final TextField locationField = new TextField();
        private final TextField startTimeField = new TextField();
        private final TextField endTimeField = new TextField();
        
        public SessionForm(int number) {
            this.sessionNumber = number;
            descriptionArea.setPrefRowCount(2);
            startTimeField.setPromptText("HH:mm");
            endTimeField.setPromptText("HH:mm");
            
            // Style the fields
            styleTextField(titleField);
            styleTextArea(descriptionArea);
            styleTextField(locationField);
            styleTextField(startTimeField);
            styleTextField(endTimeField);
        }
        
        public Pane getPane() {
            VBox sessionPane = new VBox(10);
            sessionPane.setStyle(
                "-fx-background-color: " + YELLOW_COLOR + ";" +
                "-fx-padding: 15px;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: #cccccc;" +
                "-fx-border-radius: 8px;" +
                "-fx-border-width: 1px;"
            );
            
            Label sessionTitle = new Label("Session #" + sessionNumber);
            sessionTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(10);
            
            grid.add(createFormLabel("Title:"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(createFormLabel("Description:"), 0, 1);
            grid.add(descriptionArea, 1, 1);
            grid.add(createFormLabel("Location:"), 0, 2);
            grid.add(locationField, 1, 2);
            grid.add(createFormLabel("Start Time (HH:mm):"), 0, 3);
            grid.add(startTimeField, 1, 3);
            grid.add(createFormLabel("End Time (HH:mm):"), 0, 4);
            grid.add(endTimeField, 1, 4);
            
            // Make the text field column expandable
            ColumnConstraints col1 = new ColumnConstraints();
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().addAll(col1, col2);
            
            sessionPane.getChildren().addAll(sessionTitle, grid);
            return sessionPane;
        }
        
        public boolean isValid() {
            return !titleField.getText().isEmpty() &&
                   !descriptionArea.getText().isEmpty() &&
                   !locationField.getText().isEmpty() &&
                   !startTimeField.getText().isEmpty() &&
                   !endTimeField.getText().isEmpty();
        }
        
        public String getTitle() { return titleField.getText(); }
        public String getDescription() { return descriptionArea.getText(); }
        public String getLocation() { return locationField.getText(); }
        public String getStartTime() { return startTimeField.getText(); }
        public String getEndTime() { return endTimeField.getText(); }
        public int getSessionNumber() { return sessionNumber; }
    }
}