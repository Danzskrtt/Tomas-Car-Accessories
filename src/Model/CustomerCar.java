package Model;

import javafx.beans.property.*;

public class CustomerCar {
    private final IntegerProperty carId;
    private final IntegerProperty customerId;
    private final StringProperty make;
    private final StringProperty model;
    private final IntegerProperty year;
    private final StringProperty color;
    private final StringProperty plateNumber;
    private final StringProperty vin;
    private final IntegerProperty mileage;
    private final StringProperty notes;
    private final StringProperty updatedAt;
    private final StringProperty carImagePath;
    
    // Constructor
    public CustomerCar() {
        this(0, 0, "", "", 0, "", "", "", 0, "", "");
    }
    
    // Simplified constructor for basic car info with image
    public CustomerCar(int carId, int customerId, String make, String model, int year,
                      String color, String plateNumber, String carImagePath) {
        this(carId, customerId, make, model, year, color, plateNumber, "", 0, "", "");
        this.carImagePath.set(carImagePath);
    }
    
    public CustomerCar(int carId, int customerId, String make, String model, int year,
                      String color, String plateNumber, String vin, int mileage,
                      String notes, String updatedAt) {
        this.carId = new SimpleIntegerProperty(carId);
        this.customerId = new SimpleIntegerProperty(customerId);
        this.make = new SimpleStringProperty(make);
        this.model = new SimpleStringProperty(model);
        this.year = new SimpleIntegerProperty(year);
        this.color = new SimpleStringProperty(color);
        this.plateNumber = new SimpleStringProperty(plateNumber);
        this.vin = new SimpleStringProperty(vin);
        this.mileage = new SimpleIntegerProperty(mileage);
        this.notes = new SimpleStringProperty(notes);
        this.updatedAt = new SimpleStringProperty(updatedAt);
        this.carImagePath = new SimpleStringProperty("");
    }
    
    // Car ID
    public int getCarId() { return carId.get(); }
    public void setCarId(int carId) { this.carId.set(carId); }
    public IntegerProperty carIdProperty() { return carId; }
    
    // Customer ID
    public int getCustomerId() { return customerId.get(); }
    public void setCustomerId(int customerId) { this.customerId.set(customerId); }
    public IntegerProperty customerIdProperty() { return customerId; }
    
    // Make
    public String getMake() { return make.get(); }
    public void setMake(String make) { this.make.set(make); }
    public StringProperty makeProperty() { return make; }
    
    // Model
    public String getModel() { return model.get(); }
    public void setModel(String model) { this.model.set(model); }
    public StringProperty modelProperty() { return model; }
    
    // Year
    public int getYear() { return year.get(); }
    public void setYear(int year) { this.year.set(year); }
    public IntegerProperty yearProperty() { return year; }
    
    // Color
    public String getColor() { return color.get(); }
    public void setColor(String color) { this.color.set(color); }
    public StringProperty colorProperty() { return color; }
    
    // Plate Number
    public String getPlateNumber() { return plateNumber.get(); }
    public void setPlateNumber(String plateNumber) { this.plateNumber.set(plateNumber); }
    public StringProperty plateNumberProperty() { return plateNumber; }
    
    // VIN
    public String getVin() { return vin.get(); }
    public void setVin(String vin) { this.vin.set(vin); }
    public StringProperty vinProperty() { return vin; }
    
    // Mileage
    public int getMileage() { return mileage.get(); }
    public void setMileage(int mileage) { this.mileage.set(mileage); }
    public IntegerProperty mileageProperty() { return mileage; }
    
    // Notes
    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }
    public StringProperty notesProperty() { return notes; }
    
    // Updated At
    public String getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(String updatedAt) { this.updatedAt.set(updatedAt); }
    public StringProperty updatedAtProperty() { return updatedAt; }
    
    // Car Image Path
    public String getCarImagePath() { return carImagePath.get(); }
    public void setCarImagePath(String carImagePath) { this.carImagePath.set(carImagePath); }
    public StringProperty carImagePathProperty() { return carImagePath; }
    
    // Helper method to get license plate (alias for compatibility)
    public String getLicensePlate() { return plateNumber.get(); }
    public void setLicensePlate(String licensePlate) { this.plateNumber.set(licensePlate); }
    
    // Helper method to get full car description
    public String getFullDescription() {
        return year.get() + " " + make.get() + " " + model.get() + " (" + plateNumber.get() + ")";
    }
}
