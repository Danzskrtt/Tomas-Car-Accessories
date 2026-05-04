package Model;

import javafx.beans.property.*;

public class Product {
    private final IntegerProperty productId;
    private final IntegerProperty categoryId;
    private final StringProperty productCode;
    private final StringProperty productName;
    private final StringProperty description;
    private final StringProperty category;
    private final StringProperty brand;
    private final DoubleProperty price;
    private final DoubleProperty costPrice;
    private final IntegerProperty stockQuantity;
    private final IntegerProperty reorderLevel;
    private final StringProperty supplier;
    private final StringProperty location;
    private final StringProperty barcode;
    private final StringProperty imagePath;
    private final StringProperty updatedAt;
    
    // Constructor
    public Product() {
        this(0, 0, "", "", "", "", "", 0.0, 0.0, 0, 10, "", "", "", "", "");
    }
    
    public Product(int productId, int categoryId, String productCode, String productName,
                  String description, String category, String brand, double price,
                  double costPrice, int stockQuantity, int reorderLevel,
                  String supplier, String location, String barcode, String imagePath,
                  String updatedAt) {
        this.productId = new SimpleIntegerProperty(productId);
        this.categoryId = new SimpleIntegerProperty(categoryId);
        this.productCode = new SimpleStringProperty(productCode);
        this.productName = new SimpleStringProperty(productName);
        this.description = new SimpleStringProperty(description);
        this.category = new SimpleStringProperty(category);
        this.brand = new SimpleStringProperty(brand);
        this.price = new SimpleDoubleProperty(price);
        this.costPrice = new SimpleDoubleProperty(costPrice);
        this.stockQuantity = new SimpleIntegerProperty(stockQuantity);
        this.reorderLevel = new SimpleIntegerProperty(reorderLevel);
        this.supplier = new SimpleStringProperty(supplier);
        this.location = new SimpleStringProperty(location);
        this.barcode = new SimpleStringProperty(barcode);
        this.imagePath = new SimpleStringProperty(imagePath);
        this.updatedAt = new SimpleStringProperty(updatedAt);
    }
    
    // Product ID
    public int getProductId() { return productId.get(); }
    public void setProductId(int productId) { this.productId.set(productId); }
    public IntegerProperty productIdProperty() { return productId; }
    
    // Category ID
    public int getCategoryId() { return categoryId.get(); }
    public void setCategoryId(int categoryId) { this.categoryId.set(categoryId); }
    public IntegerProperty categoryIdProperty() { return categoryId; }
    
    // Product Code
    public String getProductCode() { return productCode.get(); }
    public void setProductCode(String productCode) { this.productCode.set(productCode); }
    public StringProperty productCodeProperty() { return productCode; }
    
    // Product Name
    public String getProductName() { return productName.get(); }
    public void setProductName(String productName) { this.productName.set(productName); }
    public StringProperty productNameProperty() { return productName; }
    
    // Description
    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }
    
    // Category
    public String getCategory() { return category.get(); }
    public void setCategory(String category) { this.category.set(category); }
    public StringProperty categoryProperty() { return category; }
    
    // Brand
    public String getBrand() { return brand.get(); }
    public void setBrand(String brand) { this.brand.set(brand); }
    public StringProperty brandProperty() { return brand; }
    
    // Price
    public double getPrice() { return price.get(); }
    public void setPrice(double price) { this.price.set(price); }
    public DoubleProperty priceProperty() { return price; }
    
    // Cost Price
    public double getCostPrice() { return costPrice.get(); }
    public void setCostPrice(double costPrice) { this.costPrice.set(costPrice); }
    public DoubleProperty costPriceProperty() { return costPrice; }
    
    // Stock Quantity
    public int getStockQuantity() { return stockQuantity.get(); }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity.set(stockQuantity); }
    public IntegerProperty stockQuantityProperty() { return stockQuantity; }
    
    // Reorder Level
    public int getReorderLevel() { return reorderLevel.get(); }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel.set(reorderLevel); }
    public IntegerProperty reorderLevelProperty() { return reorderLevel; }
    
    // Supplier
    public String getSupplier() { return supplier.get(); }
    public void setSupplier(String supplier) { this.supplier.set(supplier); }
    public StringProperty supplierProperty() { return supplier; }
    
    // Location
    public String getLocation() { return location.get(); }
    public void setLocation(String location) { this.location.set(location); }
    public StringProperty locationProperty() { return location; }
    
    // Barcode
    public String getBarcode() { return barcode.get(); }
    public void setBarcode(String barcode) { this.barcode.set(barcode); }
    public StringProperty barcodeProperty() { return barcode; }
    
    // Image Path
    public String getImagePath() { return imagePath.get(); }
    public void setImagePath(String imagePath) { this.imagePath.set(imagePath); }
    public StringProperty imagePathProperty() { return imagePath; }
    
    // Updated At
    public String getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(String updatedAt) { this.updatedAt.set(updatedAt); }
    public StringProperty updatedAtProperty() { return updatedAt; }
    
    // Helper methods
    public boolean isLowStock() {
        return stockQuantity.get() <= reorderLevel.get();
    }
    
    public double getProfit() {
        return price.get() - costPrice.get();
    }
    
    public double getProfitMargin() {
        if (price.get() == 0) return 0;
        return ((price.get() - costPrice.get()) / price.get()) * 100;
    }
}
