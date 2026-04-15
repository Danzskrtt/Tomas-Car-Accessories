package Model;

import javafx.beans.property.*;

/**
 * Represents a technician/mechanic assigned to a service booking.
 * This allows one-to-many relationship: one booking can have multiple technicians.
 */
public class BookingTechnician {
    private final IntegerProperty bookingTechnicianId;
    private final IntegerProperty bookingId;
    private final StringProperty technicianName;
    private final StringProperty role;
    private final StringProperty assignedTask;
    private final DoubleProperty laborRate;
    private final IntegerProperty hoursWorked;
    private final DoubleProperty totalLaborCost;
    private final StringProperty status;
    
    // Constructor
    public BookingTechnician() {
        this(0, 0, "", "Mechanic", "", 0.0, 0, 0.0, "Assigned");
    }
    
    public BookingTechnician(int bookingTechnicianId, int bookingId, String technicianName,
                            String role, String assignedTask, double laborRate,
                            int hoursWorked, double totalLaborCost, String status) {
        this.bookingTechnicianId = new SimpleIntegerProperty(bookingTechnicianId);
        this.bookingId = new SimpleIntegerProperty(bookingId);
        this.technicianName = new SimpleStringProperty(technicianName);
        this.role = new SimpleStringProperty(role);
        this.assignedTask = new SimpleStringProperty(assignedTask);
        this.laborRate = new SimpleDoubleProperty(laborRate);
        this.hoursWorked = new SimpleIntegerProperty(hoursWorked);
        this.totalLaborCost = new SimpleDoubleProperty(totalLaborCost);
        this.status = new SimpleStringProperty(status);
    }
    
    // Booking Technician ID
    public int getBookingTechnicianId() { return bookingTechnicianId.get(); }
    public void setBookingTechnicianId(int bookingTechnicianId) { this.bookingTechnicianId.set(bookingTechnicianId); }
    public IntegerProperty bookingTechnicianIdProperty() { return bookingTechnicianId; }
    
    // Booking ID
    public int getBookingId() { return bookingId.get(); }
    public void setBookingId(int bookingId) { this.bookingId.set(bookingId); }
    public IntegerProperty bookingIdProperty() { return bookingId; }
    
    // Technician Name
    public String getTechnicianName() { return technicianName.get(); }
    public void setTechnicianName(String technicianName) { this.technicianName.set(technicianName); }
    public StringProperty technicianNameProperty() { return technicianName; }
    
    // Role
    public String getRole() { return role.get(); }
    public void setRole(String role) { this.role.set(role); }
    public StringProperty roleProperty() { return role; }
    
    // Assigned Task
    public String getAssignedTask() { return assignedTask.get(); }
    public void setAssignedTask(String assignedTask) { this.assignedTask.set(assignedTask); }
    public StringProperty assignedTaskProperty() { return assignedTask; }
    
    // Labor Rate
    public double getLaborRate() { return laborRate.get(); }
    public void setLaborRate(double laborRate) { 
        this.laborRate.set(laborRate);
        updateTotalLaborCost();
    }
    public DoubleProperty laborRateProperty() { return laborRate; }
    
    // Hours Worked
    public int getHoursWorked() { return hoursWorked.get(); }
    public void setHoursWorked(int hoursWorked) { 
        this.hoursWorked.set(hoursWorked);
        updateTotalLaborCost();
    }
    public IntegerProperty hoursWorkedProperty() { return hoursWorked; }
    
    // Total Labor Cost
    public double getTotalLaborCost() { return totalLaborCost.get(); }
    public void setTotalLaborCost(double totalLaborCost) { this.totalLaborCost.set(totalLaborCost); }
    public DoubleProperty totalLaborCostProperty() { return totalLaborCost; }
    
    // Status
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }
    
    // Helper method to update total labor cost
    private void updateTotalLaborCost() {
        totalLaborCost.set(laborRate.get() * hoursWorked.get());
    }
}
