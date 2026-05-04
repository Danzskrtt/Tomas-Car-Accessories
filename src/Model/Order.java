package Model;

import javafx.beans.property.*;

public class Order {
    private final IntegerProperty orderId;
    private final StringProperty orderNumber;
    private final IntegerProperty customerId;
    private final IntegerProperty userId;
    private final StringProperty orderDate;
    private final DoubleProperty totalAmount;
    private final DoubleProperty discountAmount;
    private final DoubleProperty taxAmount;
    private final DoubleProperty finalAmount;
    private final StringProperty paymentMethod;
    private final StringProperty paymentStatus;
    private final StringProperty orderStatus;
    private final StringProperty notes;
    private final StringProperty updatedAt;
    
    // Additional properties for display
    private final StringProperty customerName;
    private final StringProperty userName;
    
    // Constructor
    public Order() {
        this(0, "", 0, 0, "", 0.0, 0.0, 0.0, 0.0, "", "Pending", "Completed", "", "", "", "");
    }
    
    public Order(int orderId, String orderNumber, int customerId, int userId,
                String orderDate, double totalAmount, double discountAmount,
                double taxAmount, double finalAmount, String paymentMethod,
                String paymentStatus, String orderStatus, String notes,
                String updatedAt, String customerName, String userName) {
        this.orderId = new SimpleIntegerProperty(orderId);
        this.orderNumber = new SimpleStringProperty(orderNumber);
        this.customerId = new SimpleIntegerProperty(customerId);
        this.userId = new SimpleIntegerProperty(userId);
        this.orderDate = new SimpleStringProperty(orderDate);
        this.totalAmount = new SimpleDoubleProperty(totalAmount);
        this.discountAmount = new SimpleDoubleProperty(discountAmount);
        this.taxAmount = new SimpleDoubleProperty(taxAmount);
        this.finalAmount = new SimpleDoubleProperty(finalAmount);
        this.paymentMethod = new SimpleStringProperty(paymentMethod);
        this.paymentStatus = new SimpleStringProperty(paymentStatus);
        this.orderStatus = new SimpleStringProperty(orderStatus);
        this.notes = new SimpleStringProperty(notes);
        this.updatedAt = new SimpleStringProperty(updatedAt);
        this.customerName = new SimpleStringProperty(customerName);
        this.userName = new SimpleStringProperty(userName);
    }
    
    // Order ID
    public int getOrderId() { return orderId.get(); }
    public void setOrderId(int orderId) { this.orderId.set(orderId); }
    public IntegerProperty orderIdProperty() { return orderId; }
    
    // Order Number
    public String getOrderNumber() { return orderNumber.get(); }
    public void setOrderNumber(String orderNumber) { this.orderNumber.set(orderNumber); }
    public StringProperty orderNumberProperty() { return orderNumber; }
    
    // Customer ID
    public int getCustomerId() { return customerId.get(); }
    public void setCustomerId(int customerId) { this.customerId.set(customerId); }
    public IntegerProperty customerIdProperty() { return customerId; }
    
    // User ID
    public int getUserId() { return userId.get(); }
    public void setUserId(int userId) { this.userId.set(userId); }
    public IntegerProperty userIdProperty() { return userId; }
    
    // Order Date
    public String getOrderDate() { return orderDate.get(); }
    public void setOrderDate(String orderDate) { this.orderDate.set(orderDate); }
    public StringProperty orderDateProperty() { return orderDate; }
    
    // Total Amount
    public double getTotalAmount() { return totalAmount.get(); }
    public void setTotalAmount(double totalAmount) { this.totalAmount.set(totalAmount); }
    public DoubleProperty totalAmountProperty() { return totalAmount; }
    
    // Discount Amount
    public double getDiscountAmount() { return discountAmount.get(); }
    public void setDiscountAmount(double discountAmount) { this.discountAmount.set(discountAmount); }
    public DoubleProperty discountAmountProperty() { return discountAmount; }
    
    // Tax Amount
    public double getTaxAmount() { return taxAmount.get(); }
    public void setTaxAmount(double taxAmount) { this.taxAmount.set(taxAmount); }
    public DoubleProperty taxAmountProperty() { return taxAmount; }
    
    // Final Amount
    public double getFinalAmount() { return finalAmount.get(); }
    public void setFinalAmount(double finalAmount) { this.finalAmount.set(finalAmount); }
    public DoubleProperty finalAmountProperty() { return finalAmount; }
    
    // Payment Method
    public String getPaymentMethod() { return paymentMethod.get(); }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod.set(paymentMethod); }
    public StringProperty paymentMethodProperty() { return paymentMethod; }
    
    // Payment Status
    public String getPaymentStatus() { return paymentStatus.get(); }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus.set(paymentStatus); }
    public StringProperty paymentStatusProperty() { return paymentStatus; }
    
    // Order Status
    public String getOrderStatus() { return orderStatus.get(); }
    public void setOrderStatus(String orderStatus) { this.orderStatus.set(orderStatus); }
    public StringProperty orderStatusProperty() { return orderStatus; }
    
    // Notes
    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }
    public StringProperty notesProperty() { return notes; }
    
    // Updated At
    public String getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(String updatedAt) { this.updatedAt.set(updatedAt); }
    public StringProperty updatedAtProperty() { return updatedAt; }
    
    // Customer Name
    public String getCustomerName() { return customerName.get(); }
    public void setCustomerName(String customerName) { this.customerName.set(customerName); }
    public StringProperty customerNameProperty() { return customerName; }
    
    // User Name
    public String getUserName() { return userName.get(); }
    public void setUserName(String userName) { this.userName.set(userName); }
    public StringProperty userNameProperty() { return userName; }
}
