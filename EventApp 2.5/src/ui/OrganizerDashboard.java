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
import models.Event;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrganizerDashboard {
    // UI color constants matching attendee dashboard
    private static final String BLUE_COLOR = "#97A9D1";  // Nav bar and main background color
    private static final String YELLOW_COLOR = "#F1D747"; // Event cards and welcome banner
    private static final String WHITE_COLOR = "#FFFFFF";
    private static final String DARK_TEXT = "#333333";
    
    private int organizerID;
    private String organizerName = "";
    private Stage currentStage;
    private TableView<Event> eventsTable;
    private ListView<String> notificationsList;
    private Label totalEventsLabel;
    private Label totalAttendeesLabel;
    
    public void show(Stage stage, int organizerID) {
        this.organizerID = organizerID;
        this.currentStage = stage;
        
        // Fetch organizer name from database
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT firstName, lastName FROM [User] WHERE userID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, organizerID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                organizerName = rs.getString("firstName") + " " + rs.getString("lastName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Main layout
        BorderPane mainLayout = new BorderPane();
        
        // --- Top Navigation Bar ---
        HBox navBar = createNavBar();
        mainLayout.setTop(navBar);
        
        // --- Main Content ---
        // Create a blue background pane for the content
        StackPane contentWrapper = new StackPane();
        contentWrapper.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        contentWrapper.setPadding(new Insets(15));
        
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(15));
        contentPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                            "-fx-background-radius: 10px;");
        
        // Make contentPane expand to fill available space
        VBox.setVgrow(contentPane, Priority.ALWAYS);
        
        // Welcome Banner
        Label welcomeLabel = new Label("Welcome to EVENTURE, " + organizerName);
        welcomeLabel.setStyle("-fx-background-color: " + YELLOW_COLOR + ";" +
                             "-fx-padding: 12px 18px;" +
                             "-fx-background-radius: 8px;" +
                             "-fx-font-weight: bold;" +
                             "-fx-font-size: 14px;");
        
        // Content Grid - Two columns with equal width and full height
        HBox contentGrid = new HBox(15);
        contentGrid.setPadding(new Insets(10, 0, 0, 0));
        // Make contentGrid expand vertically
        VBox.setVgrow(contentGrid, Priority.ALWAYS);
        
        // Left side - Events Management Section
        VBox eventsSection = createEventsSection();
        // Make left section expand to fill available space
        HBox.setHgrow(eventsSection, Priority.ALWAYS);
        
        // Right side - Analytics and Notifications
        VBox rightSection = new VBox(15);
        // Make right section expand to fill available space
        HBox.setHgrow(rightSection, Priority.ALWAYS);
        
        // Analytics
        VBox analyticsBox = createAnalyticsSection();
        
        // Notifications - make this expand to fill remaining space
        VBox notificationsBox = createNotificationsSection();
        VBox.setVgrow(notificationsBox, Priority.ALWAYS);
        
        rightSection.getChildren().addAll(analyticsBox, notificationsBox);
        
        contentGrid.getChildren().addAll(eventsSection, rightSection);
        contentPane.getChildren().addAll(welcomeLabel, contentGrid);
        
        contentWrapper.getChildren().add(contentPane);
        mainLayout.setCenter(contentWrapper);
        
        // Updated scene size to match attendee dashboard
        Scene scene = new Scene(mainLayout, 1200, 800);
        stage.setTitle("Eventure - Organizer Dashboard");
        stage.setScene(scene);
        stage.show();
        
        // Initial data load
        loadEvents();
        loadUnreadNotifications();
        loadAnalytics();
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
            // Create a white circle background
            StackPane logoContainer = new StackPane();
            Circle whiteCircle = new Circle(20);
            whiteCircle.setFill(Color.WHITE);
            
            // Load the logo image
            ImageView logoView = new ImageView(new Image(new FileInputStream("resources/logo.png")));
            logoView.setFitHeight(32);
            logoView.setFitWidth(32);
            logoView.setPreserveRatio(true);
            
            // Stack the logo on the white circle
            logoContainer.getChildren().addAll(whiteCircle, logoView);
            logoSection.getChildren().add(logoContainer);
        } catch (Exception e) {
            // If logo loading fails, use text placeholder with white background
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
        
        // Navigation Buttons with white background
        Button eventsBtn = createNavButton("Events");
        Button myEventsBtn = createNavButton("My Events");
        Button createEventBtn = createNavButton("Create Event");
        Button attendeesBtn = createNavButton("Attendees");
        Button notificationsBtn = createNavButton("Notifications");
        Button profileBtn = createNavButton("Profile");
        
        navButtonsSection.getChildren().addAll(eventsBtn, myEventsBtn, createEventBtn, attendeesBtn, notificationsBtn, profileBtn);
        
        // Switch Account button - right aligned
        Button switchAccountBtn = createNavButton("Switch Account");
        
        // Create spacers for proper distribution
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        
        // Add all sections to navbar with proper spacing
        navBar.getChildren().addAll(logoSection, leftSpacer, navButtonsSection, rightSpacer, switchAccountBtn);
        
        // Navigation Button Actions
        switchAccountBtn.setOnAction(e -> {
            new LoginPage(new Stage());
            currentStage.close();
        });
        
        eventsBtn.setOnAction(e -> {
            EventListPage eventsPage = new EventListPage(false);
            eventsPage.show(currentStage, organizerID);
        });
        
        myEventsBtn.setOnAction(e -> {
            OrganizerEventsPage page = new OrganizerEventsPage();
            page.show(currentStage, organizerID);
        });
        
        createEventBtn.setOnAction(e -> {
            CreateEventPage page = new CreateEventPage();
            page.show(new Stage(), organizerID);
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
            notifPage.show(currentStage, organizerID);
        });
        
        profileBtn.setOnAction(e -> {
            ProfilePage profilePage = new ProfilePage();
            profilePage.show(currentStage, organizerID);
        });
        
        return navBar;
    }
    
    private Button createNavButton(String text) {
        Button button = new Button(text);
        // Add a white background to nav buttons
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
    
    private VBox createEventsSection() {
        VBox eventsSection = new VBox(12);
        // Remove fixed width to allow expansion
        // eventsSection.setPrefWidth(570);
        
        // Add blue background panel to match the design
        eventsSection.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-padding: 12px;" +
            "-fx-background-radius: 8px;"
        );
        
        Label eventsLabel = new Label("My Events");
        eventsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        // Events table with styling - make it expand to fill available space
        eventsTable = new TableView<>();
        eventsTable.setStyle("-fx-background-color: " + YELLOW_COLOR + "; -fx-background-radius: 8px;");
        // Allow table to grow vertically
        VBox.setVgrow(eventsTable, Priority.ALWAYS);
        
        TableColumn<Event, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("eventID"));
        idCol.setPrefWidth(50);
        
        TableColumn<Event, String> nameCol = new TableColumn<>("Event Name");
        nameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("eventName"));
        nameCol.setPrefWidth(200);
        
        TableColumn<Event, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("startDate"));
        startDateCol.setPrefWidth(120);
        
        TableColumn<Event, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("endDate"));
        endDateCol.setPrefWidth(120);
        
        eventsTable.getColumns().addAll(idCol, nameCol, startDateCol, endDateCol);
        // Remove fixed height to allow expansion
        // eventsTable.setPrefHeight(200);
        eventsTable.setMinHeight(300); // Set minimum height instead
        
        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(8, 0, 0, 0));
        
        Button viewDetailsBtn = new Button("View Details");
        Button refreshBtn = new Button("Refresh");
        
        styleActionButton(viewDetailsBtn);
        styleActionButton(refreshBtn);
        
        viewDetailsBtn.setDisable(true);
        eventsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            viewDetailsBtn.setDisable(newSel == null);
        });
        
        viewDetailsBtn.setOnAction(e -> {
            Event selectedEvent = eventsTable.getSelectionModel().getSelectedItem();
            if (selectedEvent != null) {
                showEventDetails(selectedEvent);
            }
        });
        
        refreshBtn.setOnAction(e -> {
            loadEvents();
            loadUnreadNotifications();
            loadAnalytics();
        });
        
        buttonBox.getChildren().addAll(viewDetailsBtn, refreshBtn);
        
        eventsSection.getChildren().addAll(eventsLabel, eventsTable, buttonBox);
        return eventsSection;
    }
    
    private VBox createAnalyticsSection() {
        VBox analyticsSection = new VBox(8);
        // Remove fixed width to allow expansion
        // analyticsSection.setPrefWidth(570);
        
        // Add blue background to match the design
        analyticsSection.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-padding: 12px;" +
            "-fx-background-radius: 8px;"
        );
        
        Label analyticsLabel = new Label("Event Analytics");
        analyticsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        VBox analyticsContainer = new VBox(8);
        analyticsContainer.setPadding(new Insets(8));
        analyticsContainer.setStyle(
            "-fx-background-color: " + YELLOW_COLOR + ";" +
            "-fx-background-radius: 6px;"
        );
        
        totalEventsLabel = new Label("Total Events: 0");
        totalEventsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        totalAttendeesLabel = new Label("Total Attendees: 0");
        totalAttendeesLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        analyticsContainer.getChildren().addAll(totalEventsLabel, totalAttendeesLabel);
        analyticsSection.getChildren().addAll(analyticsLabel, analyticsContainer);
        
        return analyticsSection;
    }
    
    private VBox createNotificationsSection() {
        VBox notificationsSection = new VBox(8);
        // Remove fixed width to allow expansion
        // notificationsSection.setPrefWidth(570);
        
        // Add blue background to match the design
        notificationsSection.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-padding: 12px;" +
            "-fx-background-radius: 8px;"
        );
        
        Label notificationLabel = new Label("Recent Notifications");
        notificationLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        notificationsList = new ListView<>();
        // Remove fixed height and let it expand to fill remaining space
        // notificationsList.setPrefHeight(150);
        notificationsList.setMinHeight(200); // Set minimum height instead
        VBox.setVgrow(notificationsList, Priority.ALWAYS); // Allow to grow
        notificationsList.setStyle(
            "-fx-background-color: " + YELLOW_COLOR + ";" +
            "-fx-background-radius: 6px;"
        );
        
        notificationsSection.getChildren().addAll(notificationLabel, notificationsList);
        return notificationsSection;
    }
    
    private void styleActionButton(Button button) {
        button.setStyle(
            "-fx-background-color: " + WHITE_COLOR + ";" +
            "-fx-text-fill: " + DARK_TEXT + ";" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 5px;" +
            "-fx-padding: 6px 12px;"
        );
        
        button.setOnMouseEntered(e ->
            button.setStyle(
                "-fx-background-color: " + BLUE_COLOR + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 5px;" +
                "-fx-padding: 6px 12px;"
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
                "-fx-padding: 6px 12px;"
            )
        );
    }
    
    private void loadEvents() {
        eventsTable.getItems().clear();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT eventID, name, startDate, endDate FROM Event WHERE organizerID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, organizerID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                eventsTable.getItems().add(new Event(
                        rs.getInt("eventID"),
                        rs.getString("name"),
                        rs.getString("startDate"),
                        rs.getString("endDate")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadUnreadNotifications() {
        notificationsList.getItems().clear();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT title, message, createdAt
                FROM Notification
                WHERE userID = ? AND notificationType = 'Organizer' AND isRead = 0
                ORDER BY createdAt DESC
                OFFSET 0 ROWS FETCH NEXT 5 ROWS ONLY
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, organizerID);
            ResultSet rs = stmt.executeQuery();
            boolean hasNotif = false;
            while (rs.next()) {
                hasNotif = true;
                String title = rs.getString("title");
                String message = rs.getString("message");
                String timestamp = rs.getString("createdAt");
                notificationsList.getItems().add("[" + timestamp + "] " + title + ": " + message);
            }
            if (!hasNotif) {
                notificationsList.getItems().add("No new notifications.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            notificationsList.getItems().add("Error loading notifications.");
        }
    }
    
    private void loadAnalytics() {
        try (Connection conn = DBConnection.getConnection()) {
            // Count total events
            String eventSql = "SELECT COUNT(*) as totalEvents FROM Event WHERE organizerID = ?";
            PreparedStatement eventStmt = conn.prepareStatement(eventSql);
            eventStmt.setInt(1, organizerID);
            ResultSet eventRs = eventStmt.executeQuery();
            if (eventRs.next()) {
                totalEventsLabel.setText("Total Events: " + eventRs.getInt("totalEvents"));
            }
            
            // Count total attendees across all events
            String attendeeSql = """
                SELECT COUNT(DISTINCT r.userID) as totalAttendees 
                FROM Registration r 
                JOIN Event e ON r.eventID = e.eventID 
                WHERE e.organizerID = ?
            """;
            PreparedStatement attendeeStmt = conn.prepareStatement(attendeeSql);
            attendeeStmt.setInt(1, organizerID);
            ResultSet attendeeRs = attendeeStmt.executeQuery();
            if (attendeeRs.next()) {
                totalAttendeesLabel.setText("Total Attendees: " + attendeeRs.getInt("totalAttendees"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            totalEventsLabel.setText("Total Events: Error");
            totalAttendeesLabel.setText("Total Attendees: Error");
        }
    }
    
    private void showEventDetails(Event event) {
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Event Details - " + event.getEventName());
        
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: " + WHITE_COLOR + ";");
        
        // Event details with styled labels
        VBox detailsBox = new VBox(10);
        detailsBox.setStyle(
            "-fx-background-color: " + YELLOW_COLOR + ";" +
            "-fx-padding: 15px;" +
            "-fx-background-radius: 8px;"
        );
        
        Label titleLabel = new Label("Event Details");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label idLabel = new Label("Event ID: " + event.getEventID());
        Label nameLabel = new Label("Name: " + event.getEventName());
        Label startLabel = new Label("Start Date: " + event.getStartDate());
        Label endLabel = new Label("End Date: " + event.getEndDate());
        
        idLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        startLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        endLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        
        detailsBox.getChildren().addAll(titleLabel, idLabel, nameLabel, startLabel, endLabel);
        
        Button closeBtn = new Button("Close");
        styleActionButton(closeBtn);
        closeBtn.setOnAction(e -> detailsStage.close());
        
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(closeBtn);
        
        layout.getChildren().addAll(detailsBox, buttonBox);
        
        Scene scene = new Scene(layout, 400, 300);
        detailsStage.setScene(scene);
        detailsStage.show();
    }
}