package models;

public class Event {
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
