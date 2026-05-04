package Model;

import javafx.beans.property.*;

public class ServiceBooking {
    private final IntegerProperty bookingId;
    private final StringProperty bookingNumber;
    private final IntegerProperty customerId;
    private final IntegerProperty carId;
    private final StringProperty serviceType;
    private final StringProperty serviceDescription;
    private final StringProperty bookingDate;
    private final StringProperty bookingTime;
    private final IntegerProperty estimatedDuration;
    private final StringProperty assignedTechnician;
    private final StringProperty status;
    private final DoubleProperty estimatedCost;
    private final DoubleProperty actualCost;
    private final DoubleProperty downpayment;
    private final DoubleProperty balance;
    private final StringProperty notes;
    private final StringProperty updatedAt;
    private final StringProperty completedAt;
    
    // Additional properties for display
    private final StringProperty customerName;
    private final StringProperty carDescription;
    private final IntegerProperty orderId;

    // Constructor
    public ServiceBooking() {
        this.bookingId = new SimpleIntegerProperty();
        this.bookingNumber = new SimpleStringProperty();
        this.customerId = new SimpleIntegerProperty();
        this.carId = new SimpleIntegerProperty();
        this.serviceType = new SimpleStringProperty();
        this.serviceDescription = new SimpleStringProperty();
        this.bookingDate = new SimpleStringProperty();
        this.bookingTime = new SimpleStringProperty();
        this.estimatedDuration = new SimpleIntegerProperty();
        this.assignedTechnician = new SimpleStringProperty();
        this.status = new SimpleStringProperty();
        this.estimatedCost = new SimpleDoubleProperty();
        this.actualCost = new SimpleDoubleProperty();
        this.downpayment = new SimpleDoubleProperty();
        this.balance = new SimpleDoubleProperty();
        this.notes = new SimpleStringProperty();
        this.updatedAt = new SimpleStringProperty();
        this.completedAt = new SimpleStringProperty();
        this.customerName = new SimpleStringProperty();
        this.carDescription = new SimpleStringProperty();
        this.orderId = new SimpleIntegerProperty();
    }

    public ServiceBooking(int bookingId, String bookingNumber, int customerId, int carId, String serviceType, 
                          String serviceDescription, String bookingDate, String bookingTime, int estimatedDuration, 
                          String assignedTechnician, String status, double estimatedCost, double actualCost, 
                          double downpayment, double balance, String notes, String updatedAt, String completedAt,
                          String customerName, String carDescription, int orderId) {
        this.bookingId = new SimpleIntegerProperty(bookingId);
        this.bookingNumber = new SimpleStringProperty(bookingNumber);
        this.customerId = new SimpleIntegerProperty(customerId);
        this.carId = new SimpleIntegerProperty(carId);
        this.serviceType = new SimpleStringProperty(serviceType);
        this.serviceDescription = new SimpleStringProperty(serviceDescription);
        this.bookingDate = new SimpleStringProperty(bookingDate);
        this.bookingTime = new SimpleStringProperty(bookingTime);
        this.estimatedDuration = new SimpleIntegerProperty(estimatedDuration);
        this.assignedTechnician = new SimpleStringProperty(assignedTechnician);
        this.status = new SimpleStringProperty(status);
        this.estimatedCost = new SimpleDoubleProperty(estimatedCost);
        this.actualCost = new SimpleDoubleProperty(actualCost);
        this.downpayment = new SimpleDoubleProperty(downpayment);
        this.balance = new SimpleDoubleProperty(balance);
        this.notes = new SimpleStringProperty(notes);
        this.updatedAt = new SimpleStringProperty(updatedAt);
        this.completedAt = new SimpleStringProperty(completedAt);
        this.customerName = new SimpleStringProperty(customerName);
        this.carDescription = new SimpleStringProperty(carDescription);
        this.orderId = new SimpleIntegerProperty(orderId);
    }

    // Getters and Setters for orderId
    public int getOrderId() {
        return orderId.get();
    }

    public void setOrderId(int orderId) {
        this.orderId.set(orderId);
    }

    public IntegerProperty orderIdProperty() {
        return orderId;
    }

