package ui;

import db.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class ViewAttendeesPage {

    public void show(Stage stage, int eventID, boolean isOwner) {
        Label title = new Label("Attendees for Event " + eventID);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<Attendee> attendeesTable = new TableView<>();
        TableColumn<Attendee, String> nameColumn = new TableColumn<>("Name");
        TableColumn<Attendee, String> emailColumn = new TableColumn<>("Email");

        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));

        attendeesTable.getColumns().addAll(nameColumn, emailColumn);

        if (isOwner) {
            TableColumn<Attendee, Void> actionColumn = new TableColumn<>("Actions");

            actionColumn.setCellFactory(col -> new TableCell<>() {
                private final Button editBtn = new Button("Edit");
                private final Button removeBtn = new Button("Remove");
                private final HBox box = new HBox(5, editBtn, removeBtn);

                {
                    editBtn.setOnAction(e -> {
                        Attendee attendee = getTableView().getItems().get(getIndex());
                        showEditDialog(attendee, eventID, () -> refreshTable(attendeesTable, eventID));
                    });

                    removeBtn.setOnAction(e -> {
                        Attendee attendee = getTableView().getItems().get(getIndex());
                        removeAttendee(eventID, attendee.getEmail());
                        refreshTable(attendeesTable, eventID);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) setGraphic(null);
                    else setGraphic(box);
                }
            });

            attendeesTable.getColumns().add(actionColumn);
        }

        refreshTable(attendeesTable, eventID);

        VBox layout = new VBox(15, title, attendeesTable);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(layout, 600, 400);
        stage.setTitle("View Attendees");
        stage.setScene(scene);
        stage.show();
    }

    private void refreshTable(TableView<Attendee> table, int eventID) {
        table.getItems().clear();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT u.name, u.email FROM Registration r " +
                         "JOIN [User] u ON r.userID = u.userID WHERE r.eventID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, eventID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                table.getItems().add(new Attendee(rs.getString("name"), rs.getString("email")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeAttendee(int eventID, String email) {
        try (Connection conn = DBConnection.getConnection()) {
            String getUserID = "SELECT userID FROM [User] WHERE email = ?";
            PreparedStatement userStmt = conn.prepareStatement(getUserID);
            userStmt.setString(1, email);
            ResultSet rs = userStmt.executeQuery();
            if (rs.next()) {
                int userID = rs.getInt("userID");
                String deleteSQL = "DELETE FROM Registration WHERE eventID = ? AND userID = ?";
                PreparedStatement stmt = conn.prepareStatement(deleteSQL);
                stmt.setInt(1, eventID);
                stmt.setInt(2, userID);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showEditDialog(Attendee attendee, int eventID, Runnable onUpdate) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Attendee");

        TextField nameField = new TextField(attendee.getName());
        TextField emailField = new TextField(attendee.getEmail());

        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> {
            String newName = nameField.getText().trim();
            String newEmail = emailField.getText().trim();

            if (newName.isEmpty() || newEmail.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Fields cannot be empty.");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String updateSql = "UPDATE [User] SET name = ?, email = ? WHERE email = ?";
                PreparedStatement stmt = conn.prepareStatement(updateSql);
                stmt.setString(1, newName);
                stmt.setString(2, newEmail);
                stmt.setString(3, attendee.getEmail());
                stmt.executeUpdate();

                dialog.close();
                onUpdate.run();
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
            }
        });

        VBox box = new VBox(10, new Label("Name:"), nameField, new Label("Email:"), emailField, saveBtn);
        box.setPadding(new Insets(15));
        dialog.setScene(new Scene(box, 300, 200));
        dialog.show();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class Attendee {
        private final String name;
        private final String email;

        public Attendee(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }
}





ORGANIZER EVENT Page
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
                    page.show(new Stage(), id);
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


ORGNIZER Dashboard

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
                    page.show(new Stage(), id);
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


EVENT LIST Page
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
                    page.show(new Stage(), id);
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


CREATE EVENT Page
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
                    page.show(new Stage(), id);
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