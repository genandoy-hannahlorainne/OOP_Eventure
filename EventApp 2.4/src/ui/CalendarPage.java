package ui;

import db.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CalendarPage {
    // UI color constants matching AttendeeDashboard
    private static final String BLUE_COLOR = "#97A9D1";
    private static final String YELLOW_COLOR = "#F1D747";
    private static final String WHITE_COLOR = "#FFFFFF";
    private static final String DARK_TEXT = "#333333";
    
    private int attendeeID;
    private String attendeeName = "";
    
    public void show(Stage stage, int attendeeID) {
        this.attendeeID = attendeeID;
        
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
        StackPane contentWrapper = new StackPane();
        contentWrapper.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        contentWrapper.setPadding(new Insets(20));
        
        VBox contentPane = new VBox(20);
        contentPane.setPadding(new Insets(25));
        contentPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                            "-fx-background-radius: 15px;");
        
        // Page Title
        Label titleLabel = new Label("My Calendar");
        titleLabel.setStyle("-fx-font-size: 28px;" +
                           "-fx-font-weight: bold;" +
                           "-fx-text-fill: " + DARK_TEXT + ";" +
                           "-fx-padding: 0 0 10px 0;");
        
        // Main content grid
        HBox mainContent = new HBox(25);
        mainContent.setAlignment(Pos.TOP_CENTER);
        
        // Left side - Event List
        VBox eventSection = createEventSection();
        
        // Right side - Calendar
        VBox calendarSection = createCalendarSection();
        
        mainContent.getChildren().addAll(eventSection, calendarSection);
        contentPane.getChildren().addAll(titleLabel, mainContent);
        
        contentWrapper.getChildren().add(contentPane);
        mainLayout.setCenter(contentWrapper);
        
        Scene scene = new Scene(mainLayout, 1200, 800);
        stage.setTitle("Eventure - My Calendar");
        stage.setScene(scene);
        stage.show();
    }
    
    private HBox createNavBar() {
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
        Button myEventsBtn = createNavButton("My Events");
        Button notificationsBtn = createNavButton("Notification");
        Button profileBtn = createNavButton("Profile");
        
        // Spacer
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        navBar.getChildren().addAll(
            spacer1, 
            dashboardBtn, switchAccountBtn, myEventsBtn, notificationsBtn, profileBtn,
            spacer2
        );
        
        // Navigation actions
        dashboardBtn.setOnAction(e -> {
            AttendeeDashboard dashboard = new AttendeeDashboard();
            dashboard.show(new Stage(), attendeeID);
            Stage currentStage = (Stage) dashboardBtn.getScene().getWindow();
            currentStage.close();
        });
        
        switchAccountBtn.setOnAction(e -> {
            new LoginPage(new Stage());
            Stage currentStage = (Stage) switchAccountBtn.getScene().getWindow();
            currentStage.close();
        });
        
        myEventsBtn.setOnAction(e -> {
            MyEventsPage myEventsPage = new MyEventsPage();
            myEventsPage.show(new Stage(), attendeeID);
        });
        
        notificationsBtn.setOnAction(e -> {
            NotificationPage notificationPage = new NotificationPage();
            notificationPage.show(new Stage(), attendeeID);
        });
        
        profileBtn.setOnAction(e -> {
            ProfilePage profilePage = new ProfilePage();
            profilePage.show(new Stage(), attendeeID);
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
    
    private VBox createEventSection() {
        VBox eventSection = new VBox(15);
        eventSection.setPrefWidth(550);
        eventSection.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-padding: 20px;" +
            "-fx-background-radius: 12px;"
        );
        
        Label sectionTitle = new Label("Your Registered Events");
        sectionTitle.setStyle("-fx-font-size: 18px;" +
                             "-fx-font-weight: bold;" +
                             "-fx-text-fill: white;");
        
        // Custom styled ListView
        ListView<String> eventListView = new ListView<>();
        eventListView.setPrefHeight(400);
        eventListView.setStyle(
            "-fx-background-color: " + WHITE_COLOR + ";" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: transparent;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;"
        );
        
        ObservableList<String> eventNames = FXCollections.observableArrayList();
        eventListView.setItems(eventNames);
        
        // Custom cell factory for better styling
        eventListView.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // Parse event info
                    String[] parts = item.split(" - ");
                    String eventName = parts[0];
                    String dateStr = parts.length > 1 ? parts[1] : "";
                    
                    VBox cellContent = new VBox(3);
                    cellContent.setPadding(new Insets(8));
                    
                    Label nameLabel = new Label(eventName);
                    nameLabel.setStyle("-fx-font-weight: bold;" +
                                     "-fx-font-size: 14px;" +
                                     "-fx-text-fill: " + DARK_TEXT + ";");
                    
                    Label dateLabel = new Label("Date: " + formatDate(dateStr));
                    dateLabel.setStyle("-fx-font-size: 12px;" +
                                     "-fx-text-fill: #666666;");
                    
                    cellContent.getChildren().addAll(nameLabel, dateLabel);
                    setGraphic(cellContent);
                    setText(null);
                    
                    // Styling for selection
                    setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                            "-fx-border-color: #E0E0E0;" +
                            "-fx-border-width: 0 0 1 0;" +
                            "-fx-padding: 5px;");
                }
            }
        });
        
        // Fetch registered events from DB
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT E.name, E.startDate
                FROM Event E
                JOIN Registration R ON E.eventID = R.eventID
                WHERE R.userID = ?
                ORDER BY E.startDate ASC
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, attendeeID);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String name = rs.getString("name");
                String date = rs.getString("startDate");
                eventNames.add(name + " - " + date);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // No events message
        if (eventNames.isEmpty()) {
            Label noEventsLabel = new Label("No registered events found");
            noEventsLabel.setStyle("-fx-font-style: italic;" +
                                  "-fx-text-fill: white;" +
                                  "-fx-font-size: 14px;");
            eventSection.getChildren().addAll(sectionTitle, noEventsLabel);
        } else {
            eventSection.getChildren().addAll(sectionTitle, eventListView);
        }
        
        return eventSection;
    }
    
    private VBox createCalendarSection() {
        VBox calendarSection = new VBox(15);
        calendarSection.setPrefWidth(550);
        calendarSection.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-padding: 20px;" +
            "-fx-background-radius: 12px;"
        );
        
        Label sectionTitle = new Label("Event Calendar");
        sectionTitle.setStyle("-fx-font-size: 18px;" +
                             "-fx-font-weight: bold;" +
                             "-fx-text-fill: white;");
        
        // Calendar container
        VBox calendarContainer = new VBox(10);
        calendarContainer.setPadding(new Insets(15));
        calendarContainer.setStyle(
            "-fx-background-color: " + YELLOW_COLOR + ";" +
            "-fx-background-radius: 10px;"
        );
        
        // Date picker with custom styling
        DatePicker calendar = new DatePicker();
        calendar.setEditable(false);
        calendar.setValue(LocalDate.now());
        calendar.setPrefWidth(500);
        calendar.setStyle(
            "-fx-background-color: " + WHITE_COLOR + ";" +
            "-fx-border-color: #CCCCCC;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 8px;"
        );
        
        // Selected event info
        VBox eventInfo = new VBox(8);
        eventInfo.setPadding(new Insets(10, 0, 0, 0));
        
        Label selectedEventLabel = new Label("Selected Event:");
        selectedEventLabel.setStyle("-fx-font-weight: bold;" +
                                   "-fx-font-size: 14px;" +
                                   "-fx-text-fill: " + DARK_TEXT + ";");
        
        Label eventDetails = new Label("Select an event from the list to view details");
        eventDetails.setStyle("-fx-font-size: 12px;" +
                             "-fx-text-fill: #666666;" +
                             "-fx-wrap-text: true;");
        eventDetails.setWrapText(true);
        eventDetails.setMaxWidth(480);
        
        eventInfo.getChildren().addAll(selectedEventLabel, eventDetails);
        
        // Get event list reference for interaction
        ListView<String> eventListView = getEventListView();
        if (eventListView != null) {
            eventListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains(" - ")) {
                    String[] parts = newVal.split(" - ");
                    if (parts.length == 2) {
                        try {
                            LocalDate eventDate = LocalDate.parse(parts[1].split(" ")[0]);
                            calendar.setValue(eventDate);
                            
                            // Update event details
                            eventDetails.setText("Event: " + parts[0] + "\nDate: " + formatDate(parts[1]));
                        } catch (Exception e) {
                            System.err.println("Invalid date format: " + parts[1]);
                        }
                    }
                }
            });
        }
        
        calendarContainer.getChildren().addAll(calendar, eventInfo);
        calendarSection.getChildren().addAll(sectionTitle, calendarContainer);
        
        return calendarSection;
    }
    
    private ListView<String> getEventListView() {
        // This is a helper method to get reference to the event list
        // In a real implementation, you might want to store this as a class field
        return null;
    }
    
    private String formatDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr.split(" ")[0]);
            return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } catch (Exception e) {
            return dateStr;
        }
    }
}