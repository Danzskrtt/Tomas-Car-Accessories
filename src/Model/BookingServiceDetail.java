package Model;

import javafx.beans.property.*;

/**
 * Represents a single service detail/item in a service booking.
 * This allows one-to-many relationship: one booking can have multiple service details.
 */
public class BookingServiceDetail {
    private final IntegerProperty detailId;
    private final IntegerProperty bookingId;
    private final StringProperty serviceType;
    private final StringProperty serviceDescription;
    private final DoubleProperty laborCost;
    private final StringProperty technician;
    private final StringProperty status;
    private final StringProperty notes;
    
    // Constructor
    public BookingServiceDetail() {
        this(0, 0, "", "", 0.0, "", "Pending", "");
    }
    
    public BookingServiceDetail(int detailId, int bookingId, String serviceType, 
                               String serviceDescription, double laborCost, 
                               String technician, String status, String notes) {
        this.detailId = new SimpleIntegerProperty(detailId);
        this.bookingId = new SimpleIntegerProperty(bookingId);
        this.serviceType = new SimpleStringProperty(serviceType);
        this.serviceDescription = new SimpleStringProperty(serviceDescription);
        this.laborCost = new SimpleDoubleProperty(laborCost);
        this.technician = new SimpleStringProperty(technician);
        this.status = new SimpleStringProperty(status);
        this.notes = new SimpleStringProperty(notes);
    }
    
    // Detail ID
    public int getDetailId() { return detailId.get(); }
    public void setDetailId(int detailId) { this.detailId.set(detailId); }
    public IntegerProperty detailIdProperty() { return detailId; }
    
    // Booking ID
    public int getBookingId() { return bookingId.get(); }
    public void setBookingId(int bookingId) { this.bookingId.set(bookingId); }
    public IntegerProperty bookingIdProperty() { return bookingId; }
    
    // Service Type
    public String getServiceType() { return serviceType.get(); }
    public void setServiceType(String serviceType) { this.serviceType.set(serviceType); }
    public StringProperty serviceTypeProperty() { return serviceType; }
    
    // Service Description
    public String getServiceDescription() { return serviceDescription.get(); }
    public void setServiceDescription(String serviceDescription) { this.serviceDescription.set(serviceDescription); }
    public StringProperty serviceDescriptionProperty() { return serviceDescription; }
    
    // Labor Cost
    public double getLaborCost() { return laborCost.get(); }
    public void setLaborCost(double laborCost) { this.laborCost.set(laborCost); }
    public DoubleProperty laborCostProperty() { return laborCost; }
    
    // Technician
    public String getTechnician() { return technician.get(); }
    public void setTechnician(String technician) { this.technician.set(technician); }
    public StringProperty technicianProperty() { return technician; }
    
    // Status
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }
    
    // Notes
    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }
    public StringProperty notesProperty() { return notes; }
}
