package Controller;
// Force rebuild - POS fixes applied
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.FlowPane;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.*;
import Model.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SalesManagementController {
    
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
    
    @FXML private TextField txtSearchProduct;
    @FXML private Button btnSearchProduct;
    @FXML private ComboBox<String> cmbCategoryFilter;
    @FXML private Button btnAddToCart;
    @FXML private Spinner<Integer> spinnerQuantity;
    
    @FXML private ComboBox<String> cmbPaymentMethod;
    @FXML private TextField txtCustomer;
    @FXML private ListView<String> listCustomerSuggestions;
    
    // Replace TableView with FlowPane
    @FXML private FlowPane flowPaneProducts;
    
    // Cart Table
    @FXML private TableView<CartItem> tableCart;
    @FXML private TableColumn<CartItem, String> colCartProduct;
    @FXML private TableColumn<CartItem, Integer> colCartQty;
    @FXML private TableColumn<CartItem, Double> colCartPrice;
    
    @FXML private Button btnRemoveItem;
    @FXML private Button btnCheckout;
    @FXML private Button btnClearCart;
    @FXML private Button btnScheduleService;
    
    @FXML private Label lblOrderNumber;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTotal;
    @FXML private Label lblVat;
    @FXML private Label lblCartCount;
    @FXML private TextField txtDiscount;
    @FXML private PasswordField txtAdminPassword;
    
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<Product> allProducts = FXCollections.observableArrayList();
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    private String currentOrderNumber;
    private int selectedCustomerId = 0;
    
    // Inner class for cart items
    public static class CartItem {
        private final IntegerProperty productId;
        private final StringProperty productName;
        private final IntegerProperty quantity;
        private final DoubleProperty unitPrice;
        private final DoubleProperty total;
        
        public CartItem(Product product, int quantity) {
            this.productId = new SimpleIntegerProperty(product.getProductId());
            this.productName = new SimpleStringProperty(product.getProductName());
            this.quantity = new SimpleIntegerProperty(quantity);
            this.unitPrice = new SimpleDoubleProperty(product.getPrice());
            this.total = new SimpleDoubleProperty(quantity * product.getPrice());
        }
        
        public CartItem(int productId, String productName, int quantity, double unitPrice) {
            this.productId = new SimpleIntegerProperty(productId);
            this.productName = new SimpleStringProperty(productName);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.unitPrice = new SimpleDoubleProperty(unitPrice);
            this.total = new SimpleDoubleProperty(quantity * unitPrice);
        }
        
        public int getProductId() { return productId.get(); }
        public IntegerProperty productIdProperty() { return productId; }
        
        public String getProductName() { return productName.get(); }
        public StringProperty productNameProperty() { return productName; }
        
        public int getQuantity() { return quantity.get(); }
        public void setQuantity(int qty) { 
            quantity.set(qty); 
            total.set(qty * unitPrice.get());
        }
        public IntegerProperty quantityProperty() { return quantity; }
        
        public double getUnitPrice() { return unitPrice.get(); }
        public DoubleProperty unitPriceProperty() { return unitPrice; }
        
        public double getTotal() { return total.get(); }
        public DoubleProperty totalProperty() { return total; }
    }
    
    private Product selectedProduct = null;
    private List<ProductCardController> productCards = new ArrayList<>();
    
    @FXML
    public void initialize() {
        NavigationManager.applyRoleBasedAccess(this);
        generateOrderNumber();
        setupTableColumns();
        setupCategoryFilter();
        setupSpinner();
        setupPaymentMethods();
        loadProducts();
        loadCustomers();
        setupTableSelectionListeners();
        updateButtonStates();
        setupDiscountListener();
        
        // Hide admin password field if current user is admin
        if ("Admin".equalsIgnoreCase(UserSession.getInstance().getUserRole())) {
            txtAdminPassword.setVisible(false);
            txtAdminPassword.setManaged(false);
        }
    }
    
    private void generateOrderNumber() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        currentOrderNumber = "ORD-" + now.format(formatter);
        lblOrderNumber.setText("Order #: " + currentOrderNumber);
    }
    
    private void setupTableColumns() {
        // Format cart columns
        colCartProduct.setCellValueFactory(cellData -> cellData.getValue().productNameProperty());
        colCartQty.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        colCartPrice.setCellValueFactory(cellData -> cellData.getValue().unitPriceProperty().asObject());
        
        // Link table to the cartItems list
        tableCart.setItems(cartItems);
        
        // Format cart columns
        colCartPrice.setCellFactory(col -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : "₱" + df.format(price));
            }
        });
    }
    
    private void setupCategoryFilter() {
        cmbCategoryFilter.getItems().add("All Categories");
        loadCategories();
        cmbCategoryFilter.getSelectionModel().select("All Categories");
        cmbCategoryFilter.setOnAction(e -> applyFilters());
    }
    
    private void loadCategories() {
        String query = "SELECT DISTINCT category FROM products WHERE category IS NOT NULL ORDER BY category";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String category = rs.getString("category");
                if (category != null && !category.trim().isEmpty()) {
                    cmbCategoryFilter.getItems().add(category);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading categories: " + e.getMessage());
        }
    }
    
    private void setupSpinner() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1);
        spinnerQuantity.setValueFactory(valueFactory);
    }
    
    private void setupPaymentMethods() {
        cmbPaymentMethod.getItems().addAll("Cash", "Credit Card", "Debit Card", "GCash", "PayMaya", "Bank Transfer");
        cmbPaymentMethod.getSelectionModel().select("Cash");
    }
    
    private void setupDiscountListener() {
        txtDiscount.textProperty().addListener((obs, oldVal, newVal) -> {
            calculateTotals();
        });
    }
    
    private void loadProducts() {
        productList.clear();
        String query = "SELECT * FROM Products";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
             
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("product_id"),
                    rs.getInt("category_id"),
                    rs.getString("product_code"),
                    rs.getString("product_name"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getString("brand"),
                    rs.getDouble("price"),
                    rs.getDouble("cost_price"),
                    rs.getInt("stock_quantity"),
                    rs.getInt("reorder_level"),
                    rs.getString("supplier"),
                    rs.getString("location"),
                    rs.getString("barcode"),
                    rs.getString("image_path"),
                    rs.getString("updated_at")
                );
                productList.add(product);
            }
            
            updateProductFlowPane(productList);
            
            // Populate categories dropdown
            Set<String> categories = new HashSet<>();
            for (Product p : productList) {
                if (p.getCategory() != null && !p.getCategory().isEmpty()) {
                    categories.add(p.getCategory());
                }
            }
            cmbCategoryFilter.getItems().addAll(categories);
            
        } catch (SQLException e) {
            showError("Error loading products: " + e.getMessage());
        }
    }
    
    private void updateProductFlowPane(List<Product> products) {
        if (flowPaneProducts == null) return;
        
        flowPaneProducts.getChildren().clear();
        productCards.clear();
        
        for (Product product : products) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/ProductCard.fxml"));
                VBox card = loader.load();
                
                ProductCardController controller = loader.getController();
                controller.setData(product, this);
                productCards.add(controller);
                
                flowPaneProducts.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void selectProductFromCard(Product product) {
        this.selectedProduct = product;
        updateActionButtons();
    }
    
    public void resetCardStylesExcept(ProductCardController activeController) {
        for (ProductCardController card : productCards) {
            if (card != activeController) {
                card.resetStyle();
            }
        }
    }
    
    private void applyFilters() {
        ObservableList<Product> filteredList = FXCollections.observableArrayList();
        String selectedCategory = cmbCategoryFilter.getValue();
        
        for (Product product : allProducts) {
            boolean matchesCategory = selectedCategory == null || 
                                     selectedCategory.equals("All Categories") || 
                                     product.getCategory().equals(selectedCategory);
            
            if (matchesCategory) {
                filteredList.add(product);
            }
        }
        
        updateProductFlowPane(filteredList);
    }
    
    private void loadCustomers() {
        customerList.clear();
        
        String query = "SELECT * FROM customers ORDER BY customer_name";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Customer customer = new Customer(
                    rs.getInt("customer_id"),
                    rs.getString("customer_name"),
                    rs.getString("customer_address"),
                    rs.getString("customer_email"),
                    rs.getString("customer_phone"),
                    "", "", "", "", ""
                );
                customerList.add(customer);
            }
            
            setupCustomerAutocomplete();
            
        } catch (SQLException e) {
            System.err.println("Error loading customers: " + e.getMessage());
        }
    }
    
    private int getOrCreateWalkInCustomerId(Connection conn) throws SQLException {
        // Check if Walk-in Customer exists
        String checkQuery = "SELECT customer_id FROM customers WHERE customer_name = 'Walk-in Customer'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkQuery)) {
            if (rs.next()) {
                return rs.getInt("customer_id");
            }
        }
        
        // If not exists, create it
        String insertQuery = "INSERT INTO customers (customer_name, customer_address, customer_email, customer_phone) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            pstmt.setString(1, "Walk-in Customer");
            pstmt.setString(2, "N/A");
            pstmt.setString(3, "N/A");
            pstmt.setString(4, "N/A");
            pstmt.executeUpdate();
        }
        
        // Get the newly created customer ID
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        throw new SQLException("Failed to create Walk-in Customer");
    }
    
    private javafx.stage.Popup autocompletePopup;
    
    private void setupCustomerAutocomplete() {
        if (autocompletePopup == null) {
            autocompletePopup = new javafx.stage.Popup();
            autocompletePopup.setAutoHide(true);
            
            // Extract from layout so it doesn't push or clip other UI elements
            if (listCustomerSuggestions.getParent() != null) {
                ((javafx.scene.layout.Pane)listCustomerSuggestions.getParent()).getChildren().remove(listCustomerSuggestions);
            }
            
            listCustomerSuggestions.setStyle("-fx-border-color: #CBD5E1; -fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 2);");
            listCustomerSuggestions.setVisible(true);
            listCustomerSuggestions.setManaged(true);
            
            autocompletePopup.getContent().add(listCustomerSuggestions);
            
            // Bind exact width to the textfield
            listCustomerSuggestions.prefWidthProperty().bind(txtCustomer.widthProperty());
        }

        txtCustomer.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                autocompletePopup.hide();
                selectedCustomerId = 0;
                return;
            }

            ObservableList<String> filteredList = FXCollections.observableArrayList();
            
            // Always allow Walk-in
            if ("Walk-in Customer".toLowerCase().contains(newValue.toLowerCase())) {
                filteredList.add("Walk-in Customer");
            }
            
            // Search customers case-insensitively
            for (Customer customer : customerList) {
                if (customer.getCustomerName().toLowerCase().contains(newValue.toLowerCase())) {
                    filteredList.add(customer.getCustomerName());
                }
            }

            if (filteredList.isEmpty()) {
                filteredList.add("No matching customers found");
            }

            listCustomerSuggestions.setItems(filteredList);
            
            // Dynamic height calculation so it perfectly fits the results
            double itemHeight = 26.0; 
            int showingItems = Math.min(filteredList.size(), 6);
            listCustomerSuggestions.setPrefHeight(showingItems * itemHeight + 6);
            
            if (!autocompletePopup.isShowing() && txtCustomer.getScene() != null && txtCustomer.getScene().getWindow() != null) {
                javafx.geometry.Point2D p = txtCustomer.localToScene(0.0, 0.0);
                autocompletePopup.show(txtCustomer,
                        p.getX() + txtCustomer.getScene().getWindow().getX() + txtCustomer.getScene().getX(),
                        p.getY() + txtCustomer.getScene().getWindow().getY() + txtCustomer.getScene().getY() + txtCustomer.getHeight() + 2);
            }
            
            // Automatically capture ID if it matches perfectly
            boolean isExactMatch = false;
            for (Customer c : customerList) {
                if (c.getCustomerName().equalsIgnoreCase(newValue)) {
                    selectedCustomerId = c.getCustomerId();
                    isExactMatch = true;
                    break;
                }
            }
            if (!isExactMatch) selectedCustomerId = 0;
        });

        // Click selection
        listCustomerSuggestions.setOnMouseClicked(event -> {
            String selected = listCustomerSuggestions.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.equals("No matching customers found")) {
                txtCustomer.setText(selected);
                
                if (selected.equals("Walk-in Customer")) {
                    selectedCustomerId = 0;
                } else {
                    for (Customer c : customerList) {
                        if (c.getCustomerName().equals(selected)) {
                            selectedCustomerId = c.getCustomerId();
                            break;
                        }
                    }
                }
            }
            autocompletePopup.hide();
        });
        
        // Keyboard selection logic (Enter and Arrow Keys)
        listCustomerSuggestions.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                String selected = listCustomerSuggestions.getSelectionModel().getSelectedItem();
                if (selected != null && !selected.equals("No matching customers found")) {
                    txtCustomer.setText(selected);
                    if (selected.equals("Walk-in Customer")) {
                        selectedCustomerId = 0;
                    } else {
                        for (Customer c : customerList) {
                            if (c.getCustomerName().equals(selected)) {
                                selectedCustomerId = c.getCustomerId();
                                break;
                            }
                        }
                    }
                }
                autocompletePopup.hide();
            }
        });

        txtCustomer.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.DOWN && autocompletePopup.isShowing()) {
                listCustomerSuggestions.requestFocus();
                listCustomerSuggestions.getSelectionModel().selectFirst();
            }
        });

        txtCustomer.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && !listCustomerSuggestions.isFocused()) {
                autocompletePopup.hide();
            }
        });
        
        txtCustomer.setOnMouseClicked(e -> {
            if (txtCustomer.getText() != null && !txtCustomer.getText().isEmpty() && !listCustomerSuggestions.getItems().isEmpty()) {
                javafx.geometry.Point2D p = txtCustomer.localToScene(0.0, 0.0);
                autocompletePopup.show(txtCustomer,
                        p.getX() + txtCustomer.getScene().getWindow().getX() + txtCustomer.getScene().getX(),
                        p.getY() + txtCustomer.getScene().getWindow().getY() + txtCustomer.getScene().getY() + txtCustomer.getHeight() + 2);
            }
        });
    }
    
    private void setupTableSelectionListeners() {
        // Selection listeners for the product pane are handled inside ProductCardController.
        
        tableCart.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateButtonStates();
        });
        
        // Removed cmbCustomer.setOnAction
    }
    
    private void updateButtonStates() {
        boolean productSelected = selectedProduct != null;
        boolean cartItemSelected = tableCart.getSelectionModel().getSelectedItem() != null;
        
        btnAddToCart.setDisable(!productSelected);
        if (btnRemoveItem != null) btnRemoveItem.setDisable(!cartItemSelected);
        if (btnCheckout != null) btnCheckout.setDisable(cartItems.isEmpty());
        if (btnClearCart != null) btnClearCart.setDisable(cartItems.isEmpty());
        if (btnScheduleService != null) btnScheduleService.setDisable(cartItems.isEmpty());
    }
    
    private void updateActionButtons() {
        boolean productSelected = selectedProduct != null;
        boolean validQuantity = spinnerQuantity.getValue() != null && spinnerQuantity.getValue() > 0;
        
        btnAddToCart.setDisable(!productSelected || !validQuantity);
        
        // Check stock availability
        if (productSelected && spinnerQuantity.getValue() != null) {
            if (spinnerQuantity.getValue() > selectedProduct.getStockQuantity()) {
                btnAddToCart.setDisable(true);
            }
        }
    }
    
    @FXML
    private void handleSearchProduct() {
        String searchText = txtSearchProduct.getText().toLowerCase();
        String selectedCategory = cmbCategoryFilter.getValue();
        
        ObservableList<Product> filteredList = FXCollections.observableArrayList();
        
        for (Product product : productList) {
            boolean matchesSearch = searchText.isEmpty() || 
                                  product.getProductName().toLowerCase().contains(searchText) ||
                                  product.getProductCode().toLowerCase().contains(searchText) ||
                                  (product.getBarcode() != null && product.getBarcode().toLowerCase().contains(searchText));
            
            boolean matchesCategory = selectedCategory == null || 
                                    selectedCategory.equals("All Categories") || 
                                    (product.getCategory() != null && product.getCategory().equals(selectedCategory));
                                    
            if (matchesSearch && matchesCategory) {
                filteredList.add(product);
            }
        }
        
        updateProductFlowPane(filteredList);
        selectedProduct = null;
        updateActionButtons();
    }
    
    @FXML
    private void handleAddToCart() {
        if (selectedProduct == null) {
            showError("Please select a product to add to cart.");
            return;
        }
        
        int quantity = spinnerQuantity.getValue();
        if (quantity <= 0) {
            showError("Please enter a valid quantity.");
            return;
        }
        
        // Check stock availability
        if (quantity > selectedProduct.getStockQuantity()) {
            showError("Only " + selectedProduct.getStockQuantity() + " items available.");
            return;
        }
        
        // Add item to cart
        cartItems.add(new CartItem(selectedProduct, quantity));
        
        // Reset selection and controls
        selectedProduct = null;
        if (flowPaneProducts != null) {
            for (ProductCardController card : productCards) {
                card.resetStyle();
            }
        }
        spinnerQuantity.getValueFactory().setValue(1);
        txtSearchProduct.clear();
        
        // Update display
        // updateCartTable updates automatically because table uses ObservableList
        calculateTotals();
        updateButtonStates();
    }
    
    @FXML
    private void handleRemoveItem() {
        CartItem selected = tableCart.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cartItems.remove(selected);
            calculateTotals();
            updateButtonStates();
        }
    }
    
    @FXML
    private void handleClearCart() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Cart");
        alert.setHeaderText("Clear all items from cart?");
        alert.setContentText("This action cannot be undone.");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            cartItems.clear();
            calculateTotals();
            updateButtonStates();
        }
    }
    
    private void calculateTotals() {
        double subtotal = 0.0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotal();
        }
        
        double discount = 0.0;
        try {
            if (!txtDiscount.getText().trim().isEmpty()) {
                discount = Double.parseDouble(txtDiscount.getText().trim());
            }
        } catch (NumberFormatException e) {
            discount = 0.0;
        }
        
        double discountedAmount = subtotal - discount;
        if (discountedAmount < 0) discountedAmount = 0;
        
        double vat = discountedAmount * 0.12;
        double total = discountedAmount + vat;
        
        lblSubtotal.setText("₱" + df.format(subtotal));
        if (lblVat != null) {
            lblVat.setText("₱" + df.format(vat));
        }
        lblTotal.setText("₱" + df.format(total));
        
        // Update cart count
        if (lblCartCount != null) {
            lblCartCount.setText(String.valueOf(cartItems.size()));
        }
    }
    
    @FXML
    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            showError("Cart is empty!");
            return;
        }
        
        if (cmbPaymentMethod.getValue() == null) {
            showError("Please select a payment method!");
            return;
        }
        
        double discount = 0.0;
        try {
            if (!txtDiscount.getText().trim().isEmpty()) {
                discount = Double.parseDouble(txtDiscount.getText().trim());
            }
        } catch (NumberFormatException e) {
            discount = 0.0;
        }
        
        if (discount > 0 && !"Admin".equalsIgnoreCase(UserSession.getInstance().getUserRole())) {
            String password = txtAdminPassword.getText().trim();
            if (password.isEmpty()) {
                showError("Admin password is required to apply out a discount.");
                return;
            }
            if (!verifyAdminPassword(password)) {
                showError("Invalid admin password! Discount cannot be applied.");
                return;
            }
        }
        
        // Confirm sale
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Complete Sale");
        alert.setHeaderText("Complete this sale?");
        alert.setContentText("Total Amount: " + lblTotal.getText());
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            processSale();
        }
    }
    
    private void processSale() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            conn.setAutoCommit(false);
            
            double subtotal = 0.0;
            for (CartItem item : cartItems) {
                subtotal += item.getTotal();
            }
            
            double discount = 0.0;
            try {
                if (!txtDiscount.getText().trim().isEmpty()) {
                    discount = Double.parseDouble(txtDiscount.getText().trim());
                }
            } catch (NumberFormatException e) {
                discount = 0.0;
            }
            
            double discountedAmount = subtotal - discount;
            if (discountedAmount < 0) discountedAmount = 0;
            
            double vat = discountedAmount * 0.12;
            double total = discountedAmount + vat;
            
            // Get customer ID - use walk-in customer if none selected
            int customerId;
            if (selectedCustomerId > 0) {
                customerId = selectedCustomerId;
            } else {
                customerId = getOrCreateWalkInCustomerId(conn);
            }
            
            // Insert order
            String orderQuery = "INSERT INTO orders (order_number, customer_id, user_id, order_date, total_amount, discount_amount, tax_amount, final_amount, payment_method, payment_status, order_status) " +
                               "VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, 'Paid', 'Completed')";
            
            PreparedStatement orderStmt = conn.prepareStatement(orderQuery);
            orderStmt.setString(1, currentOrderNumber);
            orderStmt.setInt(2, customerId);
            orderStmt.setObject(3, UserSession.getInstance().getUserId());
            orderStmt.setDouble(4, subtotal);
            orderStmt.setDouble(5, discount);
            orderStmt.setDouble(6, vat);
            orderStmt.setDouble(7, total);
            orderStmt.setString(8, cmbPaymentMethod.getValue());
            
            orderStmt.executeUpdate();
            orderStmt.close();
            
            // Get generated order ID using SQLite's last_insert_rowid()
            int orderId = 0;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
            if (rs.next()) {
                orderId = rs.getInt(1);
            }
            rs.close();
            stmt.close();
            
            // Insert order items and update stock
            String itemQuery = "INSERT INTO order_items (order_id, product_id, quantity, price, product_name, unit_price, subtotal, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            String stockQuery = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
            
            PreparedStatement itemStmt = conn.prepareStatement(itemQuery);
            PreparedStatement stockStmt = conn.prepareStatement(stockQuery);
            
            for (CartItem item : cartItems) {
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setInt(3, item.getQuantity());
                itemStmt.setDouble(4, item.getUnitPrice());
                itemStmt.setString(5, item.getProductName());
                itemStmt.setDouble(6, item.getUnitPrice());
                itemStmt.setDouble(7, item.getTotal());
                itemStmt.setDouble(8, item.getTotal());
                itemStmt.executeUpdate();
                
                stockStmt.setInt(1, item.getQuantity());
                stockStmt.setInt(2, item.getProductId());
                stockStmt.executeUpdate();
            }
            
            conn.commit();
            
            // Log activity for sale
            String userName = UserSession.getInstance().getUsername();
            String description = String.format("Processed sale of ₱%.2f with order number %s", total, currentOrderNumber);
            ActivityManager.logActivity("SALE_PROCESSED", description, userName);
            
            showInfo("Sale completed successfully!\nOrder Number: " + currentOrderNumber);
            
            // Reset form
            resetForm();
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            showError("Error processing sale: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private boolean verifyAdminPassword(String password) {
        String query = "SELECT * FROM Users WHERE role = 'Admin' AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private void resetForm() {
        cartItems.clear();
        txtDiscount.setText("0.00");
        txtAdminPassword.clear();
        txtCustomer.clear();
        txtCustomer.setPromptText("Walk-in Customer");
        selectedCustomerId = 0;
        calculateTotals();
        loadProducts();
        generateOrderNumber();
        updateButtonStates();
    }
    
    @FXML
    private void handleNewCustomer() {
        showInfo("Quick customer registration feature coming soon!");
    }
    
    @FXML
    private void handleBack() {
        navigateTo("/View/FXML/homepage.fxml", "Tomas Car Accessories - Dashboard");
    }
    
    private void navigateTo(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage currentStage = (Stage) btnDashboard.getScene().getWindow();
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
    
    @FXML
    private void handleUserManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/usermanagement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnCheckout.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("User Management");
        } catch (Exception e) {
            showError("Error loading User Management: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCustomerManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/customermanagement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnCheckout.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Customer Management");
        } catch (Exception e) {
            showError("Error loading Customer Management: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleEmployeeManagement() {
        navigateTo("/View/FXML/employeemanagement.fxml", "Employee Management");
    }

    @FXML
    private void handleInventoryManagement() {
        navigateTo("/View/FXML/inventorymanagement.fxml", "Inventory Management");
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

    @FXML
    private void handleServiceBooking() {
        navigateTo("/View/FXML/servicebooking.fxml", "Service Booking");
    }

    @FXML
    private void handleScheduleService() {
        if (cartItems.isEmpty()) {
            showError("Please add items to cart first before scheduling service.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/servicebookingdialog.fxml"));
            VBox page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Schedule Service");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            
            // Set owner to the current window
            if (btnScheduleService != null && btnScheduleService.getScene() != null) {
                dialogStage.initOwner(btnScheduleService.getScene().getWindow());
            }
            
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            ServiceBookingDialogController controller = loader.getController();
            
            // Pass pos data to the booking dialog
            int customerId = selectedCustomerId; // Updated reference
            
            double totalAmount = 0.0;
            double subtotalAmount = 0.0;
            double discountAmount = 0.0;
            double taxAmount = 0.0;
            try {
                totalAmount = Double.parseDouble(lblTotal.getText().replace(",", "").replace("₱", "").trim());
                subtotalAmount = Double.parseDouble(lblSubtotal.getText().replace(",", "").replace("₱", "").trim());
                taxAmount = Double.parseDouble(lblVat.getText().replace(",", "").replace("₱", "").trim());
                if (!txtDiscount.getText().trim().isEmpty()) {
                    discountAmount = Double.parseDouble(txtDiscount.getText().replace(",", "").replace("₱", "").trim());
                }
            } catch (NumberFormatException ignored) {}
            
            // Initialize from POS without needing parentController for reloading
            controller.setFromPOSData(customerId, cartItems, totalAmount, subtotalAmount, discountAmount, taxAmount);

            dialogStage.showAndWait();
            
            // Refresh sales data after service booking dialog closes
            refreshData();

        } catch (Exception e) {
            showError("Error opening service booking dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void refreshData() {
        loadProducts();
        loadCustomers();
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
