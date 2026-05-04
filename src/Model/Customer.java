package Model;

import javafx.beans.property.*;

public class Customer {
    private final IntegerProperty customerId;
    private final StringProperty customerName;
    private final StringProperty customerAddress;
    private final StringProperty customerEmail;
    private final StringProperty customerPhone;
    private final StringProperty plateNumber;
    private final StringProperty vehicleType;
    private final StringProperty brand;
    private final StringProperty model;
    private final StringProperty updatedAt;
    
    // Constructor
    public Customer() {
        this(0, "", "", "", "", "", "", "", "", "");
    }
    
    public Customer(int customerId, String customerName, String customerAddress, 
                   String customerEmail, String customerPhone, String plateNumber,
                   String vehicleType, String brand, String model,
                   String updatedAt) {
        this.customerId = new SimpleIntegerProperty(customerId);
        this.customerName = new SimpleStringProperty(customerName);
        this.customerAddress = new SimpleStringProperty(customerAddress);
        this.customerEmail = new SimpleStringProperty(customerEmail);
        this.customerPhone = new SimpleStringProperty(customerPhone);
        this.plateNumber = new SimpleStringProperty(plateNumber);
        this.vehicleType = new SimpleStringProperty(vehicleType);
        this.brand = new SimpleStringProperty(brand);
        this.model = new SimpleStringProperty(model);
        this.updatedAt = new SimpleStringProperty(updatedAt);
    }
    
    // Customer ID
    public int getCustomerId() { return customerId.get(); }
    public void setCustomerId(int customerId) { this.customerId.set(customerId); }
    public IntegerProperty customerIdProperty() { return customerId; }
    
    // Customer Name
    public String getCustomerName() { return customerName.get(); }
    public void setCustomerName(String customerName) { this.customerName.set(customerName); }
    public StringProperty customerNameProperty() { return customerName; }
    
    // Customer Address
    public String getCustomerAddress() { return customerAddress.get(); }
    public void setCustomerAddress(String customerAddress) { this.customerAddress.set(customerAddress); }
    public StringProperty customerAddressProperty() { return customerAddress; }
    
    // Customer Email
    public String getCustomerEmail() { return customerEmail.get(); }
    public void setCustomerEmail(String customerEmail) { this.customerEmail.set(customerEmail); }
    public StringProperty customerEmailProperty() { return customerEmail; }
    
    // Customer Phone
    public String getCustomerPhone() { return customerPhone.get(); }
    public void setCustomerPhone(String customerPhone) { this.customerPhone.set(customerPhone); }
    public StringProperty customerPhoneProperty() { return customerPhone; }
    
    // Plate Number
    public String getPlateNumber() { return plateNumber.get(); }
    public void setPlateNumber(String plateNumber) { this.plateNumber.set(plateNumber); }
    public StringProperty plateNumberProperty() { return plateNumber; }
    
    // Vehicle Type
    public String getVehicleType() { return vehicleType.get(); }
    public void setVehicleType(String vehicleType) { this.vehicleType.set(vehicleType); }
    public StringProperty vehicleTypeProperty() { return vehicleType; }
    
    // Brand
    public String getBrand() { return brand.get(); }
    public void setBrand(String brand) { this.brand.set(brand); }
    public StringProperty brandProperty() { return brand; }
    
    // Model
    public String getModel() { return model.get(); }
    public void setModel(String model) { this.model.set(model); }
    public StringProperty modelProperty() { return model; }
    
    // Updated At
    public String getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(String updatedAt) { this.updatedAt.set(updatedAt); }
    public StringProperty updatedAtProperty() { return updatedAt; }
}