    public int getBookingId() { return bookingId.get(); }
    public void setBookingId(int bookingId) { this.bookingId.set(bookingId); }
    public IntegerProperty bookingIdProperty() { return bookingId; }
    
    // Booking Number
    public String getBookingNumber() { return bookingNumber.get(); }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber.set(bookingNumber); }
    public StringProperty bookingNumberProperty() { return bookingNumber; }
    
    // Customer ID
    public int getCustomerId() { return customerId.get(); }
    public void setCustomerId(int customerId) { this.customerId.set(customerId); }
    public IntegerProperty customerIdProperty() { return customerId; }
    
    // Car ID
    public int getCarId() { return carId.get(); }
    public void setCarId(int carId) { this.carId.set(carId); }
    public IntegerProperty carIdProperty() { return carId; }
    
    // Service Type
    public String getServiceType() { return serviceType.get(); }
    public void setServiceType(String serviceType) { this.serviceType.set(serviceType); }
    public StringProperty serviceTypeProperty() { return serviceType; }
    
    // Service Description
    public String getServiceDescription() { return serviceDescription.get(); }
    public void setServiceDescription(String serviceDescription) { this.serviceDescription.set(serviceDescription); }
    public StringProperty serviceDescriptionProperty() { return serviceDescription; }
    
    // Booking Date
    public String getBookingDate() { return bookingDate.get(); }
    public void setBookingDate(String bookingDate) { this.bookingDate.set(bookingDate); }
    public StringProperty bookingDateProperty() { return bookingDate; }
    
    // Booking Time
    public String getBookingTime() { return bookingTime.get(); }
    public void setBookingTime(String bookingTime) { this.bookingTime.set(bookingTime); }
    public StringProperty bookingTimeProperty() { return bookingTime; }
    
    // Estimated Duration
    public int getEstimatedDuration() { return estimatedDuration.get(); }
    public void setEstimatedDuration(int estimatedDuration) { this.estimatedDuration.set(estimatedDuration); }
    public IntegerProperty estimatedDurationProperty() { return estimatedDuration; }
    
    // Assigned Technician
    public String getAssignedTechnician() { return assignedTechnician.get(); }
    public void setAssignedTechnician(String assignedTechnician) { this.assignedTechnician.set(assignedTechnician); }
    public StringProperty assignedTechnicianProperty() { return assignedTechnician; }
    
    // Status
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }
    
    // Estimated Cost
    public double getEstimatedCost() { return estimatedCost.get(); }
    public void setEstimatedCost(double estimatedCost) { this.estimatedCost.set(estimatedCost); }
    public DoubleProperty estimatedCostProperty() { return estimatedCost; }
    
    // Actual Cost
    public double getActualCost() { return actualCost.get(); }
    public void setActualCost(double actualCost) { this.actualCost.set(actualCost); }
    public DoubleProperty actualCostProperty() { return actualCost; }
    
    // Downpayment
    public double getDownpayment() { return downpayment.get(); }
    public void setDownpayment(double downpayment) { this.downpayment.set(downpayment); }
    public DoubleProperty downpaymentProperty() { return downpayment; }
    
    // Balance
    public double getBalance() { return balance.get(); }
    public void setBalance(double balance) { this.balance.set(balance); }
    public DoubleProperty balanceProperty() { return balance; }
    
    // Notes
    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }
    public StringProperty notesProperty() { return notes; }
    
    // Updated At
    public String getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(String updatedAt) { this.updatedAt.set(updatedAt); }
    public StringProperty updatedAtProperty() { return updatedAt; }
    
    // Completed At
    public String getCompletedAt() { return completedAt.get(); }
    public void setCompletedAt(String completedAt) { this.completedAt.set(completedAt); }
    public StringProperty completedAtProperty() { return completedAt; }
    
    // Customer Name
    public String getCustomerName() { return customerName.get(); }
    public void setCustomerName(String customerName) { this.customerName.set(customerName); }
    public StringProperty customerNameProperty() { return customerName; }
    
    // Car Description
    public String getCarDescription() { return carDescription.get(); }
    public void setCarDescription(String carDescription) { this.carDescription.set(carDescription); }
    public StringProperty carDescriptionProperty() { return carDescription; }
}