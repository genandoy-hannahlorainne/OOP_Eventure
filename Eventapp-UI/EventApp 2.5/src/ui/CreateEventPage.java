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

public class CreateEventPage {
    // UI color constants matching the design framework
    private static final String BLUE_COLOR = "#97A9D1";
    private static final String YELLOW_COLOR = "#F1D747";
    private static final String WHITE_COLOR = "#FFFFFF";
    private static final String DARK_TEXT = "#333333";
    
    private Stage stage;
    private int organizerID;
    private TextField eventNameField = new TextField();
    private TextArea eventDescriptionArea = new TextArea();
    private DatePicker eventStartDatePicker = new DatePicker();
    private DatePicker eventEndDatePicker = new DatePicker();
    private TextField eventLocationField = new TextField();
    private Spinner<Integer> sessionCountSpinner = new Spinner<>(1, 10, 1);
    private VBox sessionsContainer = new VBox(15);
    private final List<SessionForm> sessionForms = new ArrayList<>();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public void show(Stage stage, int organizerID) {
        this.stage = stage;
        this.organizerID = organizerID;
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
        
        // Header section
        Label header = new Label("Create New Event");
        header.setStyle("-fx-background-color: " + YELLOW_COLOR + ";" +
                       "-fx-padding: 12px 18px;" +
                       "-fx-background-radius: 8px;" +
                       "-fx-font-weight: bold;" +
                       "-fx-font-size: 16px;");
        
        // Event form section
        VBox formSection = createEventFormSection();
        
        // Button section
        HBox buttonSection = new HBox(10);
        buttonSection.setAlignment(Pos.CENTER);
        buttonSection.setPadding(new Insets(15, 0, 0, 0));
        
        Button generateButton = new Button("Add Session Details");
        styleActionButton(generateButton);
        generateButton.setOnAction(e -> generateSessionForms());
        
        buttonSection.getChildren().add(generateButton);
        
        contentPane.getChildren().addAll(header, formSection, buttonSection);
        contentWrapper.getChildren().add(contentPane);
        mainLayout.setCenter(contentWrapper);
        
        Scene scene = new Scene(mainLayout, 1200, 800);
        stage.setTitle("Eventure - Create Event");
        stage.setScene(scene);
        stage.show();
    }
    
    private VBox createEventFormSection() {
        VBox formSection = new VBox(15);
        formSection.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-padding: 20px;" +
            "-fx-background-radius: 8px;"
        );
        
        Label sectionLabel = new Label("Event Information");
        sectionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        // Form container with yellow background
        VBox formContainer = new VBox(12);
        formContainer.setStyle("-fx-background-color: " + YELLOW_COLOR + "; -fx-background-radius: 8px; -fx-padding: 20px;");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        
        // Style form fields
        styleFormField(eventNameField);
        styleFormField(eventDescriptionArea);
        styleFormField(eventLocationField);
        styleFormField(eventStartDatePicker);
        styleFormField(eventEndDatePicker);
        styleFormField(sessionCountSpinner);
        
        eventDescriptionArea.setPrefRowCount(3);
        
        // Add form fields with styled labels
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
        
        formContainer.getChildren().add(grid);
        formSection.getChildren().addAll(sectionLabel, formContainer);
        
