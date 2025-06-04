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
import java.sql.*;

public class OrganizerEventsPage {
    // UI color constants matching organizer dashboard
    private static final String BLUE_COLOR = "#97A9D1";
    private static final String YELLOW_COLOR = "#F1D747";
    private static final String WHITE_COLOR = "#FFFFFF";
    private static final String DARK_TEXT = "#333333";
    
    private Stage currentStage;
    private int organizerID;
    private TableView<EventRow> eventsTable;

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
        Label header = new Label("My Events Management");
        header.setStyle("-fx-background-color: " + YELLOW_COLOR + ";" +
                       "-fx-padding: 12px 18px;" +
                       "-fx-background-radius: 8px;" +
                       "-fx-font-weight: bold;" +
                       "-fx-font-size: 16px;");
        
        // Events table section
        VBox tableSection = createTableSection();
        VBox.setVgrow(tableSection, Priority.ALWAYS);
        
        contentPane.getChildren().addAll(header, tableSection);
        contentWrapper.getChildren().add(contentPane);
        mainLayout.setCenter(contentWrapper);
        
        Scene scene = new Scene(mainLayout, 1200, 800);
        stage.setTitle("Eventure - My Events Management");
        stage.setScene(scene);
        stage.show();
        
        // Initial data load
        refreshData();
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
        myEventsBtn.setStyle(getActiveNavButtonStyle());
        
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
            EventListPage eventsPage = new EventListPage(false);
            eventsPage.show(currentStage, organizerID);
        });
        
        myEventsBtn.setOnAction(e -> {
            // Already on this page - do nothing or refresh
            refreshData();
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
    
    private VBox createTableSection() {
        VBox tableSection = new VBox(12);
        tableSection.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-padding: 12px;" +
            "-fx-background-radius: 8px;"
        );
        
        Label tableLabel = new Label("Events Management");
        tableLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        // Events table
        eventsTable = new TableView<>();
        eventsTable.setStyle("-fx-background-color: " + YELLOW_COLOR + "; -fx-background-radius: 8px;");
        VBox.setVgrow(eventsTable, Priority.ALWAYS);
        
        // Event Name Column
        TableColumn<EventRow, String> eventCol = new TableColumn<>("Event Name");
        eventCol.setCellValueFactory(data -> data.getValue().eventNameProperty());
        eventCol.setPrefWidth(250);
        
        // Start Date Column
        TableColumn<EventRow, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(data -> data.getValue().startDateProperty());
        startDateCol.setPrefWidth(120);
        
        // End Date Column
        TableColumn<EventRow, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(data -> data.getValue().endDateProperty());
        endDateCol.setPrefWidth(120);
        
        // Edit Column
        TableColumn<EventRow, Void> editCol = new TableColumn<>("Edit");
        editCol.setPrefWidth(80);
        editCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");

            {
                styleActionButton(editBtn);
                editBtn.setOnAction(e -> {
                    EventRow row = getTableView().getItems().get(getIndex());
                    EditEventPage eventPage = new EditEventPage();
                    eventPage.show(new Stage(), row.toEvent(), () -> refreshData());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editBtn);
            }
        });

        // Delete Column
        TableColumn<EventRow, Void> deleteCol = new TableColumn<>("Delete");
        deleteCol.setPrefWidth(80);
        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                styleDeleteButton(deleteBtn);
                deleteBtn.setOnAction(e -> {
                    EventRow row = getTableView().getItems().get(getIndex());
                    
                    // Confirmation dialog
                    Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmDialog.setTitle("Delete Event");
                    confirmDialog.setHeaderText("Delete Event: " + row.getEventName());
                    confirmDialog.setContentText("Are you sure you want to delete this event? This action cannot be undone.");
                    
                    confirmDialog.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            deleteEvent(row.getEventID());
                            refreshData();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        eventsTable.getColumns().clear();
        eventsTable.getColumns().addAll(eventCol, startDateCol, endDateCol, editCol, deleteCol);
        eventsTable.setMinHeight(400);
        
        // Action buttons section
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(8, 0, 0, 0));
        
        Button refreshBtn = new Button("Refresh");
        Button createEventBtn = new Button("Create New Event");
        
        styleActionButton(refreshBtn);
        styleActionButton(createEventBtn);
        
        refreshBtn.setOnAction(e -> refreshData());
        createEventBtn.setOnAction(e -> {
            CreateEventPage page = new CreateEventPage();
            page.show(new Stage(), organizerID);
        });
        
        buttonBox.getChildren().addAll(refreshBtn, createEventBtn);
        
        tableSection.getChildren().addAll(tableLabel, eventsTable, buttonBox);
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
    
    private void styleDeleteButton(Button button) {
        button.setStyle(
            "-fx-background-color: #FF6B6B;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 5px;" +
            "-fx-padding: 6px 12px;"
        );
        
        button.setOnMouseEntered(e ->
            button.setStyle(
                "-fx-background-color: #FF5252;" +
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
                "-fx-background-color: #FF6B6B;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 5px;" +
                "-fx-padding: 6px 12px;"
            )
        );
    }

    private void refreshData() {
        eventsTable.getItems().clear();
        ObservableList<EventRow> data = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT eventID, name, startDate, endDate FROM Event WHERE organizerID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, organizerID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int eventID = rs.getInt("eventID");
                String eventName = rs.getString("name");
                String startDate = rs.getString("startDate");
                String endDate = rs.getString("endDate");

                data.add(new EventRow(eventID, eventName, startDate, endDate));
            }
            
            // Add placeholder if no events found
            if (data.isEmpty()) {
                eventsTable.setPlaceholder(new Label("No events found. Create your first event!"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            eventsTable.setPlaceholder(new Label("Error loading events."));
        }

        eventsTable.setItems(data);
    }

    private void deleteEvent(int eventID) {
        try (Connection conn = DBConnection.getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);
            
            try {
                // Delete registrations first
                PreparedStatement regStmt = conn.prepareStatement("DELETE FROM Registration WHERE eventID = ?");
                regStmt.setInt(1, eventID);
                regStmt.executeUpdate();
                
                // Delete sessions
                PreparedStatement sessionStmt = conn.prepareStatement("DELETE FROM Session WHERE eventID = ?");
                sessionStmt.setInt(1, eventID);
                sessionStmt.executeUpdate();

                // Delete event
                PreparedStatement eventStmt = conn.prepareStatement("DELETE FROM Event WHERE eventID = ?");
                eventStmt.setInt(1, eventID);
                eventStmt.executeUpdate();
                
                // Commit transaction
                conn.commit();
                
                // Show success message
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Event Deleted");
                successAlert.setContentText("The event has been successfully deleted.");
                successAlert.showAndWait();
                
            } catch (SQLException e) {
                // Rollback on error
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Delete Failed");
            errorAlert.setContentText("Failed to delete the event. Please try again.");
            errorAlert.showAndWait();
        }
    }

    public static class EventRow {
        private final int eventID;
        private final String eventName;
        private final String startDate;
        private final String endDate;

        public EventRow(int eventID, String eventName, String startDate, String endDate) {
            this.eventID = eventID;
            this.eventName = eventName;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public int getEventID() {
            return eventID;
        }
        
        public String getEventName() {
            return eventName;
        }

        public javafx.beans.property.SimpleStringProperty eventNameProperty() {
            return new javafx.beans.property.SimpleStringProperty(eventName);
        }
        
        public javafx.beans.property.SimpleStringProperty startDateProperty() {
            return new javafx.beans.property.SimpleStringProperty(startDate);
        }
        
        public javafx.beans.property.SimpleStringProperty endDateProperty() {
            return new javafx.beans.property.SimpleStringProperty(endDate);
        }

        // Convert to your event object for passing to EditEventPage
        public MyEventsPage.Event toEvent() {
            return new MyEventsPage.Event(eventID, eventName, startDate, endDate);
        }
    }
}