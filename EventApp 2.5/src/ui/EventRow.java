package ui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class EventRow {
    private final SimpleIntegerProperty eventID;
    private final SimpleStringProperty eventName;
    private final SimpleIntegerProperty sessionID;
    private final SimpleStringProperty sessionTitle;

    public EventRow(int eventID, String eventName, int sessionID, String sessionTitle) {
        this.eventID = new SimpleIntegerProperty(eventID);
        this.eventName = new SimpleStringProperty(eventName);
        this.sessionID = new SimpleIntegerProperty(sessionID);
        this.sessionTitle = new SimpleStringProperty(sessionTitle);
    }

    public int getEventID() { return eventID.get(); }
    public String getEventName() { return eventName.get(); }
    public int getSessionID() { return sessionID.get(); }
    public String getSessionTitle() { return sessionTitle.get(); }

    public SimpleStringProperty eventNameProperty() {
        return eventName;
    }

    public SimpleStringProperty sessionTitleProperty() {
        return sessionTitle;
    }
}
