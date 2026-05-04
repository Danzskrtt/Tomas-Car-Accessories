package Controller;

import Model.Product;
import Model.ServiceBooking;
import Model.UserSession;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ServiceCheckoutDialogController {

    @FXML private Label lblBookingNumber;
    @FXML private Label lblCustomerName;
    @FXML private Label lblCarDetails;

    @FXML private TextField txtSearchProduct;
    @FXML private javafx.scene.layout.FlowPane flowProducts;

    @FXML private TableView<CartItem> tblCart;
    @FXML private TableColumn<CartItem, String> colCartItem;
    @FXML private TableColumn<CartItem, Integer> colCartQty;
    @FXML private TableColumn<CartItem, Double> colCartPrice;
    @FXML private TableColumn<CartItem, Double> colCartTotal;

    @FXML private Button btnRemoveItem;
    
    @FXML private Label lblServicesTotal;
    @FXML private Label lblProductsTotal;
    @FXML private Label lblSubtotal;
    @FXML private TextField txtDiscount;
    @FXML private PasswordField txtAdminPassword;
    @FXML private Label lblVat;
    @FXML private Label lblDownpayment;
    @FXML private Label lblGrandTotal;
    @FXML private Label lblBalanceDue;

    @FXML private ComboBox<String> cmbPaymentMethod;
    @FXML private Button btnComplete;
    @FXML private Button btnCancel;

    private ServiceBooking booking;
    private ServiceBookingController parentController;
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    private DecimalFormat df = new DecimalFormat("#,##0.00");

    private ObservableList<Product> searchResults = FXCollections.observableArrayList();
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    
    private double servicesTotal = 0.0;

    public static class CartItem {
        private final IntegerProperty productId;
        private final StringProperty itemName;
        private final IntegerProperty quantity;
        private final DoubleProperty unitPrice;
        private final DoubleProperty total;
        private final BooleanProperty isService;

        public CartItem(int productId, String itemName, int quantity, double unitPrice, boolean isService) {
            this.productId = new SimpleIntegerProperty(productId);
            this.itemName = new SimpleStringProperty(itemName);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.unitPrice = new SimpleDoubleProperty(unitPrice);
            this.total = new SimpleDoubleProperty(quantity * unitPrice);
            this.isService = new SimpleBooleanProperty(isService);
        }

        public int getProductId() { return productId.get(); }
        public String getItemName() { return itemName.get(); }
        public StringProperty itemNameProperty() { return itemName; }
        public int getQuantity() { return quantity.get(); }
        public IntegerProperty quantityProperty() { return quantity; }
        public double getUnitPrice() { return unitPrice.get(); }
        public DoubleProperty unitPriceProperty() { return unitPrice; }
        public double getTotal() { return total.get(); }
        public DoubleProperty totalProperty() { return total; }
        public boolean isService() { return isService.get(); }
        
        public void setQuantity(int qty) {
            this.quantity.set(qty);
            this.total.set(qty * getUnitPrice());
        }
    }

    @FXML
    public void initialize() {
        setupTables();
        setupSearch();
        setupDiscountListener();
        
        cmbPaymentMethod.setItems(FXCollections.observableArrayList("Cash", "Credit Card", "Debit Card", "GCash", "PayMaya", "Bank Transfer"));
        cmbPaymentMethod.setValue("Cash");
        
        if ("Admin".equalsIgnoreCase(UserSession.getInstance().getUserRole())) {
            txtAdminPassword.setVisible(false);
            txtAdminPassword.setManaged(false);
        }
    }

    private void setupTables() {
        colCartItem.setCellValueFactory(cellData -> cellData.getValue().itemNameProperty());
        colCartQty.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        colCartPrice.setCellValueFactory(cellData -> cellData.getValue().unitPriceProperty().asObject());
        colCartTotal.setCellValueFactory(cellData -> cellData.getValue().totalProperty().asObject());
        
        // Setup Quantity Action Cell with +/- buttons
        colCartQty.setCellFactory(col -> new TableCell<CartItem, Integer>() {
            private final Button btnMinus = new Button("-");
            private final Button btnPlus = new Button("+");
            private final Label lblQty = new Label();
            private final javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5, btnMinus, lblQty, btnPlus);

            {
                btnMinus.setStyle("-fx-cursor: hand; -fx-padding: 2 6; -fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
                btnPlus.setStyle("-fx-cursor: hand; -fx-padding: 2 6; -fx-background-color: #d1fae5; -fx-text-fill: #059669; -fx-font-weight: bold;");
                hbox.setAlignment(javafx.geometry.Pos.CENTER);

                btnMinus.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    if (!item.isService()) {
                        int newQty = item.getQuantity() - 1;
                        if (newQty <= 0) {
                            cartItems.remove(item);
                        } else {
                            item.setQuantity(newQty);
                            tblCart.refresh();
                        }
                        calculateTotals();
                    }
                });

                btnPlus.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    if (!item.isService()) {
                        // verify stock logic -> we don't have access to max stock easily here without checking db.
                        // I will fetch stock inside just to be sure or simply allow and rely on checkout validation.
                        // Better to check list from search results or db directly.
                        int currentStock = 999;
                        try (Connection conn = DriverManager.getConnection(DB_URL);
                             PreparedStatement pstmt = conn.prepareStatement("SELECT stock_quantity FROM Products WHERE product_id=?")) {
                             pstmt.setInt(1, item.getProductId());
                             ResultSet rs = pstmt.executeQuery();
                             if (rs.next()) currentStock = rs.getInt("stock_quantity");
                        } catch (Exception ex) {}
                        
                        if (item.getQuantity() < currentStock) {
                            item.setQuantity(item.getQuantity() + 1);
                            tblCart.refresh();
                            calculateTotals();
                        } else {
                            showAlert("Stock Limit", "Cannot add more of " + item.getItemName(), Alert.AlertType.WARNING);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Integer qty, boolean empty) {
                super.updateItem(qty, empty);
                if (empty || qty == null) {
                    setGraphic(null);
                } else {
                    CartItem item = getTableView().getItems().get(getIndex());
                    if (item.isService()) {
                        lblQty.setText(String.valueOf(qty));
                        setGraphic(lblQty);
                    } else {
                        lblQty.setText(String.valueOf(qty));
                        setGraphic(hbox);
                    }
                }
            }
        });

        // Format prices
        colCartPrice.setCellFactory(col -> new TableCell<CartItem, Double>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : "₱" + df.format(price));
            }
        });
        colCartTotal.setCellFactory(col -> new TableCell<CartItem, Double>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : "₱" + df.format(price));
            }
        });

        tblCart.setItems(cartItems);
        tblCart.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            btnRemoveItem.setDisable(newVal == null || newVal.isService());
        });
    }

    private void setupSearch() {
        txtSearchProduct.textProperty().addListener((obs, oldVal, newVal) -> {
            searchProducts(newVal);
        });
    }
    
    private void setupDiscountListener() {
        txtDiscount.textProperty().addListener((obs, oldVal, newVal) -> {
            calculateTotals();
        });
    }

    public void initData(ServiceBooking booking, ServiceBookingController parentController) {
        this.booking = booking;
        this.parentController = parentController;

        lblBookingNumber.setText("Booking #" + booking.getBookingNumber());
        lblCustomerName.setText(booking.getCustomerName());
        lblCarDetails.setText(booking.getCarDescription());
        
        // Fetch existing discount to start with
        double existingDiscount = 0.0;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT discount_amount FROM service_bookings WHERE booking_id = ?")) {
            pstmt.setInt(1, booking.getBookingId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                existingDiscount = rs.getDouble("discount_amount");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        txtDiscount.setText(String.format("%.2f", existingDiscount));

        loadServicesAndProducts();
    }

    private void loadServicesAndProducts() {
        cartItems.clear();
        servicesTotal = 0.0;
        
        // Add services as line items
        if (booking.getServiceType() != null && !booking.getServiceType().isEmpty()) {
            String[] services = booking.getServiceType().split(", ");
            for (String s : services) {
                double fee = getServiceFee(s);
                cartItems.add(new CartItem(0, "[Service] " + s, 1, fee, true));
                servicesTotal += fee;
            }
        }
        
        // Add existing products
        String query = "SELECT bp.product_id, bp.product_name, bp.quantity, bp.unit_price FROM booking_products bp WHERE bp.booking_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, booking.getBookingId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                cartItems.add(new CartItem(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("unit_price"),
                    false
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        calculateTotals();
    }

    private double getServiceFee(String serviceName) {
        double fee = 0.0;
        String query = "SELECT base_fee FROM service_fees WHERE service_name = ? COLLATE NOCASE";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, serviceName.trim());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                fee = rs.getDouble("base_fee");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fee;
    }

    private void searchProducts(String searchText) {
        flowProducts.getChildren().clear();
        if (searchText == null || searchText.trim().isEmpty()) {
            return;
        }
        
        String query = "SELECT * FROM Products WHERE (product_name LIKE ? OR product_code LIKE ?) AND stock_quantity > 0 LIMIT 20";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "%" + searchText.trim() + "%");
            pstmt.setString(2, "%" + searchText.trim() + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Product p = new Product(
                    rs.getInt("product_id"), rs.getInt("category_id"), rs.getString("product_code"),
                    rs.getString("product_name"), rs.getString("description"), rs.getString("category"),
                    rs.getString("brand"), rs.getDouble("price"), rs.getDouble("cost_price"),
                    rs.getInt("stock_quantity"), rs.getInt("reorder_level"), rs.getString("supplier"),
                    rs.getString("location"), rs.getString("barcode"), rs.getString("image_path"),
                    rs.getString("updated_at")
                );
                flowProducts.getChildren().add(createProductCard(p));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private javafx.scene.layout.VBox createProductCard(Product p) {
        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(5);
        card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #cbd5e1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 2);");
        card.setPrefWidth(160);
        card.setPrefHeight(180);
        card.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        
        javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView();
        imgView.setFitHeight(80);
        imgView.setFitWidth(80);
        imgView.setPreserveRatio(true);
        if (p.getImagePath() != null && !p.getImagePath().trim().isEmpty()) {
            // First check if the image path is already absolute, otherwise assume relative to product_images
            java.io.File file = new java.io.File(p.getImagePath());
            if (!file.exists()) {
                file = new java.io.File("product_images/" + p.getImagePath());
            }
            if (file.exists()) {
                imgView.setImage(new javafx.scene.image.Image(file.toURI().toString()));
            } else {
                // Ignore missing files gracefully
            }
        }
        
        Label lblName = new Label(p.getProductName());
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        lblName.setWrapText(true);
        lblName.setMaxWidth(140);
        lblName.setAlignment(javafx.geometry.Pos.CENTER);
        lblName.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        Label lblPrice = new Label("₱" + df.format(p.getPrice()));
        lblPrice.setStyle("-fx-font-weight: bold; -fx-text-fill: #059669;");
        
        Label lblStock = new Label("Stock: " + p.getStockQuantity());
        lblStock.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Button btnAdd = new Button("+ Add");
        btnAdd.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;");
        btnAdd.setMaxWidth(Double.MAX_VALUE);
        
        if (p.getStockQuantity() <= 0) {
            btnAdd.setDisable(true);
            btnAdd.setText("Out of Stock");
            lblStock.setStyle("-fx-font-size: 11px; -fx-text-fill: #ef4444;");
        }
        
        btnAdd.setOnAction(e -> {
            addToCart(p);
            // Highlight effect briefly
            String origStyle = card.getStyle();
            card.setStyle(origStyle + "-fx-border-color: #0ea5e9; -fx-background-color: #f0f9ff;");
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(200));
            pause.setOnFinished(evt -> card.setStyle(origStyle));
            pause.play();
        });
        
        card.getChildren().addAll(imgView, lblName, lblPrice, lblStock, spacer, btnAdd);
        return card;
    }

    private void addToCart(Product product) {
        for (CartItem item : cartItems) {
            if (!item.isService() && item.getProductId() == product.getProductId()) {
                if (item.getQuantity() < product.getStockQuantity()) {
                    item.setQuantity(item.getQuantity() + 1);
                    tblCart.refresh();
                    calculateTotals();
                } else {
                    showAlert("Stock Limit", "Cannot add more of " + product.getProductName(), Alert.AlertType.WARNING);
                }
                return;
            }
        }
        
        cartItems.add(new CartItem(product.getProductId(), product.getProductName(), 1, product.getPrice(), false));
        calculateTotals();
    }

    @FXML
    private void handleRemoveItem() {
        CartItem selected = tblCart.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.isService()) {
            cartItems.remove(selected);
            calculateTotals();
        }
    }

    private void calculateTotals() {
        double productsTotal = 0.0;
        double subtotal = 0.0;
        
        for (CartItem item : cartItems) {
            if (!item.isService()) {
                productsTotal += item.getTotal();
            }
            subtotal += item.getTotal();
        }
        
        double discount = 0.0;
        try {
            if (!txtDiscount.getText().trim().isEmpty()) {
                discount = Double.parseDouble(txtDiscount.getText().trim());
            }
        } catch (NumberFormatException ignored) {}
        
        double discountedAmount = subtotal - discount;
        if (discountedAmount < 0) discountedAmount = 0;
        
        double vat = discountedAmount * 0.12;
        double grandTotal = discountedAmount + vat;
        
        double downpayment = 0.0;
        if (booking != null) {
            downpayment = booking.getDownpayment();
        }
        
        double balanceDue = grandTotal - downpayment;
        if (balanceDue < 0) balanceDue = 0;
        
        lblServicesTotal.setText("₱" + df.format(servicesTotal));
        lblProductsTotal.setText("₱" + df.format(productsTotal));
        lblSubtotal.setText("₱" + df.format(subtotal));
        lblVat.setText("₱" + df.format(vat));
        if (lblDownpayment != null) lblDownpayment.setText("₱" + df.format(downpayment));
        lblGrandTotal.setText("₱" + df.format(grandTotal));
        if (lblBalanceDue != null) lblBalanceDue.setText("₱" + df.format(balanceDue));
    }

    @FXML
    private void handleComplete() {
        if (cartItems.isEmpty()) {
            showAlert("Error", "No items to checkout.", Alert.AlertType.ERROR);
            return;
        }

        double discount = 0.0;
        try {
            if (!txtDiscount.getText().trim().isEmpty()) {
                discount = Double.parseDouble(txtDiscount.getText().trim());
            }
        } catch (NumberFormatException ignored) {}

        if (discount > 0 && !"Admin".equalsIgnoreCase(UserSession.getInstance().getUserRole())) {
            String password = txtAdminPassword.getText().trim();
            if (password.isEmpty() || !verifyAdminPassword(password)) {
                showAlert("Unauthorized", "Invalid admin password for discount.", Alert.AlertType.ERROR);
                return;
            }
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Complete Checkout");
        confirm.setHeaderText("Finalize Service Booking?");
        confirm.setContentText("This will mark the service as Completed and deduct inventory stock. Total: " + lblGrandTotal.getText());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            processCheckout(discount);
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

    private void processCheckout(double discount) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            conn.setAutoCommit(false);
            
            double subtotal = 0.0;
            for (CartItem item : cartItems) {
                subtotal += item.getTotal();
            }
            
            double discountedAmount = Math.max(0, subtotal - discount);
            double vat = discountedAmount * 0.12;
            double grandTotal = discountedAmount + vat;
            
            String paymentMethod = cmbPaymentMethod.getValue();
            
            // Update booking status and financials
            String updateBooking = "UPDATE service_bookings SET status = 'Completed', completed_at = CURRENT_TIMESTAMP, " +
                                 "estimated_cost = ?, actual_cost = ?, subtotal_amount = ?, discount_amount = ?, tax_amount = ?, " +
                                 "payment_method = ?, balance = 0 WHERE booking_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateBooking)) {
                pstmt.setDouble(1, grandTotal);
                pstmt.setDouble(2, grandTotal);
                pstmt.setDouble(3, subtotal);
                pstmt.setDouble(4, discount);
                pstmt.setDouble(5, vat);
                pstmt.setString(6, paymentMethod);
                pstmt.setInt(7, booking.getBookingId());
                pstmt.executeUpdate();
            }

            // Sync booking_products table and update inventory (only for new added products)
            // First, delete old products for this booking
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM booking_products WHERE booking_id = ?")) {
                pstmt.setInt(1, booking.getBookingId());
                pstmt.executeUpdate();
            }

            String insertBp = "INSERT INTO booking_products (booking_id, product_id, product_name, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?, ?)";
            String updateInv = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
            
            try (PreparedStatement insertStmt = conn.prepareStatement(insertBp);
                 PreparedStatement invStmt = conn.prepareStatement(updateInv)) {
                 
                for (CartItem item : cartItems) {
                    if (!item.isService()) {
                        insertStmt.setInt(1, booking.getBookingId());
                        insertStmt.setInt(2, item.getProductId());
                        insertStmt.setString(3, item.getItemName());
                        insertStmt.setInt(4, item.getQuantity());
                        insertStmt.setDouble(5, item.getUnitPrice());
                        insertStmt.setDouble(6, item.getTotal());
                        insertStmt.executeUpdate();

                        // Deduct stock
                        invStmt.setInt(1, item.getQuantity());
                        invStmt.setInt(2, item.getProductId());
                        invStmt.executeUpdate();
                    }
                }
            }
            
            conn.commit();
            
            showAlert("Success", "Service checkout completed successfully!", Alert.AlertType.INFORMATION);
            if (parentController != null) {
                parentController.loadBookings();
            }
            handleCancel();
            
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            showAlert("Error", "Checkout failed: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
