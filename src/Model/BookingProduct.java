package Model;

import javafx.beans.property.*;

/**
 * Represents a product/part used in a service booking.
 * This allows one-to-many relationship: one booking can have multiple products.
 */
public class BookingProduct {
    private final IntegerProperty bookingProductId;
    private final IntegerProperty bookingId;
    private final IntegerProperty productId;
    private final StringProperty productName;
    private final StringProperty productCode;
    private final IntegerProperty quantity;
    private final DoubleProperty unitPrice;
    private final DoubleProperty totalPrice;
    private final StringProperty notes;
    
    // Constructor
    public BookingProduct() {
        this(0, 0, 0, "", "", 1, 0.0, 0.0, "");
    }
    
    public BookingProduct(int bookingProductId, int bookingId, int productId,
                         String productName, String productCode, int quantity,
                         double unitPrice, double totalPrice, String notes) {
        this.bookingProductId = new SimpleIntegerProperty(bookingProductId);
        this.bookingId = new SimpleIntegerProperty(bookingId);
        this.productId = new SimpleIntegerProperty(productId);
        this.productName = new SimpleStringProperty(productName);
        this.productCode = new SimpleStringProperty(productCode);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        this.totalPrice = new SimpleDoubleProperty(totalPrice);
        this.notes = new SimpleStringProperty(notes);
    }
    
    // Booking Product ID
    public int getBookingProductId() { return bookingProductId.get(); }
    public void setBookingProductId(int bookingProductId) { this.bookingProductId.set(bookingProductId); }
    public IntegerProperty bookingProductIdProperty() { return bookingProductId; }
    
    // Booking ID
    public int getBookingId() { return bookingId.get(); }
    public void setBookingId(int bookingId) { this.bookingId.set(bookingId); }
    public IntegerProperty bookingIdProperty() { return bookingId; }
    
    // Product ID
    public int getProductId() { return productId.get(); }
    public void setProductId(int productId) { this.productId.set(productId); }
    public IntegerProperty productIdProperty() { return productId; }
    
    // Product Name
    public String getProductName() { return productName.get(); }
    public void setProductName(String productName) { this.productName.set(productName); }
    public StringProperty productNameProperty() { return productName; }
    
    // Product Code
    public String getProductCode() { return productCode.get(); }
    public void setProductCode(String productCode) { this.productCode.set(productCode); }
    public StringProperty productCodeProperty() { return productCode; }
    
    // Quantity
    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int quantity) { 
        this.quantity.set(quantity);
        updateTotalPrice();
    }
    public IntegerProperty quantityProperty() { return quantity; }
    
    // Unit Price
    public double getUnitPrice() { return unitPrice.get(); }
    public void setUnitPrice(double unitPrice) { 
        this.unitPrice.set(unitPrice);
        updateTotalPrice();
    }
    public DoubleProperty unitPriceProperty() { return unitPrice; }
    
    // Total Price
    public double getTotalPrice() { return totalPrice.get(); }
    public void setTotalPrice(double totalPrice) { this.totalPrice.set(totalPrice); }
    public DoubleProperty totalPriceProperty() { return totalPrice; }
    
    // Notes
    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }
    public StringProperty notesProperty() { return notes; }
    
    // Helper method to update total price
    private void updateTotalPrice() {
        totalPrice.set(quantity.get() * unitPrice.get());
    }
}