        return formSection;
    }
    
    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + DARK_TEXT + ";");
        return label;
    }
    
    private void styleFormField(Control field) {
        field.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                      "-fx-border-color: #ccc;" +
                      "-fx-border-radius: 5px;" +
                      "-fx-background-radius: 5px;" +
                      "-fx-padding: 8px;" +
                      "-fx-font-size: 12px;");
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
        
        // Header section
        Label header = new Label("Create Event - Step 2");
        header.setStyle("-fx-background-color: " + YELLOW_COLOR + ";" +
                       "-fx-padding: 12px 18px;" +
                       "-fx-background-radius: 8px;" +
                       "-fx-font-weight: bold;" +
                       "-fx-font-size: 16px;");
        
        // Event details section (readonly)
        VBox eventDetailsSection = createEventDetailsSection();
        
        // Sessions section
        VBox sessionSection = createSessionSection();
        
        // Submit button
        HBox buttonSection = new HBox(10);
        buttonSection.setAlignment(Pos.CENTER);
        buttonSection.setPadding(new Insets(15, 0, 0, 0));
        
        Button submitButton = new Button("Create Event and Sessions");
        styleActionButton(submitButton);
        submitButton.setOnAction(e -> saveEventAndSessions());
        
        buttonSection.getChildren().add(submitButton);
        
        contentPane.getChildren().addAll(header, eventDetailsSection, new Separator(), sessionSection, buttonSection);
        
        // Wrap in scroll pane
        ScrollPane scrollPane = new ScrollPane(contentPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + BLUE_COLOR + ";");
        
        contentWrapper.getChildren().add(scrollPane);
        mainLayout.setCenter(contentWrapper);
        
        Scene scene = new Scene(mainLayout, 1200, 800);
        stage.setTitle("Eventure - Create Event with Sessions");
        stage.setScene(scene);
        stage.show();
    }
    
    private VBox createEventDetailsSection() {
        VBox detailsSection = new VBox(12);
        detailsSection.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-padding: 15px;" +
            "-fx-background-radius: 8px;"
        );
        
        Label sectionLabel = new Label("Event Details Summary");
        sectionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        VBox summaryContainer = new VBox(8);
        summaryContainer.setStyle("-fx-background-color: " + YELLOW_COLOR + "; -fx-background-radius: 8px; -fx-padding: 15px;");
        
        Label nameLabel = new Label("Event: " + eventNameField.getText());
        Label locationLabel = new Label("Location: " + eventLocationField.getText());
        Label dateLabel = new Label("Date: " + eventStartDatePicker.getValue() + " to " + eventEndDatePicker.getValue());
        
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        locationLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        dateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        summaryContainer.getChildren().addAll(nameLabel, locationLabel, dateLabel);
        detailsSection.getChildren().addAll(sectionLabel, summaryContainer);
        
        return detailsSection;
    }
    
    private VBox createSessionSection() {
        VBox sessionSection = new VBox(15);
        sessionSection.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-padding: 15px;" +
            "-fx-background-radius: 8px;"
        );
        
        Label sectionLabel = new Label("Session Details");
        sectionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        VBox sessionContainer = new VBox(10);
        sessionContainer.setStyle("-fx-background-color: " + YELLOW_COLOR + "; -fx-background-radius: 8px; -fx-padding: 15px;");
        sessionContainer.getChildren().add(sessionsContainer);
        
        sessionSection.getChildren().addAll(sectionLabel, sessionContainer);
        return sessionSection;
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
        
        // Navigation buttons section - centered
        HBox navButtonsSection = new HBox(10);
        navButtonsSection.setAlignment(Pos.CENTER);
        
        Button dashboardBtn = createNavButton("Dashboard");
        Button eventsBtn = createNavButton("Events");
        Button myEventsBtn = createNavButton("My Events");
        Button createEventBtn = createNavButton("Create Event");
        Button attendeesBtn = createNavButton("Attendees");
        Button notificationsBtn = createNavButton("Notifications");
        Button profileBtn = createNavButton("Profile");
        
        // Highlight current page
        createEventBtn.setStyle(getActiveNavButtonStyle());
        
        navButtonsSection.getChildren().addAll(dashboardBtn, eventsBtn, myEventsBtn, createEventBtn, attendeesBtn, notificationsBtn, profileBtn);
        
        // Switch Account button - right aligned
        Button switchAccountBtn = createNavButton("Switch Account");
        
        // Create spacers for proper distribution
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        
        navBar.getChildren().addAll(logoSection, leftSpacer, navButtonsSection, rightSpacer, switchAccountBtn);
        
        // Navigation Button Actions
        dashboardBtn.setOnAction(e -> {
            OrganizerDashboard dashboard = new OrganizerDashboard();
            dashboard.show(stage, organizerID);
        });
        
        eventsBtn.setOnAction(e -> {
            EventListPage eventsPage = new EventListPage(false);
            eventsPage.show(stage, organizerID);
        });
        
        myEventsBtn.setOnAction(e -> {
            OrganizerEventsPage page = new OrganizerEventsPage();
            page.show(stage, organizerID);
        });
        
        attendeesBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Event ID");
            dialog.setHeaderText("Enter Event ID");
            dialog.setContentText("Please enter the event ID:");
            dialog.showAndWait().ifPresent(eventID -> {
                try {
                    int id = Integer.parseInt(eventID);
                    ViewAttendeesPage page = new ViewAttendeesPage();
                    page.show(new Stage(), id, false);
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, "Invalid Event ID").showAndWait();
                }
            });
        });
        
        notificationsBtn.setOnAction(e -> {
            OrganizerNotificationPage notifPage = new OrganizerNotificationPage();
            notifPage.show(stage, organizerID);
        });
        
        profileBtn.setOnAction(e -> {
            ProfilePage profilePage = new ProfilePage();
            profilePage.show(stage, organizerID);
        });
        
        switchAccountBtn.setOnAction(e -> {
            new LoginPage(new Stage());
            stage.close();
        });
        
        return navBar;
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
    
    private String getActiveNavButtonStyle() {
        return "-fx-background-color: " + YELLOW_COLOR + ";" +
               "-fx-text-fill: #333333;" +
               "-fx-font-weight: bold;" +
               "-fx-font-size: 12px;" +
               "-fx-padding: 6px 10px;" +
               "-fx-cursor: hand;" +
               "-fx-background-radius: 12px;";
    }
    
    private void styleActionButton(Button button) {
        button.setStyle(
            "-fx-background-color: " + WHITE_COLOR + ";" +
            "-fx-text-fill: " + DARK_TEXT + ";" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 5px;" +
            "-fx-padding: 8px 16px;"
        );
        
        button.setOnMouseEntered(e ->
            button.setStyle(
                "-fx-background-color: " + BLUE_COLOR + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 5px;" +
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
                "-fx-background-radius: 5px;" +
                "-fx-padding: 8px 16px;"
            )
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

            String sessionSQL = "INSERT INTO Session (eventID, title, description, location, startTime, endTime, speakerID) VALUES (?, ?, ?, ?, ?, ?, ?)";
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
                sessionStmt.setInt(7, Integer.parseInt(sf.getSpeakerID()));
                sessionStmt.addBatch();
            }

            sessionStmt.executeBatch();
            conn.commit();
            showAlert("Success", "Event and sessions created successfully!");
            stage.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Database Error", "Error saving event and sessions: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Invalid input. Ensure speaker IDs are numeric and time is in HH:mm format.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class SessionForm {
        private final int sessionNumber;
        private final TextField titleField = new TextField();
        private final TextArea descriptionArea = new TextArea();
        private final TextField locationField = new TextField();
        private final TextField speakerIDField = new TextField();
        private final TextField startTimeField = new TextField();
        private final TextField endTimeField = new TextField();

        public SessionForm(int number) {
            this.sessionNumber = number;
            descriptionArea.setPrefRowCount(2);
            startTimeField.setPromptText("HH:mm");
            endTimeField.setPromptText("HH:mm");
            speakerIDField.setPromptText("Numeric ID");
            
            // Style all form fields
            styleFormField(titleField);
            styleFormField(descriptionArea);
            styleFormField(locationField);
            styleFormField(speakerIDField);
            styleFormField(startTimeField);
            styleFormField(endTimeField);
        }
        
        private void styleFormField(Control field) {
            field.setStyle("-fx-background-color: #FFFFFF;" +
                          "-fx-border-color: #ccc;" +
                          "-fx-border-radius: 5px;" +
                          "-fx-background-radius: 5px;" +
                          "-fx-padding: 6px;" +
                          "-fx-font-size: 11px;");
        }

        public Pane getPane() {
            VBox sessionBox = new VBox(12);
            sessionBox.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-border-color: #97A9D1;" +
                "-fx-border-radius: 8px;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-width: 2px;" +
                "-fx-padding: 15px;"
            );
            
            Label sessionHeader = new Label("Session #" + sessionNumber);
            sessionHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333333;");
            
            GridPane grid = new GridPane();
            grid.setHgap(12);
            grid.setVgap(10);

            grid.add(createFormLabel("Title:"), 0, 0);
            grid.add(titleField, 1, 0);

            grid.add(createFormLabel("Description:"), 0, 1);
            grid.add(descriptionArea, 1, 1);

            grid.add(createFormLabel("Location:"), 0, 2);
            grid.add(locationField, 1, 2);

            grid.add(createFormLabel("Speaker ID:"), 0, 3);
            grid.add(speakerIDField, 1, 3);

            grid.add(createFormLabel("Start Time:"), 0, 4);
            grid.add(startTimeField, 1, 4);

            grid.add(createFormLabel("End Time:"), 0, 5);
            grid.add(endTimeField, 1, 5);
            
            sessionBox.getChildren().addAll(sessionHeader, grid);
            return sessionBox;
        }
        
        private Label createFormLabel(String text) {
            Label label = new Label(text);
            label.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #333333;");
            return label;
        }

        public boolean isValid() {
            return !titleField.getText().isEmpty() &&
                   !descriptionArea.getText().isEmpty() &&
                   !locationField.getText().isEmpty() &&
                   !speakerIDField.getText().isEmpty() &&
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

        public String getSpeakerID() {
            return speakerIDField.getText();
        }

        public String getStartTime() {
            return startTimeField.getText();
        }

        public String getEndTime() {
            return endTimeField.getText();
        }
    }
}