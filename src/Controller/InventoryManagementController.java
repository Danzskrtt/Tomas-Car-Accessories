package Controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Callback;
import java.io.File;
import java.util.List;

import Model.Product;
import java.sql.*;
import java.text.DecimalFormat;

public class InventoryManagementController {
    
    // Navigation buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnUserManagement;
    @FXML private Button btnCustomerManagement;
    @FXML private Button btnServiceBooking;
    @FXML private Button btnInventory;
    @FXML private Button btnSales;
    @FXML private Button btnEmployeeManagement;
    @FXML private Button btnTransactions;
    @FXML private Button btnLogout;
    
    @FXML private TextField txtSearch;
    @FXML private Button btnSearch;
    @FXML private Button btnRefresh;
    @FXML private Button btnStockHistory;
    @FXML private Button btnStockArrivals;
    @FXML private Button btnAddProduct;
    @FXML private Label lblProductCount;
    @FXML private ComboBox<String> cmbFilter;
    @FXML private CheckBox chkLowStockOnly;
    
    // Stats labels
    @FXML private Label lblTotalProducts;
    @FXML private Label lblLowStock;
    @FXML private Label lblStockValue;
    @FXML private Label lblCategories;
    
    // Replace TableView
    @FXML private TableView<Product> tvProducts;
    @FXML private TableColumn<Product, String> colImage;
    @FXML private TableColumn<Product, String> colCode;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, String> colStatus;
    @FXML private TableColumn<Product, Product> colActions;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<Product> allProducts = FXCollections.observableArrayList();
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    private DecimalFormat df = new DecimalFormat("#,##0.00");

    @FXML
    public void initialize() {
        NavigationManager.applyRoleBasedAccess(this);
        // Only verify access if not an initial load (Session might be null in some scenes during tests)
        // Let's assume scene controller will handle this or we can do it after scene load
        
        initializeDatabaseTables();
        setupTable();
        setupFilters();
        loadData();
    }
    
    private void initializeDatabaseTables() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS stock_transactions (" +
                        "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "product_id INTEGER NOT NULL, " +
                        "transaction_type TEXT NOT NULL, " +
                        "quantity INTEGER NOT NULL, " +
                        "reference_number TEXT, " +
                        "supplier TEXT, " +
                        "transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "user_id INTEGER, " +
                        "notes TEXT, " +
                        "FOREIGN KEY (product_id) REFERENCES products(product_id))");

