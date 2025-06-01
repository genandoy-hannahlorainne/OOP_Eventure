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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewEventsPage {
    // UI color constants matching organizer dashboard
    private static final String BLUE_COLOR = "#97A9D1";  // Nav bar and main background color
    private static final String YELLOW_COLOR = "#F1D747"; // Event cards and welcome banner
    private static final String WHITE_COLOR = "#FFFFFF";
    private static final String DARK_TEXT = "#333333";
    
    private Stage currentStage;
    
    public void show(Stage stage, int organizerID) {
        this.currentStage = stage;
        
        // Main layout
        BorderPane mainLayout = new BorderPane();
        
        // --- Top Navigation Bar ---
        HBox navBar = createNavBar();
        mainLayout.setTop(navBar);
        
        // --- Main Content ---
        // Create a blue background pane for the content
        StackPane contentWrapper = new StackPane();
        contentWrapper.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        contentWrapper.setPadding(new Insets(20));
        
        VBox contentPane = new VBox(20);
        contentPane.setPadding(new Insets(25));
        contentPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                            "-fx-background-radius: 10px;");
        
        // Title Banner
        Label titleLabel = new Label("Your Events");
        titleLabel.setStyle("-fx-background-color: " + YELLOW_COLOR + ";" +
                           "-fx-padding: 15px 20px;" +
                           "-fx-background-radius: 8px;" +
                           "-fx-font-weight: bold;" +
                           "-fx-font-size: 18px;" +
                           "-fx-text-fill: " + DARK_TEXT + ";");
        
        // Events Section
        VBox eventsSection = createEventsSection(organizerID);
        VBox.setVgrow(eventsSection, Priority.ALWAYS);
        
        contentPane.getChildren().addAll(titleLabel, eventsSection);
        contentWrapper.getChildren().add(contentPane);
        mainLayout.setCenter(contentWrapper);
        
        Scene scene = new Scene(mainLayout, 900, 600);
        stage.setTitle("Eventure - Your Events");
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
        
        // Page title in center
        Label pageTitle = new Label("Event Management");
        pageTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Back button - right aligned
        Button backBtn = createNavButton("← Back to Dashboard");
        backBtn.setOnAction(e -> currentStage.close());
        
        // Create spacers for proper distribution
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        
        navBar.getChildren().addAll(logoSection, leftSpacer, pageTitle, rightSpacer, backBtn);
        
        return navBar;
    }
    
    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + WHITE_COLOR + ";" +
            "-fx-text-fill: #333333;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 6px 12px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 12px;"
        );
        button.setOnMouseEntered(e ->
            button.setStyle(
                "-fx-background-color: " + YELLOW_COLOR + ";" +
                "-fx-text-fill: #333333;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                "-fx-padding: 6px 12px;" +
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
                "-fx-padding: 6px 12px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 12px;"
            )
        );
        return button;
    }
    
    private VBox createEventsSection(int organizerID) {
        VBox eventsSection = new VBox(15);
        eventsSection.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-padding: 15px;" +
            "-fx-background-radius: 8px;"
        );
        
        // Section header with event count
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label sectionLabel = new Label("Your Created Events");
        sectionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");
        
        Label countLabel = new Label();
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-padding: 0 0 0 10px;");
        
        headerBox.getChildren().addAll(sectionLabel, countLabel);
        
        // Events table with styling
        TableView<Event> eventsTable = new TableView<>();
        eventsTable.setStyle("-fx-background-color: " + YELLOW_COLOR + "; -fx-background-radius: 8px;");
        eventsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(eventsTable, Priority.ALWAYS);
        
        // Event ID column
        TableColumn<Event, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("eventID"));
        idColumn.setPrefWidth(60);
        idColumn.setStyle("-fx-font-weight: bold;");
        
        // Name column
        TableColumn<Event, String> nameColumn = new TableColumn<>("Event Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);
        nameColumn.setStyle("-fx-font-weight: bold;");
        
        // Start Date column
        TableColumn<Event, String> startDateColumn = new TableColumn<>("Start Date");
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startDateColumn.setPrefWidth(120);
        
        // End Date column
        TableColumn<Event, String> endDateColumn = new TableColumn<>("End Date");
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        endDateColumn.setPrefWidth(120);
        
        // Location column
        TableColumn<Event, String> locationColumn = new TableColumn<>("Location");
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        locationColumn.setPrefWidth(180);
        
        // Actions column
        TableColumn<Event, Void> actionColumn = new TableColumn<>("Actions");
        actionColumn.setPrefWidth(150);
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(8, editBtn, deleteBtn);
            
            {
                box.setAlignment(Pos.CENTER);
                styleActionButton(editBtn, false);
                styleActionButton(deleteBtn, true);
                
                editBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    showEditDialog(event);
                });
                
                deleteBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(event, eventsTable, countLabel, organizerID);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
        
        eventsTable.getColumns().addAll(idColumn, nameColumn, startDateColumn, endDateColumn, locationColumn, actionColumn);
        
        // Load data and update count
        refreshTable(eventsTable, countLabel, organizerID);
        
        // Action buttons section
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button refreshBtn = new Button("🔄 Refresh");
        Button createBtn = new Button("➕ Create New Event");
        
        styleActionButton(refreshBtn, false);
        styleActionButton(createBtn, false);
        
        refreshBtn.setOnAction(e -> refreshTable(eventsTable, countLabel, organizerID));
        createBtn.setOnAction(e -> showCreateEventDialog(eventsTable, countLabel, organizerID));
        
        buttonBox.getChildren().addAll(refreshBtn, createBtn);
        
        eventsSection.getChildren().addAll(headerBox, eventsTable, buttonBox);
        return eventsSection;
    }
    
    private void styleActionButton(Button button, boolean isDanger) {
        String bgColor = isDanger ? "#FF6B6B" : WHITE_COLOR;
        String textColor = isDanger ? WHITE_COLOR : DARK_TEXT;
        String hoverBg = isDanger ? "#FF5252" : BLUE_COLOR;
        String hoverText = isDanger ? WHITE_COLOR : WHITE_COLOR;
        
        button.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 11px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 5px;" +
            "-fx-padding: 6px 12px;"
        );
        
        button.setOnMouseEntered(e ->
            button.setStyle(
                "-fx-background-color: " + hoverBg + ";" +
                "-fx-text-fill: " + hoverText + ";" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 5px;" +
                "-fx-padding: 6px 12px;"
            )
        );
        
        button.setOnMouseExited(e ->
            button.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-text-fill: " + textColor + ";" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 5px;" +
                "-fx-padding: 6px 12px;"
            )
        );
    }
    
    private void refreshTable(TableView<Event> table, Label countLabel, int organizerID) {
        table.getItems().clear();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT eventID, name, startDate, endDate, location FROM Event WHERE organizerID = ? ORDER BY startDate DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, organizerID);
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                int eventID = rs.getInt("eventID");
                String name = rs.getString("name");
                String startDate = rs.getString("startDate");
                String endDate = rs.getString("endDate");
                String location = rs.getString("location");
                table.getItems().add(new Event(eventID, name, startDate, endDate, location));
                count++;
            }
            
            countLabel.setText("(" + count + " events)");
            
            if (count == 0) {
                table.setPlaceholder(new Label("No events created yet. Click 'Create New Event' to get started!"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showStyledAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load events: " + e.getMessage());
        }
    }
    
    private void showDeleteConfirmation(Event event, TableView<Event> table, Label countLabel, int organizerID) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Event");
        confirmation.setHeaderText("Are you sure you want to delete this event?");
        confirmation.setContentText("Event: " + event.getName() + "\nLocation: " + event.getLocation() + 
                                   "\n\nThis action cannot be undone and will remove all associated registrations.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteEvent(event.getEventID());
                refreshTable(table, countLabel, organizerID);
                showStyledAlert(Alert.AlertType.INFORMATION, "Success", "Event deleted successfully.");
            }
        });
    }
    
    private void deleteEvent(int eventID) {
        try (Connection conn = DBConnection.getConnection()) {
            // First delete all registrations for this event
            String deleteRegistrations = "DELETE FROM Registration WHERE eventID = ?";
            PreparedStatement regStmt = conn.prepareStatement(deleteRegistrations);
            regStmt.setInt(1, eventID);
            regStmt.executeUpdate();
            
            // Then delete the event
            String deleteEvent = "DELETE FROM Event WHERE eventID = ?";
            PreparedStatement eventStmt = conn.prepareStatement(deleteEvent);
            eventStmt.setInt(1, eventID);
            eventStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showStyledAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete event: " + e.getMessage());
        }
    }
    
    private void showEditDialog(Event event) {
        showStyledAlert(Alert.AlertType.INFORMATION, "Edit Event", 
                       "Edit functionality will be implemented soon.\nThis will allow you to modify event details.");
    }
    
    private void showCreateEventDialog(TableView<Event> table, Label countLabel, int organizerID) {
        showStyledAlert(Alert.AlertType.INFORMATION, "Create Event", 
                       "Create event functionality will be implemented soon.\nThis will allow you to create new events.");
    }
    
    private void showStyledAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: " + DARK_TEXT + "; -fx-font-size: 12px;");
        
        alert.showAndWait();
    }
    
    // ✅ Updated Event class
    public static class Event {
        private final int eventID;
        private final String name;
        private final String startDate;
        private final String endDate;
        private final String location;
        
        public Event(int eventID, String name, String startDate, String endDate, String location) {
            this.eventID = eventID;
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
            this.location = location;
        }
        
        public int getEventID() {
            return eventID;
        }
        
        public String getName() {
            return name;
        }
        
        public String getStartDate() {
            return startDate;
        }
        
        public String getEndDate() {
            return endDate;
        }
        
        public String getLocation() {
            return location;
        }
    }
}