package Controller;

import Model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class StockInDialogController {

    @FXML private Label lblProductName;
    @FXML private Label lblCurrentStock;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtReferenceNumber;
    @FXML private TextField txtSupplier;
    @FXML private TextArea txtNotes;
    
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    
    private Product product;
    private InventoryManagementController parentController;
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";

    public void initData(Product product, InventoryManagementController parentController) {
        this.product = product;
        this.parentController = parentController;
        
        lblProductName.setText(product.getProductName());
        lblCurrentStock.setText("Current Stock: " + product.getStockQuantity());
        
        if (product.getSupplier() != null && !product.getSupplier().isEmpty()) {
            txtSupplier.setText(product.getSupplier());
        }
    }

     @FXML
     private void handleSave() {
         if (product == null) {
             showError("Invalid product selection.");
             return;
         }

         String qtyStr = txtQuantity.getText().trim();
         String refNum = txtReferenceNumber.getText().trim();
         String supplier = txtSupplier.getText().trim();
         String notes = txtNotes.getText().trim();
         
         if (qtyStr.isEmpty()) {
             showError("Please enter the quantity to stock in.");
             return;
         }

         if (refNum.isEmpty()) {
             showError("Please enter the Delivery Reference Number.");
             return;
         }
         
         int qty = 0;
         try {
             qty = Integer.parseInt(qtyStr);
             if (qty <= 0) {
                 showError("Quantity must be greater than zero.");
                 return;
             }
         } catch (NumberFormatException e) {
             showError("Invalid quantity number.");
             return;
         }
         
         Connection conn = null;
         try {
             conn = DriverManager.getConnection(DB_URL);
             
             // Validate reference number matches a stock arrival for this product
             boolean matchFound = false;
             try (PreparedStatement checkArr = conn.prepareStatement("SELECT 1 FROM stock_arrivals WHERE reference_number = ? AND product_id = ? LIMIT 1")) {
                 checkArr.setString(1, refNum);
                 checkArr.setInt(2, product.getProductId());
                 try (ResultSet rsArr = checkArr.executeQuery()) {
                     if (rsArr.next()) {
                         matchFound = true;
                     }
                 }
             }
             if (!matchFound) {
                 showError("Delivery reference number must match an existing stock arrival for this product.");
                 return;
             }

             // 1. Validate product exists in DB and get its real current stock
             int currentStock = 0;
             int currentReorder = 0;
             try (PreparedStatement checkStmt = conn.prepareStatement("SELECT stock_quantity, reorder_level FROM products WHERE product_id = ?")) {
                 checkStmt.setInt(1, product.getProductId());
                 try (ResultSet rs = checkStmt.executeQuery()) {
                     if (rs.next()) {
                         currentStock = rs.getInt("stock_quantity");
                         currentReorder = rs.getInt("reorder_level");
                     } else {
                         showError("Product does not exist or has been deleted.");
                         return;
                     }
                 }
             }

             // Begin transaction
             conn.setAutoCommit(false);
             
             int newStock = currentStock + qty;

             // 2. Create Stock History record FIRST
             // We append previous and new quantity to notes for legacy UI support, and also store them in specific columns
             String appendedNotes = notes.isEmpty() ? "Prev Qty: " + currentStock + " | New Qty: " + newStock : notes + " (Prev: " + currentStock + " | New: " + newStock + ")";
             String insertTx = "INSERT INTO stock_transactions (product_id, transaction_type, quantity, previous_quantity, new_quantity, reference_number, supplier, user_id, notes) VALUES (?, 'IN', ?, ?, ?, ?, ?, ?, ?)";
             try (PreparedStatement pstmt = conn.prepareStatement(insertTx)) {
                 pstmt.setInt(1, product.getProductId());
                 pstmt.setInt(2, qty);
                 pstmt.setInt(3, currentStock);
                 pstmt.setInt(4, newStock);
                 pstmt.setString(5, refNum);
                 pstmt.setString(6, supplier);
                 pstmt.setInt(7, Model.UserSession.getInstance().getUserId() > 0 ? Model.UserSession.getInstance().getUserId() : 1);
                 pstmt.setString(8, appendedNotes);
                 pstmt.executeUpdate();
             }
             
             // 3. Update the product quantity (and dynamic reorder level)
             int newReorderLevel = calculateDynamicReorderLevel(conn, product.getProductId(), currentReorder);
             
             String updateStock = "UPDATE products SET stock_quantity = ?, reorder_level = ? WHERE product_id = ?";
             try (PreparedStatement pstmt = conn.prepareStatement(updateStock)) {
                 pstmt.setInt(1, newStock);
                 pstmt.setInt(2, newReorderLevel);
                 pstmt.setInt(3, product.getProductId());
                 int updatedRows = pstmt.executeUpdate();
                 if (updatedRows == 0) {
                     // If update fails for any reason
                     throw new SQLException("Failed to update product stock quantity.");
                 }
             }
             
             // Commit transaction
             conn.commit();
             
             showInfo("Stock-in recorded and product quantity updated successfully.\nNew Stock: " + newStock + "\nReorder Level Adjusted to: " + newReorderLevel);
             
             if (parentController != null) {
                 parentController.refreshData();
             }
             handleCancel();
             
         } catch (SQLException e) {
             e.printStackTrace();
             if (conn != null) {
                 try {
                     System.err.println("Rolling back transaction due to error: " + e.getMessage());
                     if (!conn.isClosed()) {
                         conn.rollback();
                     }
                 } catch (SQLException rollbackEx) {
                     rollbackEx.printStackTrace();
                 }
             }
             showError("Database Error: failed to process stock in. Changes were rolled back.\n" + e.getMessage());
         } finally {
             if (conn != null) {
                 try {
                     if (!conn.isClosed()) {
                         conn.setAutoCommit(true);
                         conn.close();
                     }
                 } catch (SQLException ex) {
                     ex.printStackTrace();
                 }
             }
         }
     }
     
     /**
      * Calculate dynamic reorder level based on sales velocity
      * Formula: Base reorder level + (Average daily sales * Lead time days)
      * Lead time is estimated as 7 days by default
      */
     private int calculateDynamicReorderLevel(Connection conn, int productId, int currentReorderLevel) {
         try {
             // Get sales data from the last 30 days
             String salesQuery = "SELECT SUM(oi.quantity) as total_sales FROM order_items oi " +
                               "JOIN orders o ON oi.product_id = ? AND o.order_id = oi.order_id " +
                               "WHERE datetime(o.order_date) >= datetime('now', '-30 days') " +
                               "AND o.order_status = 'Completed'";
             
             try (PreparedStatement pstmt = conn.prepareStatement(salesQuery)) {
                 pstmt.setInt(1, productId);
                 java.sql.ResultSet rs = pstmt.executeQuery();
                 
                 if (rs.next()) {
                     int totalSales = rs.getInt("total_sales");
                     
                     if (totalSales > 0) {
                         // Average daily sales over 30 days
                         double avgDailySales = totalSales / 30.0;
                         
                         // Lead time assumed to be 7 days
                         int leadTimeDays = 7;
                         
                         // New reorder level = safety stock + (avg daily sales * lead time)
                         // Safety stock is 50% of average daily sales
                         int safetyStock = (int) Math.ceil(avgDailySales * 0.5);
                         int reorderPoint = safetyStock + (int) Math.ceil(avgDailySales * leadTimeDays);
                         
                         // Only increase reorder level, don't decrease (conservative approach)
                         return Math.max(currentReorderLevel, Math.max(reorderPoint, 5));
                     }
                 }
             }
         } catch (SQLException e) {
             System.err.println("Error calculating dynamic reorder level: " + e.getMessage());
         }
         
         // Return current reorder level if calculation fails
         return currentReorderLevel;
     }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
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
