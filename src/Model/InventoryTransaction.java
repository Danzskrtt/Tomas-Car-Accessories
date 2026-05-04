package Model;

import javafx.beans.property.*;

public class InventoryTransaction {
    private final IntegerProperty transactionId;
    private final IntegerProperty productId;
    private final StringProperty productName;
    private final StringProperty transactionType; // IN or OUT
    private final IntegerProperty quantity;
    private final StringProperty referenceNumber;
    private final StringProperty supplier;
    private final StringProperty transactionDate;
    private final IntegerProperty userId;
    private final StringProperty notes;
    
    // Constructor
    public InventoryTransaction() {
        this(0, 0, "", "", 0, "", "", "", 0, "");
    }
    
    public InventoryTransaction(int transactionId, int productId, String productName,
                               String transactionType, int quantity, String referenceNumber,
                               String supplier, String transactionDate, int userId, String notes) {
        this.transactionId = new SimpleIntegerProperty(transactionId);
        this.productId = new SimpleIntegerProperty(productId);
        this.productName = new SimpleStringProperty(productName);
        this.transactionType = new SimpleStringProperty(transactionType);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.referenceNumber = new SimpleStringProperty(referenceNumber);
        this.supplier = new SimpleStringProperty(supplier);
        this.transactionDate = new SimpleStringProperty(transactionDate);
        this.userId = new SimpleIntegerProperty(userId);
        this.notes = new SimpleStringProperty(notes);
    }
    
    // Transaction ID
    public int getTransactionId() { return transactionId.get(); }
    public void setTransactionId(int transactionId) { this.transactionId.set(transactionId); }
    public IntegerProperty transactionIdProperty() { return transactionId; }
    
    // Product ID
    public int getProductId() { return productId.get(); }
    public void setProductId(int productId) { this.productId.set(productId); }
    public IntegerProperty productIdProperty() { return productId; }
    
    // Product Name
    public String getProductName() { return productName.get(); }
    public void setProductName(String productName) { this.productName.set(productName); }
    public StringProperty productNameProperty() { return productName; }
    
    // Transaction Type
    public String getTransactionType() { return transactionType.get(); }
    public void setTransactionType(String transactionType) { this.transactionType.set(transactionType); }
    public StringProperty transactionTypeProperty() { return transactionType; }
    
    // Quantity
    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public IntegerProperty quantityProperty() { return quantity; }
    
    // Reference Number
    public String getReferenceNumber() { return referenceNumber.get(); }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber.set(referenceNumber); }
    public StringProperty referenceNumberProperty() { return referenceNumber; }
    
    // Supplier
    public String getSupplier() { return supplier.get(); }
    public void setSupplier(String supplier) { this.supplier.set(supplier); }
    public StringProperty supplierProperty() { return supplier; }
    
    // Transaction Date
    public String getTransactionDate() { return transactionDate.get(); }
    public void setTransactionDate(String transactionDate) { this.transactionDate.set(transactionDate); }
    public StringProperty transactionDateProperty() { return transactionDate; }
    
    // User ID
    public int getUserId() { return userId.get(); }
    public void setUserId(int userId) { this.userId.set(userId); }
    public IntegerProperty userIdProperty() { return userId; }
    
    // Notes
    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }
    public StringProperty notesProperty() { return notes; }
}