            try {
                stmt.execute("ALTER TABLE stock_transactions ADD COLUMN previous_quantity INTEGER DEFAULT 0");
                stmt.execute("ALTER TABLE stock_transactions ADD COLUMN new_quantity INTEGER DEFAULT 0");
            } catch (SQLException ignore) {
                // Columns likely already exist
            }

        } catch (SQLException e) {
            System.err.println("Note: Error initializing inventory tables: " + e.getMessage());
        }
    }
    
    private void setupTable() {
        colImage.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getImagePath()));
        colImage.setCellFactory(column -> new TableCell<Product, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
            }
            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.trim().isEmpty()) {
                    setGraphic(null);
                } else {
                    File file = new File(imagePath);
                    if (file.exists()) {
                        imageView.setImage(new Image(file.toURI().toString(), 50, 50, true, true));
                        setGraphic(imageView);
                    } else {
                        try {
                            java.io.InputStream is = getClass().getResourceAsStream("/pics/default_product.png");
                            if (is != null) {
                                imageView.setImage(new Image(is, 50, 50, true, true));
                                setGraphic(imageView);
                            } else {
                                setGraphic(null);
                            }
                        } catch (Exception e) {
                            setGraphic(null);
                        }
                    }
                }
            }
        });

        colCode.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductCode()));
        colName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
        colCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
        
        colPrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrice()));
        colPrice.setCellFactory(column -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText("₱" + df.format(price));
                }
            }
        });
        
        colStock.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStockQuantity()));
        colStock.setCellFactory(column -> new TableCell<Product, Integer>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(stock.toString());
                    Product product = getTableView().getItems().get(getIndex());
                    if (stock <= product.getReorderLevel()) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        colStatus.setCellValueFactory(cellData -> {
            Product p = cellData.getValue();
            return new SimpleStringProperty(p.getStockQuantity() > p.getReorderLevel() ? "In Stock" : "Low Stock");
        });
        colStatus.setCellFactory(column -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(status);
                    label.setStyle("-fx-padding: 3 8; -fx-border-radius: 12; -fx-background-radius: 12;");
                    if (status.equals("In Stock")) {
                        label.setStyle(label.getStyle() + "-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                    } else {
                        label.setStyle(label.getStyle() + "-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                    }
                    setGraphic(label);
                }
            }
        });

        colActions.setCellValueFactory(param -> new javafx.beans.property.ReadOnlyObjectWrapper<>(param.getValue()));
        colActions.setCellFactory(new Callback<TableColumn<Product, Product>, TableCell<Product, Product>>() {
            @Override
            public TableCell<Product, Product> call(final TableColumn<Product, Product> param) {
                return new TableCell<Product, Product>() {
                    private final Button btnEdit = new Button("✎ Edit");
                    private final Button btnReorder = new Button("📦 Stock In");
                    private final Button btnDelete = new Button("✕ Delete");
                    private final HBox pane = new HBox(10, btnEdit, btnReorder, btnDelete);

                    {
                        pane.setAlignment(javafx.geometry.Pos.CENTER);
                        
                        btnEdit.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
                        btnReorder.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
                        btnDelete.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
                        
                        btnEdit.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #212529; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 6;");
                        btnReorder.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 6;");
                        btnDelete.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 6;");

                        btnEdit.setOnAction(event -> {
                            Product data = getItem();
                            if (data != null) {
                                showProductDialog(data);
                            }
                        });

                        btnReorder.setOnAction(event -> {
                            Product data = getItem();
                            if (data != null) {
                                handleReorder(data);
                            }
                        });

                        btnDelete.setOnAction(event -> {
                            Product data = getItem();
                            if (data != null) {
                                deleteProduct(data);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Product item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        });
    }

    private void setupFilters() {
        cmbFilter.getItems().add("All Products");
        loadCategories();
        cmbFilter.getSelectionModel().select("All Products");
        cmbFilter.setOnAction(e -> handleFilterChange());
    }
    
    private void loadCategories() {
        String query = "SELECT DISTINCT category FROM products WHERE category IS NOT NULL AND category != '' ORDER BY category";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String category = rs.getString("category");
                if (category != null && !category.trim().isEmpty()) {
                    cmbFilter.getItems().add(category);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading categories: " + e.getMessage());
        }
    }
    
    private void loadData() {
        allProducts.clear();
        String query = "SELECT * FROM products ORDER BY product_name";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("product_id"),
                    rs.getInt("category_id"),
                    getString(rs, "product_code"),
                    rs.getString("product_name"),
                    getString(rs, "description"),
                    getString(rs, "category"),
                    getString(rs, "brand"),
                    rs.getDouble("price"),
                    getDouble(rs, "cost_price"),
                    rs.getInt("stock_quantity"),
                    getInt(rs, "reorder_level"),
                    getString(rs, "supplier"),
                    getString(rs, "location"),
                    getString(rs, "barcode"),
                    getString(rs, "image_path"),
                    getString(rs, "updated_at")
                );
                allProducts.add(product);
            }
            
            applyFilters();
            updateStatistics();
            
        } catch (SQLException e) {
            showError("Error loading products: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        int totalProducts = allProducts.size();
        int lowStock = 0;
        double stockValue = 0.0;
        java.util.Set<String> categories = new java.util.HashSet<>();

        for (Product p : allProducts) {
            if (p.isLowStock()) {
                lowStock++;
            }
            stockValue += p.getPrice() * p.getStockQuantity();
            if (p.getCategory() != null && !p.getCategory().trim().isEmpty()) {
                categories.add(p.getCategory());
            }
        }

        if (lblTotalProducts != null) lblTotalProducts.setText(String.valueOf(totalProducts));
        if (lblLowStock != null) lblLowStock.setText(String.valueOf(lowStock));
        if (lblCategories != null) lblCategories.setText(String.valueOf(categories.size()));
        
        if (lblStockValue != null) {
            String role = Model.UserSession.getInstance().getUserRole();
            if ("Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role)) {
                lblStockValue.setText("₱" + df.format(stockValue));
            } else {
                lblStockValue.setText("Hidden");
            }
        }
    }
    
    private String getString(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value != null ? value : "";
    }
    
    private double getDouble(ResultSet rs, String columnName) throws SQLException {
        try {
            return rs.getDouble(columnName);
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    private int getInt(ResultSet rs, String columnName) throws SQLException {
        try {
            return rs.getInt(columnName);
        } catch (Exception e) {
            return 0;
        }
    }
    
    @FXML
    private void handleFilterChange() {
        applyFilters();
    }
    
    private void applyFilters() {
        productList.clear();

        String selectedCategory = cmbFilter != null ? cmbFilter.getValue() : null;
        boolean lowStockOnly = chkLowStockOnly != null && chkLowStockOnly.isSelected();

        for (Product product : allProducts) {
            boolean matchesCategory = selectedCategory == null || 
                                     selectedCategory.equals("All Products") || 
                                     product.getCategory().equals(selectedCategory);
                                     
            boolean matchesStockFilter = !lowStockOnly || product.isLowStock();
            
            if (matchesCategory && matchesStockFilter) {
                productList.add(product);
            }
        }
        
        updateProductFlowPane(productList);
        if (lblProductCount != null) {
            lblProductCount.setText("Total: " + productList.size());
        }
    }

    private void updateProductFlowPane(List<Product> products) {
        if (tvProducts == null) return;
        ObservableList<Product> items = tvProducts.getItems();
        if (items != null) {
            items.setAll(products);
        } else {
            tvProducts.setItems(FXCollections.observableArrayList(products));
        }
    }
    
    public void showProductDialog(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/productdialog.fxml"));
            Parent root = loader.load();
            
            ProductDialogController controller = loader.getController();
            controller.setProduct(product);
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.setTitle(product == null ? "Add Product" : "Edit Product");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (Exception e) {
            showError("Error opening product dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void deleteProduct(Product product) {
        if (product.getStockQuantity() > 0) {
            showError("Cannot delete product that still has stock (" + product.getStockQuantity() + " remaining).");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Product");
        alert.setHeaderText("Delete " + product.getProductName() + "?");
        alert.setContentText("Are you sure you want to delete this product? This action cannot be undone.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String query = "DELETE FROM products WHERE product_id = ?";
                try (Connection conn = DriverManager.getConnection(DB_URL);
                     PreparedStatement pstmt = conn.prepareStatement(query)) {
                    
                    pstmt.setInt(1, product.getProductId());
                    pstmt.executeUpdate();
                    
                    showInfo("Product deleted successfully!");
                    loadData();
                    
                } catch (SQLException e) {
                    showError("Error deleting product: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleStockHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/stockhistorydialog.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Stock Transaction History");
            stage.initModality(Modality.APPLICATION_MODAL);
            java.io.InputStream iconStream = getClass().getResourceAsStream("/pics/tomas_logo.jpg");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
            
            // Adjust preferred width/height if needed
            Scene scene = new Scene(root, 1000, 600);
            stage.setScene(scene);
            
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not open Stock History: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleStockArrivals() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/stockarrivalsdialog.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Stock Arrivals Monitor");
            stage.initModality(Modality.APPLICATION_MODAL);
            java.io.InputStream iconStream = getClass().getResourceAsStream("/pics/tomas_logo.jpg");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
            
            Scene scene = new Scene(root, 950, 650);
            stage.setScene(scene);
            
            stage.showAndWait();
            
            // Refresh inventory when closed so new stock changes are reflected
            loadData();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not open Stock Arrivals: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAddProduct() {
        showProductDialog(null);
    }
    
    @FXML
    private void handleSearch() {
        String searchText = txtSearch.getText().toLowerCase();
        if (searchText.isEmpty()) {
            applyFilters();
            return;
        }
        
        productList.clear();
        for (Product product : allProducts) {
            if (product.getProductName().toLowerCase().contains(searchText) ||
                product.getProductCode().toLowerCase().contains(searchText) ||
                product.getCategory().toLowerCase().contains(searchText)) {
                
                String selectedCategory = cmbFilter != null ? cmbFilter.getValue() : null;
                boolean lowStockOnly = chkLowStockOnly != null && chkLowStockOnly.isSelected();
                
                boolean matchesCategory = selectedCategory == null || 
                                         selectedCategory.equals("All Products") || 
                                         product.getCategory().equals(selectedCategory);
                                         
                boolean matchesStockFilter = !lowStockOnly || product.isLowStock();
                
                if (matchesCategory && matchesStockFilter) {
                    productList.add(product);
                }
            }
        }
        updateProductFlowPane(productList);
        if (lblProductCount != null) {
            lblProductCount.setText("Total: " + productList.size());
        }
    }
    
    @FXML
    private void handleRefresh() {
        refreshData();
    }

    // Navigation handlers
    @FXML
    private void handleBack() {
        navigateTo("/View/FXML/homepage.fxml", "Tomas Car Accessories - Dashboard");
    }
    
    @FXML
    private void handleUserManagement() {
        navigateTo("/View/FXML/usermanagement.fxml", "User Management");
    }
    
    @FXML
    private void handleEmployeeManagement() {
        navigateTo("/View/FXML/employeemanagement.fxml", "Employee Management");
    }
    
    @FXML
    private void handleCustomerManagement() {
        navigateTo("/View/FXML/customermanagement.fxml", "Customer Management");
    }
    
    @FXML
    private void handleServiceBooking() {
        navigateTo("/View/FXML/servicebooking.fxml", "Service Booking");
    }
    
    @FXML
    private void handleSalesManagement() {
        navigateTo("/View/FXML/salesmanagement.fxml", "Sales Management");
    }
    
    @FXML
    private void handleTransactions() {
        navigateTo("/View/FXML/recenttransactions.fxml", "Transaction History");
    }
    
    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Confirm Logout");
        alert.setContentText("Are you sure you want to logout?");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            navigateTo("/View/FXML/loginpage.fxml", "Tomas Car Accessories - Login");
        }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage currentStage = (Stage) tvProducts.getScene().getWindow();
            currentStage.getScene().setRoot(root);
            currentStage.setTitle(title);
            currentStage.setMaximized(false);
            currentStage.setWidth(1547);
            currentStage.setHeight(832);
            currentStage.centerOnScreen();
        } catch (Exception e) {
            showError("Error loading page: " + e.getMessage());
        }
    }

    private void handleReorder(Product product) {
        // Change from generic restock to opening StockInDialog specifically for this product
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/stockindialog.fxml"));
            Parent root = loader.load();
            
            Object controller = loader.getController();
            controller.getClass().getMethod("initData", Product.class, InventoryManagementController.class)
                .invoke(controller, product, this);
            
            Stage stage = new Stage();
            stage.setTitle("Reorder / Stock-In Product");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (Exception e) {
            showError("Error opening Stock-In dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showStockAdjustmentDialog(Product product) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(product.getStockQuantity()));
        dialog.setTitle("Adjust Stock");
        dialog.setHeaderText("Adjust stock for: " + product.getProductName());
        dialog.setContentText("New stock quantity:");
        
        dialog.showAndWait().ifPresent(result -> {
            try {
                int newStock = Integer.parseInt(result);
                if (newStock < 0) {
                    showError("Stock quantity cannot be negative!");
                    return;
                }
                
                updateStock(product.getProductId(), newStock);
            } catch (NumberFormatException e) {
                showError("Please enter a valid number!");
            }
        });
    }
    
    private void updateStock(int productId, int newStock) {
        String query = "UPDATE products SET stock_quantity = ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, newStock);
            pstmt.setInt(2, productId);
            pstmt.executeUpdate();
            
            showInfo("Stock updated successfully!");
            loadData();
            
        } catch (SQLException e) {
            showError("Error updating stock: " + e.getMessage());
        }
    }
    
    public void refreshData() {
        loadData();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
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
    
    /**
     * Record a stock OUT transaction (for sales or adjustments)
     * This method is called when stock leaves inventory
     */
    public void recordStockOutTransaction(int productId, int quantity, String referenceNumber, String notes) {
        String query = "INSERT INTO stock_transactions (product_id, transaction_type, quantity, reference_number, user_id, notes) " +
                      "VALUES (?, 'OUT', ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, productId);
            pstmt.setInt(2, quantity);
            pstmt.setString(3, referenceNumber);
            pstmt.setInt(4, Model.UserSession.getInstance().getUserId() > 0 ? Model.UserSession.getInstance().getUserId() : 1);
            pstmt.setString(5, notes);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error recording stock OUT transaction: " + e.getMessage());
        }
    }
    
    /**
     * Get sales count for a product in the last 30 days
     */
    public int getSalesCountLast30Days(int productId) {
        String query = "SELECT SUM(oi.quantity) as total_sales FROM order_items oi " +
                      "JOIN orders o ON oi.product_id = ? AND o.order_id = oi.order_id " +
                      "WHERE datetime(o.order_date) >= datetime('now', '-30 days') " +
                      "AND o.order_status = 'Completed'";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int totalSales = rs.getInt("total_sales");
                return totalSales > 0 ? totalSales : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating sales count: " + e.getMessage());
        }
        
        return 0;
    }
}
