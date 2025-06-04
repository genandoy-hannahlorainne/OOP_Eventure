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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AttendeeDashboard {
    // UI color constants based on the design
    private static final String BLUE_COLOR = "#97A9D1";  // Nav bar and main background color
    private static final String YELLOW_COLOR = "#F1D747"; // Event cards and welcome banner
    private static final String WHITE_COLOR = "#FFFFFF";
    private static final String DARK_TEXT = "#333333";
    
    private int attendeeID;
    private String attendeeName = "";
    private Stage currentStage; // Store reference to current stage
    
    public void show(Stage stage) {
        show(stage, 1); // Default attendeeID for testing
    }
    
    public void show(Stage stage, int attendeeID) {
        this.attendeeID = attendeeID;
        this.currentStage = stage; // Store the stage reference
        
        // Fetch attendee name from database
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT firstName, lastName FROM [User] WHERE userID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, attendeeID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                attendeeName = rs.getString("firstName") + " " + rs.getString("lastName");
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
        
        // Welcome Banner
        Label welcomeLabel = new Label("Welcome to EVENTURE, " + attendeeName);
        welcomeLabel.setStyle("-fx-background-color: " + YELLOW_COLOR + ";" +
                             "-fx-padding: 12px 18px;" +
                             "-fx-background-radius: 8px;" +
                             "-fx-font-weight: bold;" +
                             "-fx-font-size: 14px;");
        
        // Content Grid - Two columns with equal width
        HBox contentGrid = new HBox(15);
        contentGrid.setPadding(new Insets(10, 0, 0, 0));
        
        // Left side - Events Table (600px width)
        VBox eventsSection = createEventsTableSection();
        
        // Right side - Notifications and Calendar (600px width)
        VBox rightSection = new VBox(15);
        
        // Notifications
        VBox notificationsBox = createNotificationsSection();
        
        // Calendar
        VBox calendarBox = createCalendarSection();
        
        rightSection.getChildren().addAll(notificationsBox, calendarBox);
        
        contentGrid.getChildren().addAll(eventsSection, rightSection);
        contentPane.getChildren().addAll(welcomeLabel, contentGrid);
        
        contentWrapper.getChildren().add(contentPane);
        mainLayout.setCenter(contentWrapper);
        
        // Updated scene size: 1200x800
        Scene scene = new Scene(mainLayout, 1200, 800);
        stage.setTitle("Eventure - Attendee Dashboard");
        stage.setScene(scene);
        stage.show();
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
        Button calendarBtn = createNavButton("Calendar");
        Button myEventsBtn = createNavButton("My Events");
        Button notificationsBtn = createNavButton("Notification");
        Button profileBtn = createNavButton("Profile");
        
        navButtonsSection.getChildren().addAll(calendarBtn, myEventsBtn, notificationsBtn, profileBtn);
        
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
            new LoginPage(new Stage()); // Show login page
            currentStage.close(); // Close attendee dashboard
        });
        
        calendarBtn.setOnAction(e -> {
            CalendarPage calendarPage = new CalendarPage();
            calendarPage.show(currentStage, attendeeID);
        });
        
        myEventsBtn.setOnAction(e -> {
            MyEventsPage myEventsPage = new MyEventsPage();
            myEventsPage.show(currentStage, attendeeID);
        });
        
        notificationsBtn.setOnAction(e -> {
            NotificationPage notificationPage = new NotificationPage();
            notificationPage.show(currentStage, attendeeID);
        });
        
        profileBtn.setOnAction(e -> {
            ProfilePage profilePage = new ProfilePage();
            profilePage.show(currentStage, attendeeID);
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
    
    private VBox createEventsTableSection() {
        VBox eventsSection = new VBox(12);
        eventsSection.setPrefWidth(570); // Slightly reduced to fit better with spacing
        
        // Add blue background panel to match the design
        eventsSection.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-padding: 12px;" +
            "-fx-background-radius: 8px;"
        );
        
        // Header with title and refresh button
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER);
        
        Label availableEventsLabel = new Label("Available Events");
        availableEventsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle(
            "-fx-background-color: " + YELLOW_COLOR + ";" +
            "-fx-text-fill: #333333;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 4px 8px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 5px;"
        );
        
        headerBox.getChildren().addAll(availableEventsLabel, spacer, refreshBtn);
        
        // Create table with yellow background container
        VBox tableContainer = new VBox();
        tableContainer.setStyle(
            "-fx-background-color: " + YELLOW_COLOR + ";" +
            "-fx-padding: 8px;" +
            "-fx-background-radius: 8px;"
        );
        
        // --- Table of Events ---
        TableView<Event> eventTable = new TableView<>();
        
        // Set table properties for better styling
        eventTable.setPrefWidth(540);
        eventTable.setPrefHeight(280); // Fixed height to enable scrolling
        eventTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        eventTable.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-color: transparent;"
        );
        
        TableColumn<Event, String> nameCol = new TableColumn<>("Event Name");
        nameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(160);
        nameCol.setStyle("-fx-font-size: 11px;");
        
        TableColumn<Event, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("startDate"));
        startDateCol.setPrefWidth(90);
        startDateCol.setStyle("-fx-font-size: 11px;");
        
        TableColumn<Event, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("endDate"));
        endDateCol.setPrefWidth(90);
        endDateCol.setStyle("-fx-font-size: 11px;");
        
        TableColumn<Event, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(160);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button registerBtn = new Button("Register");
            private final Button viewBtn = new Button("View Details");
            private final HBox pane = new HBox(3, registerBtn, viewBtn);
            
            {
                // Style the buttons to match the design
                registerBtn.setStyle(
                    "-fx-background-color: " + BLUE_COLOR + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 9px;" +
                    "-fx-padding: 3px 6px;" +
                    "-fx-background-radius: 3px;" +
                    "-fx-cursor: hand;"
                );
                viewBtn.setStyle(
                    "-fx-background-color: #666666;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 9px;" +
                    "-fx-padding: 3px 6px;" +
                    "-fx-background-radius: 3px;" +
                    "-fx-cursor: hand;"
                );
                
                // Add hover effects
                registerBtn.setOnMouseEntered(e -> 
                    registerBtn.setStyle(
                        "-fx-background-color: #7A8BC4;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 9px;" +
                        "-fx-padding: 3px 6px;" +
                        "-fx-background-radius: 3px;" +
                        "-fx-cursor: hand;"
                    )
                );
                registerBtn.setOnMouseExited(e -> 
                    registerBtn.setStyle(
                        "-fx-background-color: " + BLUE_COLOR + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 9px;" +
                        "-fx-padding: 3px 6px;" +
                        "-fx-background-radius: 3px;" +
                        "-fx-cursor: hand;"
                    )
                );
                
                pane.setAlignment(Pos.CENTER);
                
                registerBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    registerToEvent(event.getEventID(), attendeeID);
                });
                
                viewBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    showEventDetails(event);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
        
        eventTable.getColumns().addAll(nameCol, startDateCol, endDateCol, actionCol);
        
        // Load Events from DB
        loadEventsFromDatabase(eventTable);
        
        // Refresh Button Action
        refreshBtn.setOnAction(e -> {
            loadEventsFromDatabase(eventTable);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Refreshed");
            alert.setHeaderText(null);
            alert.setContentText("Event list has been refreshed!");
            alert.showAndWait();
        });
        
        tableContainer.getChildren().add(eventTable);
        eventsSection.getChildren().addAll(headerBox, tableContainer);
        return eventsSection;
    }
    
    private void showEventDetails(Event event) {
        // Create a custom dialog with yellow background
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Event Details");
        alert.setHeaderText(event.getName());
        
        // Create content with better formatting
        VBox content = new VBox(8);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: " + YELLOW_COLOR + "; -fx-background-radius: 5px;");
        
        Label eventIdLabel = new Label("Event ID: " + event.getEventID());
        Label nameLabel = new Label("Event Name: " + event.getName());
        Label startLabel = new Label("Start Date: " + event.getStartDate());
        Label endLabel = new Label("End Date: " + event.getEndDate());
        Label actionLabel = new Label("\nClick 'Register' to join this event!");
        actionLabel.setStyle("-fx-font-weight: bold;");
        
        content.getChildren().addAll(eventIdLabel, nameLabel, startLabel, endLabel, actionLabel);
        
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
    
    private VBox createNotificationsSection() {
        VBox notificationsSection = new VBox(8);
        notificationsSection.setPrefWidth(570); // Slightly reduced to fit better with spacing
        
        // Add blue background to match the design
        notificationsSection.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-padding: 12px;" +
            "-fx-background-radius: 8px;"
        );
        
        Label notificationLabel = new Label("Notification");
        notificationLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        VBox notificationsList = new VBox(8);
        
        // Fetch notifications from database
        List<Notification> notifications = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT TOP 2 notificationID, title, message, createdAt FROM Notification " +
                         "WHERE userID = ? ORDER BY createdAt DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, attendeeID);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                notifications.add(new Notification(
                    rs.getInt("notificationID"),
                    rs.getString("title"),
                    rs.getString("message"),
                    rs.getTimestamp("createdAt")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Add notification items
        for (Notification notification : notifications) {
            VBox notifItem = new VBox(2);
            notifItem.setStyle(
                "-fx-background-color: " + YELLOW_COLOR + ";" + 
                "-fx-padding: 8px;" + 
                "-fx-background-radius: 6px;"
            );
            
            Label titleLabel = new Label(notification.getTitle());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            
            Label messageLabel = new Label(notification.getMessage());
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(520); // Adjusted for better fit
            messageLabel.setStyle("-fx-font-size: 11px;");
            
            notifItem.getChildren().addAll(titleLabel, messageLabel);
            notificationsList.getChildren().add(notifItem);
        }
        
        // If no notifications, show message
        if (notifications.isEmpty()) {
            Label noNotifLabel = new Label("No notifications");
            noNotifLabel.setStyle("-fx-font-style: italic; -fx-text-fill: white;");
            notificationsList.getChildren().add(noNotifLabel);
        }
        
        notificationsSection.getChildren().addAll(notificationLabel, notificationsList);
        return notificationsSection;
    }
    
    private VBox createCalendarSection() {
        VBox calendarSection = new VBox(8);
        calendarSection.setPrefWidth(570); // Slightly reduced to fit better with spacing
        
        // Add blue background to match the design
        calendarSection.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-padding: 12px;" +
            "-fx-background-radius: 8px;"
        );
        
        Label calendarLabel = new Label("My Calendar");
        calendarLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        // Calendar container with yellow background
        VBox calendarContainer = new VBox(5);
        calendarContainer.setPadding(new Insets(8));
        calendarContainer.setStyle(
            "-fx-background-color: " + YELLOW_COLOR + ";" +
            "-fx-background-radius: 6px;"
        );
        
        // Current date information
        LocalDate today = LocalDate.now();
        YearMonth yearMonth = YearMonth.from(today);
        
        // Month and year header
        Label monthYearLabel = new Label(yearMonth.getMonth().toString() + " " + yearMonth.getYear());
        monthYearLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        // Days of week header
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        GridPane daysOfWeekGrid = new GridPane();
        daysOfWeekGrid.setHgap(8);
        daysOfWeekGrid.setVgap(8);
        daysOfWeekGrid.setPadding(new Insets(4, 0, 4, 0));
        
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666666; -fx-font-size: 10px;");
            dayLabel.setPrefWidth(35);
            dayLabel.setAlignment(Pos.CENTER);
            daysOfWeekGrid.add(dayLabel, i, 0);
        }
        
        // Calendar grid
        GridPane calendarGrid = new GridPane();
        calendarGrid.setHgap(8);
        calendarGrid.setVgap(8);
        
        // Get the first day of the month
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Convert to 0-based Sunday start
        
        // Get the number of days in the month
        int daysInMonth = yearMonth.lengthOfMonth();
        
        // Fetch registered events for highlighting
        List<LocalDate> eventDates = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT e.startDate FROM Event e " +
                         "JOIN Registration r ON e.eventID = r.eventID " +
                         "WHERE r.userID = ? AND MONTH(e.startDate) = MONTH(GETDATE()) AND YEAR(e.startDate) = YEAR(GETDATE())";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, attendeeID);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String dateStr = rs.getString("startDate");
                LocalDate eventDate = LocalDate.parse(dateStr.split(" ")[0]);
                eventDates.add(eventDate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Create calendar grid
        int day = 1;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                if (row == 0 && col < dayOfWeek) {
                    // Empty cell before first day of month
                    continue;
                }
                if (day > daysInMonth) {
                    // No more days in month
                    break;
                }
                
                StackPane dayPane = new StackPane();
                dayPane.setPrefSize(35, 35);
                
                // Check if this day has an event
                LocalDate currentDate = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), day);
                boolean hasEvent = eventDates.contains(currentDate);
                
                // Check if this is today
                boolean isToday = currentDate.equals(today);
                
                Label dayNumber = new Label(String.valueOf(day));
                dayNumber.setAlignment(Pos.CENTER);
                dayNumber.setStyle("-fx-font-size: 10px;");
                
                if (hasEvent) {
                    // Highlight days with events using white background
                    dayPane.setStyle("-fx-background-color: " + WHITE_COLOR + "; -fx-background-radius: 17;");
                    dayNumber.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
                } else if (isToday) {
                    // Highlight today with blue background
                    dayPane.setStyle("-fx-background-color: " + BLUE_COLOR + "; -fx-background-radius: 17;");
                    dayNumber.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 10px;");
                } else {
                    // Regular days with transparent background
                    dayPane.setStyle("-fx-background-color: transparent;");
                }
                
                dayPane.getChildren().add(dayNumber);
                calendarGrid.add(dayPane, col, row);
                day++;
            }
        }
        
        calendarContainer.getChildren().addAll(monthYearLabel, daysOfWeekGrid, calendarGrid);
        calendarSection.getChildren().addAll(calendarLabel, calendarContainer);
        
        return calendarSection;
    }
    
    private void loadEventsFromDatabase(TableView<Event> eventTable) {
        eventTable.getItems().clear(); // Clear existing items
        
        try (Connection conn = DBConnection.getConnection()) {
            // Order by eventID DESC to show newest events first
            String sql = "SELECT eventID, name, startDate, endDate FROM Event ORDER BY eventID DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                eventTable.getItems().add(new Event(
                        rs.getInt("eventID"),
                        rs.getString("name"),
                        rs.getString("startDate"),
                        rs.getString("endDate")
                ));
            }
            
            // Auto-scroll to top to show newest events
            if (!eventTable.getItems().isEmpty()) {
                eventTable.scrollTo(0);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load events: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private void registerToEvent(int eventId, int attendeeId) {
        try (Connection conn = DBConnection.getConnection()) {
            // First check if already registered
            String checkSql = "SELECT COUNT(*) FROM Registration WHERE userID = ? AND eventID = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, attendeeId);
            checkStmt.setInt(2, eventId);
            ResultSet checkRs = checkStmt.executeQuery();
            
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Already Registered");
                alert.setHeaderText(null);
                alert.setContentText("You are already registered for this event!");
                alert.showAndWait();
                return;
            }
            
            // Get event name for notification
            String eventName = "";
            String getEventSql = "SELECT name FROM Event WHERE eventID = ?";
            PreparedStatement getEventStmt = conn.prepareStatement(getEventSql);
            getEventStmt.setInt(1, eventId);
            ResultSet eventRs = getEventStmt.executeQuery();
            if (eventRs.next()) {
                eventName = eventRs.getString("name");
            }
            
            // Step 1: Insert into Registration
            String sql = "INSERT INTO Registration (userID, eventID, registrationDate, registrationStatus) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, attendeeId);
            stmt.setInt(2, eventId);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setString(4, "Registered");
            stmt.executeUpdate();
            
            // Step 2: Insert a Notification for the user
            String notifSql = "INSERT INTO Notification (name, title, message, createdAt, notificationType, userID) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement notifStmt = conn.prepareStatement(notifSql);
            notifStmt.setString(1, "Event Registration");
            notifStmt.setString(2, "Registration Successful!");
            notifStmt.setString(3, "You have successfully registered for " + eventName + ". Check 'My Events' to view your registered events.");
            notifStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            notifStmt.setString(5, "event");
            notifStmt.setInt(6, attendeeId);
            notifStmt.executeUpdate();
            
            // Show confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText("Welcome to the Event!");
            alert.setContentText("You have successfully registered for " + eventName + "!\nA confirmation notification has been sent to your notifications panel.");
            alert.showAndWait();
            
            // Refresh the dashboard to show the new notification
            show(currentStage, attendeeId);
            
        } catch (SQLException e) {
        	e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Registration Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to register for event: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    // Event class for TableView
    public static class Event {
        private int eventID;
        private String name;
        private String startDate;
        private String endDate;
        
        public Event(int eventID, String name, String startDate, String endDate) {
            this.eventID = eventID;
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        public int getEventID() { return eventID; }
        public String getName() { return name; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        
        public void setEventID(int eventID) { this.eventID = eventID; }
        public void setName(String name) { this.name = name; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
    }
    
    // Notification class for displaying notifications
    public static class Notification {
        private int notificationID;
        private String title;
        private String message;
        private java.sql.Timestamp createdAt;
        
        public Notification(int notificationID, String title, String message, java.sql.Timestamp createdAt) {
            this.notificationID = notificationID;
            this.title = title;
            this.message = message;
            this.createdAt = createdAt;
        }
        
        public int getNotificationID() { return notificationID; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public java.sql.Timestamp getCreatedAt() { return createdAt; }
        
        public void setNotificationID(int notificationID) { this.notificationID = notificationID; }
        public void setTitle(String title) { this.title = title; }
        public void setMessage(String message) { this.message = message; }
        public void setCreatedAt(java.sql.Timestamp createdAt) { this.createdAt = createdAt; }
    }
}