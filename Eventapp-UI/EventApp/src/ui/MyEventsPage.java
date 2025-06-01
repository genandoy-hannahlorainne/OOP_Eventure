package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MyEventsPage {
    // UI color constants matching other pages
    private static final String BLUE_COLOR = "#97A9D1";
    private static final String YELLOW_COLOR = "#F1D747";
    private static final String WHITE_COLOR = "#FFFFFF";
    private static final String DARK_TEXT = "#333333";
    private static final String SUCCESS_COLOR = "#4CAF50";
    private static final String DANGER_COLOR = "#F44336";
    
    private NotificationPage notificationPage = new NotificationPage();
    private int attendeeID;
    private String attendeeName = "";
    
    public void show(Stage stage, int userID) {
        this.attendeeID = userID;
        
        // Fetch attendee name from database
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT firstName, lastName FROM [User] WHERE userID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userID);
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
        HBox navBar = createNavBar(stage);
        mainLayout.setTop(navBar);
        
        // --- Main Content ---
        StackPane contentWrapper = new StackPane();
        contentWrapper.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        contentWrapper.setPadding(new Insets(20));
        
        VBox contentPane = new VBox(25);
        contentPane.setPadding(new Insets(30));
        contentPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                            "-fx-background-radius: 15px;");
        
        // Page Header
        VBox headerSection = createHeaderSection();
        
        // Events Table Section
        VBox tableSection = createTableSection(userID);
        
        contentPane.getChildren().addAll(headerSection, tableSection);
        contentWrapper.getChildren().add(contentPane);
        mainLayout.setCenter(contentWrapper);
        
        Scene scene = new Scene(mainLayout, 1200, 800);
        stage.setTitle("Eventure - My Registered Events");
        stage.setScene(scene);
        stage.show();
    }
    
    private HBox createNavBar(Stage stage) {
        HBox navBar = new HBox(12);
        navBar.setPadding(new Insets(8, 15, 8, 15));
        navBar.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        navBar.setAlignment(Pos.CENTER_LEFT);
        
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
            navBar.getChildren().add(logoContainer);
        } catch (Exception e) {
            StackPane logoContainer = new StackPane();
            Circle whiteCircle = new Circle(20);
            whiteCircle.setFill(Color.WHITE);
            
            Label logoPlaceholder = new Label("E");
            logoPlaceholder.setStyle("-fx-text-fill: " + BLUE_COLOR + "; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            logoContainer.getChildren().addAll(whiteCircle, logoPlaceholder);
            navBar.getChildren().add(logoContainer);
        }
        
        // Spacer
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        // Navigation Buttons
        Button dashboardBtn = createNavButton("Dashboard");
        Button switchAccountBtn = createNavButton("Switch Account");
        Button calendarBtn = createNavButton("Calendar");
        Button notificationsBtn = createNavButton("Notification");
        Button profileBtn = createNavButton("Profile");
        
        // Spacer
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        navBar.getChildren().addAll(
            spacer1, 
            dashboardBtn, switchAccountBtn, calendarBtn, notificationsBtn, profileBtn,
            spacer2
        );
        
        // Navigation actions - all using the same stage
        dashboardBtn.setOnAction(e -> {
            AttendeeDashboard dashboard = new AttendeeDashboard();
            dashboard.show(stage, attendeeID);
        });
        
        switchAccountBtn.setOnAction(e -> {
            new LoginPage(stage);
        });
        
        calendarBtn.setOnAction(e -> {
            CalendarPage calendarPage = new CalendarPage();
            calendarPage.show(stage, attendeeID);
        });
        
        notificationsBtn.setOnAction(e -> {
            NotificationPage notificationPage = new NotificationPage();
            notificationPage.show(stage, attendeeID);
        });
        
        profileBtn.setOnAction(e -> {
            ProfilePage profilePage = new ProfilePage();
            profilePage.show(stage, attendeeID);
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
    
    private VBox createHeaderSection() {
        VBox headerSection = new VBox(10);
        
        // Main title
        Label titleLabel = new Label("My Registered Events");
        titleLabel.setStyle("-fx-font-size: 28px;" +
                           "-fx-font-weight: bold;" +
                           "-fx-text-fill: " + DARK_TEXT + ";");
        
        // Welcome message
        Label welcomeLabel = new Label("Welcome back, " + attendeeName + "!");
        welcomeLabel.setStyle("-fx-font-size: 16px;" +
                             "-fx-text-fill: #666666;" +
                             "-fx-padding: 0 0 5px 0;");
        
        // Stats banner
        HBox statsBox = createStatsBox();
        
        headerSection.getChildren().addAll(titleLabel, welcomeLabel, statsBox);
        return headerSection;
    }
    
    private HBox createStatsBox() {
        HBox statsBox = new HBox(20);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle("-fx-background-color: " + YELLOW_COLOR + ";" +
                         "-fx-background-radius: 10px;");
        statsBox.setAlignment(Pos.CENTER_LEFT);
        
        // Get event count
        int eventCount = getRegisteredEventCount();
        int upcomingCount = getUpcomingEventCount();
        
        VBox totalEventsBox = new VBox(2);
        totalEventsBox.setAlignment(Pos.CENTER);
        Label totalCountLabel = new Label(String.valueOf(eventCount));
        totalCountLabel.setStyle("-fx-font-size: 24px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-text-fill: " + DARK_TEXT + ";");
        Label totalTextLabel = new Label("Total Events");
        totalTextLabel.setStyle("-fx-font-size: 12px;" +
                               "-fx-text-fill: #666666;");
        totalEventsBox.getChildren().addAll(totalCountLabel, totalTextLabel);
        
        // Separator
        Region separator1 = new Region();
        separator1.setPrefWidth(2);
        separator1.setStyle("-fx-background-color: #CCCCCC;");
        
        VBox upcomingEventsBox = new VBox(2);
        upcomingEventsBox.setAlignment(Pos.CENTER);
        Label upcomingCountLabel = new Label(String.valueOf(upcomingCount));
        upcomingCountLabel.setStyle("-fx-font-size: 24px;" +
                                   "-fx-font-weight: bold;" +
                                   "-fx-text-fill: " + SUCCESS_COLOR + ";");
        Label upcomingTextLabel = new Label("Upcoming Events");
        upcomingTextLabel.setStyle("-fx-font-size: 12px;" +
                                  "-fx-text-fill: #666666;");
        upcomingEventsBox.getChildren().addAll(upcomingCountLabel, upcomingTextLabel);
        
        statsBox.getChildren().addAll(totalEventsBox, separator1, upcomingEventsBox);
        return statsBox;
    }
    
    private VBox createTableSection(int userID) {
        VBox tableSection = new VBox(15);
        
        // Table container with blue background
        VBox tableContainer = new VBox(15);
        tableContainer.setPadding(new Insets(20));
        tableContainer.setStyle("-fx-background-color: " + BLUE_COLOR + ";" +
                               "-fx-background-radius: 12px;");
        
        Label tableTitle = new Label("Event Details");
        tableTitle.setStyle("-fx-font-size: 18px;" +
                           "-fx-font-weight: bold;" +
                           "-fx-text-fill: white;");
        
        // Create styled table
        TableView<Event> table = createStyledTable();
        loadRegisteredEvents(userID, table);
        
        // Add columns
        setupTableColumns(table, userID);
        
        tableContainer.getChildren().addAll(tableTitle, table);
        tableSection.getChildren().add(tableContainer);
        
        return tableSection;
    }
    
    private TableView<Event> createStyledTable() {
        TableView<Event> table = new TableView<>();
        table.setPrefHeight(400);
        table.setStyle(
            "-fx-background-color: " + WHITE_COLOR + ";" +
            "-fx-border-color: transparent;" +
            "-fx-background-radius: 8px;" +
            "-fx-table-cell-border-color: #E0E0E0;"
        );
        
        // Custom row factory for alternating colors
        table.setRowFactory(tv -> {
            TableRow<Event> row = new TableRow<>();
            row.itemProperty().addListener((obs, previousItem, currentItem) -> {
                if (currentItem == null) {
                    row.setStyle("");
                } else {
                    if (row.getIndex() % 2 == 0) {
                        row.setStyle("-fx-background-color: #F8F9FA;");
                    } else {
                        row.setStyle("-fx-background-color: " + WHITE_COLOR + ";");
                    }
                }
            });
            return row;
        });
        
        return table;
    }
    
    private void setupTableColumns(TableView<Event> table, int userID) {
        // Event Name Column
        TableColumn<Event, String> nameCol = new TableColumn<>("Event Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(300);
        nameCol.setStyle("-fx-alignment: CENTER-LEFT;");
        
        // Start Date Column
        TableColumn<Event, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startDateCol.setPrefWidth(150);
        startDateCol.setCellFactory(col -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatDate(item));
                }
            }
        });
        
        // End Date Column
        TableColumn<Event, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        endDateCol.setPrefWidth(150);
        endDateCol.setCellFactory(col -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatDate(item));
                }
            }
        });
        
        // Status Column
        TableColumn<Event, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(col -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Event event = getTableView().getItems().get(getIndex());
                    if (event != null) {
                        boolean isUpcoming = isEventUpcoming(event.getStartDate());
                        Label statusLabel = new Label(isUpcoming ? "Upcoming" : "Past");
                        statusLabel.setStyle(
                            "-fx-background-color: " + (isUpcoming ? SUCCESS_COLOR : "#95A5A6") + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 4px 8px;" +
                            "-fx-background-radius: 12px;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;"
                        );
                        setGraphic(statusLabel);
                        setText(null);
                    }
                }
            }
        });
        
        // Cancel Button Column
        TableColumn<Event, Void> cancelCol = new TableColumn<>("Action");
        cancelCol.setPrefWidth(100);
        cancelCol.setCellFactory(col -> new TableCell<>() {
            private final Button cancelBtn = new Button("Cancel");
            
            {
                cancelBtn.setStyle(
                    "-fx-background-color: " + DANGER_COLOR + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 11px;" +
                    "-fx-padding: 6px 12px;" +
                    "-fx-cursor: hand;" +
                    "-fx-background-radius: 6px;"
                );
                
                cancelBtn.setOnMouseEntered(e ->
                    cancelBtn.setStyle(
                        "-fx-background-color: #D32F2F;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 6px 12px;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-radius: 6px;"
                    )
                );
                
                cancelBtn.setOnMouseExited(e ->
                    cancelBtn.setStyle(
                        "-fx-background-color: " + DANGER_COLOR + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 6px 12px;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-radius: 6px;"
                    )
                );
                
                cancelBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    showCancellationDialog(userID, event, table);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Event event = getTableView().getItems().get(getIndex());
                    // Only show cancel button for upcoming events
                    if (event != null && isEventUpcoming(event.getStartDate())) {
                        setGraphic(cancelBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        
        table.getColumns().addAll(nameCol, startDateCol, endDateCol, statusCol, cancelCol);
    }
    
    private void showCancellationDialog(int userID, Event event, TableView<Event> table) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Registration");
        confirm.setHeaderText("Cancel Event Registration");
        confirm.setContentText("Are you sure you want to cancel your registration for '" + event.getName() + "'?\n\nThis action cannot be undone.");
        
        // Style the dialog
        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                cancelRegistration(userID, event.getEventID(), event.getName());
                table.getItems().remove(event);
                
                // Show success message
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("Registration cancelled successfully!");
                success.showAndWait();
                
                // Refresh the table
                loadRegisteredEvents(userID, table);
            }
        });
    }
    
    private void loadRegisteredEvents(int userID, TableView<Event> table) {
        table.getItems().clear();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT E.eventID, E.name, E.startDate, E.endDate
                FROM Event E
                JOIN Registration R ON E.eventID = R.eventID
                WHERE R.userID = ?
                ORDER BY E.startDate ASC
                """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                table.getItems().add(new Event(
                        rs.getInt("eventID"),
                        rs.getString("name"),
                        rs.getString("startDate"),
                        rs.getString("endDate")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load registered events: " + e.getMessage());
        }
    }
    
    private int getRegisteredEventCount() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM Registration WHERE userID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, attendeeID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private int getUpcomingEventCount() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT COUNT(*)
                FROM Event E
                JOIN Registration R ON E.eventID = R.eventID
                WHERE R.userID = ? AND E.startDate >= GETDATE()
                """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, attendeeID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private boolean isEventUpcoming(String startDate) {
        try {
            LocalDate eventDate = LocalDate.parse(startDate.split(" ")[0]);
            return eventDate.isAfter(LocalDate.now()) || eventDate.isEqual(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }
    
    private String formatDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr.split(" ")[0]);
            return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } catch (Exception e) {
            return dateStr;
        }
    }
    
    private void cancelRegistration(int userID, int eventID, String eventName) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM Registration WHERE userID = ? AND eventID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userID);
            stmt.setInt(2, eventID);
            int affected = stmt.executeUpdate();
            
            if (affected > 0) {
                notificationPage.insertNotification(
                        userID, eventName, "Event Cancellation",
                        "You canceled your registration for event '" + eventName + "'.", "cancellation"
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to cancel registration: " + e.getMessage());
        }
    }
    
    public void registerForEvent(int userID, int eventID, String eventName) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO Registration (userID, eventID) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userID);
            stmt.setInt(2, eventID);
            int affected = stmt.executeUpdate();
            
            if (affected > 0) {
                notificationPage.insertNotification(
                        userID, eventName, "Event Registration",
                        "You successfully registered for event '" + eventName + "'.", "registration"
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to register for event: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the error dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";");
        
        alert.showAndWait();
    }
    
    public static class Event {
        private final int eventID;
        private final String name;
        private final String startDate;
        private final String endDate;
        
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
    }
}