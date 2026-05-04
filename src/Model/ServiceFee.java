package Model;

import javafx.beans.property.*;

public class ServiceFee {
    private final IntegerProperty serviceId;
    private final StringProperty serviceName;
    private final StringProperty description;
    private final DoubleProperty baseFee;
    private final IntegerProperty estimatedDuration; // in minutes
    private final StringProperty category;
    private final StringProperty status;
    private final StringProperty updatedAt;
    private final StringProperty updatedBy;

    public ServiceFee(int serviceId, String serviceName, String description, double baseFee, 
                      int estimatedDuration, String category, String status, 
                      String updatedAt, String updatedBy) {
        this.serviceId = new SimpleIntegerProperty(serviceId);
        this.serviceName = new SimpleStringProperty(serviceName);
        this.description = new SimpleStringProperty(description);
        this.baseFee = new SimpleDoubleProperty(baseFee);
        this.estimatedDuration = new SimpleIntegerProperty(estimatedDuration);
        this.category = new SimpleStringProperty(category);
        this.status = new SimpleStringProperty(status);
        this.updatedAt = new SimpleStringProperty(updatedAt);
        this.updatedBy = new SimpleStringProperty(updatedBy);
    }

    public int getServiceId() { return serviceId.get(); }
    public void setServiceId(int id) { this.serviceId.set(id); }
    public IntegerProperty serviceIdProperty() { return serviceId; }

    public String getServiceName() { return serviceName.get(); }
    public void setServiceName(String name) { this.serviceName.set(name); }
    public StringProperty serviceNameProperty() { return serviceName; }

    public String getDescription() { return description.get(); }
    public void setDescription(String desc) { this.description.set(desc); }
    public StringProperty descriptionProperty() { return description; }

    public double getBaseFee() { return baseFee.get(); }
    public void setBaseFee(double fee) { this.baseFee.set(fee); }
    public DoubleProperty baseFeeProperty() { return baseFee; }

    public int getEstimatedDuration() { return estimatedDuration.get(); }
    public void setEstimatedDuration(int duration) { this.estimatedDuration.set(duration); }
    public IntegerProperty estimatedDurationProperty() { return estimatedDuration; }

    public String getCategory() { return category.get(); }
    public void setCategory(String cat) { this.category.set(cat); }
    public StringProperty categoryProperty() { return category; }

    public String getStatus() { return status.get(); }
    public void setStatus(String stat) { this.status.set(stat); }
    public StringProperty statusProperty() { return status; }

    public String getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(String date) { this.updatedAt.set(date); }
    public StringProperty updatedAtProperty() { return updatedAt; }

    public String getUpdatedBy() { return updatedBy.get(); }
    public void setUpdatedBy(String by) { this.updatedBy.set(by); }
    public StringProperty updatedByProperty() { return updatedBy; }
    
    @Override
    public String toString() {
        return getServiceName() + " (₱" + getBaseFee() + ")";
    }
}
