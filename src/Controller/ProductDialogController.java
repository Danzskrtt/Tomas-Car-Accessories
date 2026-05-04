package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import Model.Product;
import Model.ActivityManager;
import Model.UserSession;

public class ProductDialogController {
    
    @FXML private TextField txtProductCode;
    @FXML private TextField txtProductName;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private TextField txtBrand;
    @FXML private TextField txtPrice;
    @FXML private TextField txtCostPrice;
    @FXML private TextField txtStockQuantity;
    @FXML private TextField txtReorderLevel;
    @FXML private TextField txtSupplier;
    @FXML private TextField txtLocation;
    @FXML private TextField txtBarcode;
    
    @FXML private Label lblImagePath;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    private Product product;
    private InventoryManagementController parentController;
    private File selectedImageFile;
    private String currentImagePath = "";

    @FXML
    public void initialize() {
        loadCategories();
    }
    
    private void loadCategories() {
        String query = "SELECT DISTINCT category FROM products WHERE category IS NOT NULL AND category != '' ORDER BY category";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String category = rs.getString("category");
                if (category != null && !category.trim().isEmpty()) {
                    cmbCategory.getItems().add(category);
                }
            }
            
            // Add common categories if not present
            String[] commonCategories = {"Oils & Fluids", "Filters", "Brakes", "Tires", "Electrical", "Engine Parts", "Suspension", "Accessories"};
            for (String cat : commonCategories) {
                if (!cmbCategory.getItems().contains(cat)) {
                    cmbCategory.getItems().add(cat);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading categories: " + e.getMessage());
        }
    }
    
    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            txtProductCode.setText(product.getProductCode());
            txtProductName.setText(product.getProductName());
            txtDescription.setText(product.getDescription());
            cmbCategory.setValue(product.getCategory());
            txtBrand.setText(product.getBrand());
            txtPrice.setText(String.valueOf(product.getPrice()));
            txtCostPrice.setText(String.valueOf(product.getCostPrice()));
            txtStockQuantity.setText(String.valueOf(product.getStockQuantity()));
            txtReorderLevel.setText(String.valueOf(product.getReorderLevel()));
            txtSupplier.setText(product.getSupplier());
            txtLocation.setText(product.getLocation());
            txtBarcode.setText(product.getBarcode());
            
            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                currentImagePath = product.getImagePath();
                lblImagePath.setText("Image selected");
            }
        }
    }
    
    public void setParentController(InventoryManagementController controller) {
        this.parentController = controller;
    }
    
    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        selectedImageFile = fileChooser.showOpenDialog(stage);
        
        if (selectedImageFile != null) {
            lblImagePath.setText(selectedImageFile.getName());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }
        
        if (product == null) {
            insertProduct();
        } else {
            updateProduct();
        }
    }
    
    private boolean validateInput() {
        String productCode = txtProductCode.getText().trim();
        String productName = txtProductName.getText().trim();
        String category = cmbCategory.getValue();
        String priceStr = txtPrice.getText().trim();
        String stockStr = txtStockQuantity.getText().trim();
        
        if (productCode.isEmpty()) {
            showError("Product code is required!");
            return false;
        } else if (isProductCodeDuplicate(productCode)) {
            showError("Product code '" + productCode + "' already exists! Please use a different code.");
            return false;
        }
        
        if (productName.isEmpty()) {
            showError("Product name is required!");
            return false;
        }
        
        if (category == null || category.trim().isEmpty()) {
            showError("Category is required!");
            return false;
        }
        
        if (priceStr.isEmpty()) {
            showError("Selling price is required!");
            return false;
        }
        
        try {
            double price = Double.parseDouble(priceStr);
            if (price < 0) {
                showError("Price cannot be negative!");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid price!");
            return false;
        }
        
        if (stockStr.isEmpty()) {
            showError("Stock quantity is required!");
            return false;
        }
        
        try {
            int stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                showError("Stock quantity cannot be negative!");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid stock quantity!");
            return false;
        }
        
        // Validate cost price if provided
        String costPriceStr = txtCostPrice.getText().trim();
        if (!costPriceStr.isEmpty()) {
            try {
                Double.parseDouble(costPriceStr);
            } catch (NumberFormatException e) {
                showError("Please enter a valid cost price!");
                return false;
            }
        }
        
        // Validate reorder level if provided
        String reorderStr = txtReorderLevel.getText().trim();
        if (!reorderStr.isEmpty()) {
            try {
                Integer.parseInt(reorderStr);
            } catch (NumberFormatException e) {
                showError("Please enter a valid reorder level!");
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isProductCodeDuplicate(String productCode) {
        String query = "SELECT COUNT(*) FROM products WHERE product_code = ?";
        if (product != null) {
            query += " AND product_id != ?";
        }
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, productCode);
            if (product != null) {
                pstmt.setInt(2, product.getProductId());
            }
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private void insertProduct() {
        String query = "INSERT INTO products (product_code, product_name, description, category, brand, price, cost_price, stock_quantity, reorder_level, supplier, location, barcode, image_path) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, txtProductCode.getText().trim());
            pstmt.setString(2, txtProductName.getText().trim());
            pstmt.setString(3, txtDescription.getText().trim());
            pstmt.setString(4, cmbCategory.getValue().trim());
            pstmt.setString(5, txtBrand.getText().trim());
            pstmt.setDouble(6, Double.parseDouble(txtPrice.getText().trim()));
            
            String costPriceStr = txtCostPrice.getText().trim();
            pstmt.setDouble(7, costPriceStr.isEmpty() ? 0.0 : Double.parseDouble(costPriceStr));
            
            pstmt.setInt(8, Integer.parseInt(txtStockQuantity.getText().trim()));
            
            String reorderStr = txtReorderLevel.getText().trim();
            pstmt.setInt(9, reorderStr.isEmpty() ? 10 : Integer.parseInt(reorderStr));
            
            pstmt.setString(10, txtSupplier.getText().trim());
            pstmt.setString(11, txtLocation.getText().trim());
            pstmt.setString(12, txtBarcode.getText().trim());
            
            // Handle image saving
            if (selectedImageFile != null) {
                try {
                    File destDir = new File("product_images");
                    if (!destDir.exists()) destDir.mkdirs();
                    
                    File destFile = new File(destDir, System.currentTimeMillis() + "_" + selectedImageFile.getName());
                    Files.copy(selectedImageFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    pstmt.setString(13, destFile.getPath());
                } catch (Exception e) {
                    System.err.println("Failed to copy image: " + e.getMessage());
                    pstmt.setString(13, null);
                }
            } else {
                pstmt.setString(13, null);
            }
            
            pstmt.executeUpdate();
            
            showInfo("Product added successfully!");
            // Log activity for new product
            String userName = UserSession.getInstance().getUsername();
            String description = String.format("Added new product: %s (Code: %s) with initial stock of %s units", 
                    txtProductName.getText().trim(), txtProductCode.getText().trim(), txtStockQuantity.getText().trim());
            ActivityManager.logActivity("PRODUCT_ADDED", description, userName);
            
            if (parentController != null) {
                parentController.refreshData();
            }
            closeDialog();
            
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                showError("Product code already exists! Please use a different code.");
            } else {
                showError("Error adding product: " + e.getMessage());
            }
        }
    }
    
    private void updateProduct() {
        String query = "UPDATE Products SET product_code = ?, product_name = ?, description = ?, " +
                     "category = ?, brand = ?, price = ?, cost_price = ?, stock_quantity = ?, " +
                     "reorder_level = ?, supplier = ?, location = ?, barcode = ?, image_path = ? " +
                     "WHERE product_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, txtProductCode.getText().trim());
            pstmt.setString(2, txtProductName.getText().trim());
            pstmt.setString(3, txtDescription.getText().trim());
            pstmt.setString(4, cmbCategory.getValue().trim());
            pstmt.setString(5, txtBrand.getText().trim());
            pstmt.setDouble(6, Double.parseDouble(txtPrice.getText().trim()));
            
            String costPriceStr = txtCostPrice.getText().trim();
            pstmt.setDouble(7, costPriceStr.isEmpty() ? 0.0 : Double.parseDouble(costPriceStr));
            
            pstmt.setInt(8, Integer.parseInt(txtStockQuantity.getText().trim()));
            
            String reorderStr = txtReorderLevel.getText().trim();
            pstmt.setInt(9, reorderStr.isEmpty() ? 10 : Integer.parseInt(reorderStr));
            
            pstmt.setString(10, txtSupplier.getText().trim());
            pstmt.setString(11, txtLocation.getText().trim());
            pstmt.setString(12, txtBarcode.getText().trim());
            
            // Handle image saving
            if (selectedImageFile != null) {
                try {
                    File destDir = new File("product_images");
                    if (!destDir.exists()) destDir.mkdirs();
                    
                    File destFile = new File(destDir, System.currentTimeMillis() + "_" + selectedImageFile.getName());
                    Files.copy(selectedImageFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    pstmt.setString(13, destFile.getPath());
                } catch (Exception e) {
                    System.err.println("Failed to copy image: " + e.getMessage());
                    pstmt.setString(13, null);
                }
            } else if (currentImagePath != null && !currentImagePath.isEmpty()) {
                pstmt.setString(13, currentImagePath);
            } else {
                pstmt.setString(13, null);
            }
            
            pstmt.setInt(14, product.getProductId());
            
            pstmt.executeUpdate();
            
            showInfo("Product updated successfully!");
            // Log activity for product update/restock
            String userName = UserSession.getInstance().getUsername();
            int newStock = Integer.parseInt(txtStockQuantity.getText().trim());
            String description = String.format("Updated product: %s (Code: %s) - Stock quantity changed to %d units", 
                    txtProductName.getText().trim(), txtProductCode.getText().trim(), newStock);
            ActivityManager.logActivity("PRODUCT_RESTOCKED", description, userName);
            
            if (parentController != null) {
                parentController.refreshData();
            }
            closeDialog();
            
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                showError("Product code already exists! Please use a different code.");
            } else {
                showError("Error updating product: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    private void closeDialog() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}