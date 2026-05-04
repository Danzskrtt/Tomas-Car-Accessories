package Model;

import javafx.beans.property.*;

public class Activity {
    private final IntegerProperty activityId;
    private final StringProperty activityType;
    private final StringProperty description;
    private final StringProperty timestamp;
    private final StringProperty performedBy;
    
    // Constructor
    public Activity() {
        this(0, "", "", "", "");
    }
    
    public Activity(int activityId, String activityType, String description, String timestamp, String performedBy) {
        this.activityId = new SimpleIntegerProperty(activityId);
        this.activityType = new SimpleStringProperty(activityType);
        this.description = new SimpleStringProperty(description);
        this.timestamp = new SimpleStringProperty(timestamp);
        this.performedBy = new SimpleStringProperty(performedBy);
    }
    
    // Getters for properties
    public IntegerProperty activityIdProperty() {
        return activityId;
    }
    
    public StringProperty activityTypeProperty() {
        return activityType;
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }
    
    public StringProperty timestampProperty() {
        return timestamp;
    }
    
    public StringProperty performedByProperty() {
        return performedBy;
    }
    
    // Getters for values
    public int getActivityId() {
        return activityId.get();
    }
    
    public String getActivityType() {
        return activityType.get();
    }
    
    public String getDescription() {
        return description.get();
    }
    
    public String getTimestamp() {
        return timestamp.get();
    }
    
    public String getPerformedBy() {
        return performedBy.get();
    }
    
    // Setters
    public void setActivityId(int id) {
        activityId.set(id);
    }
    
    public void setActivityType(String type) {
        activityType.set(type);
    }
    
    public void setDescription(String desc) {
        description.set(desc);
    }
    
    public void setTimestamp(String time) {
        timestamp.set(time);
    }
    
    public void setPerformedBy(String user) {
        performedBy.set(user);
    }
    
    @Override
    public String toString() {
        return "Activity{" +
                "activityId=" + activityId.get() +
                ", activityType='" + activityType.get() + '\'' +
                ", description='" + description.get() + '\'' +
                ", timestamp='" + timestamp.get() + '\'' +
                ", performedBy='" + performedBy.get() + '\'' +
                '}';
    }
}
