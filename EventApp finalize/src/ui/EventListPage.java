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

public class EventListPage {
    // UI color constants matching organizer dashboard
    private static final String BLUE_COLOR = "#97A9D1";
    private static final String YELLOW_COLOR = "#F1D747";
    private static final String WHITE_COLOR = "#FFFFFF";
    private static final String DARK_TEXT = "#333333";
    
    private boolean isMyEvents;
    private Stage currentStage;
    private int organizerID;
    private TableView<Event> eventTable;

    public EventListPage(boolean isMyEvents) {
        this.isMyEvents = isMyEvents;
    }

    public void show(Stage stage, int organizerID) {
        this.currentStage = stage;
        this.organizerID = organizerID;
        
        // Main layout
        BorderPane mainLayout = new BorderPane();
        
        // --- Top Navigation Bar ---
        HBox navBar = createNavBar();
        mainLayout.setTop(navBar);
        
        // --- Main Content ---
        StackPane contentWrapper = new StackPane();
        contentWrapper.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        contentWrapper.setPadding(new Insets(15));
        
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                            "-fx-background-radius: 10px;");
        
        // Header section
        Label header = new Label(isMyEvents ? "My Events" : "All Events");
        header.setStyle("-fx-background-color: " + YELLOW_COLOR + ";" +
                       "-fx-padding: 12px 18px;" +
                       "-fx-background-radius: 8px;" +
                       "-fx-font-weight: bold;" +
                       "-fx-font-size: 16px;");
        
        // Toggle buttons section
        HBox toggleSection = createToggleSection();
        
        // Events table section
        VBox tableSection = createTableSection();
        VBox.setVgrow(tableSection, Priority.ALWAYS);
        
        contentPane.getChildren().addAll(header, toggleSection, tableSection);
        contentWrapper.getChildren().add(contentPane);
        mainLayout.setCenter(contentWrapper);
        
        Scene scene = new Scene(mainLayout, 1200, 800);
        stage.setTitle("Eventure - " + (isMyEvents ? "My Events" : "All Events"));
        stage.setScene(scene);
        stage.show();
        
        // Initial data load
        loadEvents(true);
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
        if (isMyEvents) {
            myEventsBtn.setStyle(getActiveNavButtonStyle());
        } else {
            eventsBtn.setStyle(getActiveNavButtonStyle());
        }
        
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
            dashboard.show(currentStage, organizerID);
        });
        
        eventsBtn.setOnAction(e -> {
            if (!isMyEvents) return; // Already on this page
            EventListPage eventsPage = new EventListPage(false);
            eventsPage.show(currentStage, organizerID);
        });
        
        myEventsBtn.setOnAction(e -> {
            if (isMyEvents) return; // Already on this page
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
                    page.show(new Stage(), id, isMyEvents);
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
        
        switchAccountBtn.setOnAction(e -> {
            new LoginPage(new Stage());
            currentStage.close();
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
    
    private HBox createToggleSection() {
        HBox toggleSection = new HBox(15);
        toggleSection.setAlignment(Pos.CENTER_LEFT);
        toggleSection.setPadding(new Insets(10, 0, 10, 0));
        
        Label filterLabel = new Label("Filter Events:");
        filterLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton upcomingBtn = new RadioButton("Upcoming Events");
        RadioButton pastBtn = new RadioButton("Past Events");
        
        upcomingBtn.setToggleGroup(toggleGroup);
        pastBtn.setToggleGroup(toggleGroup);
        upcomingBtn.setSelected(true);
        
        // Style radio buttons
        String radioStyle = "-fx-font-size: 12px; -fx-font-weight: bold;";
        upcomingBtn.setStyle(radioStyle);
        pastBtn.setStyle(radioStyle);
        
        // Event handlers
        upcomingBtn.setOnAction(e -> loadEvents(true));
        pastBtn.setOnAction(e -> loadEvents(false));
        
        toggleSection.getChildren().addAll(filterLabel, upcomingBtn, pastBtn);
        return toggleSection;
    }
    
    private VBox createTableSection() {
        VBox tableSection = new VBox(12);
        tableSection.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-padding: 12px;" +
            "-fx-background-radius: 8px;"
        );
        
        Label tableLabel = new Label("Events List");
        tableLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        // Events table
        eventTable = new TableView<>();
        eventTable.setStyle("-fx-background-color: " + YELLOW_COLOR + "; -fx-background-radius: 8px;");
        VBox.setVgrow(eventTable, Priority.ALWAYS);
        
        TableColumn<Event, Integer> idCol = new TableColumn<>("Event ID");
        idCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("eventID"));
        idCol.setPrefWidth(80);
        
        TableColumn<Event, String> nameCol = new TableColumn<>("Event Name");
        nameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("eventName"));
        nameCol.setPrefWidth(300);
        
        TableColumn<Event, String> startCol = new TableColumn<>("Start Date");
        startCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("startDate"));
        startCol.setPrefWidth(150);
        
        TableColumn<Event, String> endCol = new TableColumn<>("End Date");
        endCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("endDate"));
        endCol.setPrefWidth(150);
        
        eventTable.getColumns().addAll(idCol, nameCol, startCol, endCol);
        eventTable.setMinHeight(400);
        
        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(8, 0, 0, 0));
        
        Button viewDetailsBtn = new Button("View Details");
        Button refreshBtn = new Button("Refresh");
        
        styleActionButton(viewDetailsBtn);
        styleActionButton(refreshBtn);
        
        viewDetailsBtn.setDisable(true);
        eventTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            viewDetailsBtn.setDisable(newSel == null);
        });
        
        viewDetailsBtn.setOnAction(e -> {
            Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
            if (selectedEvent != null) {
                showEventDetails(selectedEvent);
            }
        });
        
        refreshBtn.setOnAction(e -> {
            // Determine which filter is currently selected
            loadEvents(true); // You might want to track the current filter state
        });
        
        buttonBox.getChildren().addAll(viewDetailsBtn, refreshBtn);
        
        tableSection.getChildren().addAll(tableLabel, eventTable, buttonBox);
        return tableSection;
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

    private void loadEvents(boolean upcoming) {
        eventTable.getItems().clear();
        String sql = "SELECT eventID, name, startDate, endDate FROM Event WHERE ";

        if (isMyEvents) {
            sql += "organizerID = ? AND ";
        }

        if (upcoming) {
            sql += "startDate >= GETDATE()";
        } else {
            sql += "startDate < GETDATE()";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (isMyEvents) {
                stmt.setInt(1, organizerID);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                eventTable.getItems().add(new Event(
                        rs.getInt("eventID"),
                        rs.getString("name"),
                        rs.getString("startDate"),
                        rs.getString("endDate")
                ));
            }

            // Add placeholder if no events found
            if (eventTable.getItems().isEmpty()) {
                String message = upcoming ? "No upcoming events found." : "No past events found.";
                eventTable.setPlaceholder(new Label(message));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            eventTable.setPlaceholder(new Label("Error loading events."));
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

    // Event class
    public static class Event {
        private int eventID;
        private String eventName;
        private String startDate;
        private String endDate;

        public Event(int eventID, String eventName, String startDate, String endDate) {
            this.eventID = eventID;
            this.eventName = eventName;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public int getEventID() { return eventID; }
        public String getEventName() { return eventName; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
    }
}