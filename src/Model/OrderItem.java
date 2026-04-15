package Model;

import javafx.beans.property.*;

public class OrderItem {
    private final IntegerProperty orderItemId;
    private final IntegerProperty orderId;
    private final IntegerProperty productId;
    private final StringProperty productName;
    private final IntegerProperty quantity;
    private final DoubleProperty unitPrice;
    private final DoubleProperty subtotal;
    private final DoubleProperty discount;
    private final DoubleProperty total;
    
    // Constructor
    public OrderItem() {
        this(0, 0, 0, "", 0, 0.0, 0.0, 0.0, 0.0);
    }
    
    public OrderItem(int orderItemId, int orderId, int productId, String productName,
                    int quantity, double unitPrice, double subtotal, double discount,
                    double total) {
        this.orderItemId = new SimpleIntegerProperty(orderItemId);
        this.orderId = new SimpleIntegerProperty(orderId);
        this.productId = new SimpleIntegerProperty(productId);
        this.productName = new SimpleStringProperty(productName);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        this.subtotal = new SimpleDoubleProperty(subtotal);
        this.discount = new SimpleDoubleProperty(discount);
        this.total = new SimpleDoubleProperty(total);
    }
    
    // Order Item ID
    public int getOrderItemId() { return orderItemId.get(); }
    public void setOrderItemId(int orderItemId) { this.orderItemId.set(orderItemId); }
    public IntegerProperty orderItemIdProperty() { return orderItemId; }
    
    // Order ID
    public int getOrderId() { return orderId.get(); }
    public void setOrderId(int orderId) { this.orderId.set(orderId); }
    public IntegerProperty orderIdProperty() { return orderId; }
    
    // Product ID
    public int getProductId() { return productId.get(); }
    public void setProductId(int productId) { this.productId.set(productId); }
    public IntegerProperty productIdProperty() { return productId; }
    
    // Product Name
    public String getProductName() { return productName.get(); }
    public void setProductName(String productName) { this.productName.set(productName); }
    public StringProperty productNameProperty() { return productName; }
    
    // Quantity
    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public IntegerProperty quantityProperty() { return quantity; }
    
    // Unit Price
    public double getUnitPrice() { return unitPrice.get(); }
    public void setUnitPrice(double unitPrice) { this.unitPrice.set(unitPrice); }
    public DoubleProperty unitPriceProperty() { return unitPrice; }
    
    // Subtotal
    public double getSubtotal() { return subtotal.get(); }
    public void setSubtotal(double subtotal) { this.subtotal.set(subtotal); }
    public DoubleProperty subtotalProperty() { return subtotal; }
    
    // Discount
    public double getDiscount() { return discount.get(); }
    public void setDiscount(double discount) { this.discount.set(discount); }
    public DoubleProperty discountProperty() { return discount; }
    
    // Total
    public double getTotal() { return total.get(); }
    public void setTotal(double total) { this.total.set(total); }
    public DoubleProperty totalProperty() { return total; }
    
    // Helper method to calculate subtotal
    public void calculateSubtotal() {
        subtotal.set(quantity.get() * unitPrice.get());
    }
    
    // Helper method to calculate total
    public void calculateTotal() {
        total.set(subtotal.get() - discount.get());
    }
}
