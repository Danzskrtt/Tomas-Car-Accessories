package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import Model.Order;
import Model.OrderItem;
import Model.Customer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecentTransactionsController {
    
    // Navigation buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnUserManagement;
    @FXML private Button btnEmployeeManagement;
    @FXML private Button btnCustomerManagement;
    @FXML private Button btnServiceBooking;
    @FXML private Button btnInventory;
    @FXML private Button btnSales;
    @FXML private Button btnTransactions;
    @FXML private Button btnLogout;
    
    // Filter Controls
    @FXML private TextField txtSearchOrderNumber;
    @FXML private TextField txtCustomerFilter;
    @FXML private ComboBox<String> cmbPaymentMethodFilter;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private Button btnFilter;
    @FXML private Button btnClearFilter;
    
    // Table and Columns
    @FXML private TableView<Order> tableTransactions;
    @FXML private TableColumn<Order, String> colOrderNumber;
    @FXML private TableColumn<Order, String> colOrderDate;
    @FXML private TableColumn<Order, String> colCustomerName;
    @FXML private TableColumn<Order, Double> colTotalAmount;
    @FXML private TableColumn<Order, Double> colDiscountAmount;
    @FXML private TableColumn<Order, Double> colTaxAmount;
    @FXML private TableColumn<Order, Double> colFinalAmount;
    @FXML private TableColumn<Order, String> colPaymentMethod;
    @FXML private TableColumn<Order, String> colPaymentStatus;
    @FXML private TableColumn<Order, String> colUserName;
    
    // Summary Labels
    @FXML private Label lblTransactionCount;
    @FXML private Label lblTotalSales;
    @FXML private Label lblTotalDiscount;
    @FXML private Label lblTotalTax;
    @FXML private Label lblNetTotal;
    
    // Action Buttons
    @FXML private Button btnViewDetails;
    @FXML private Button btnReprintReceipt;
    @FXML private Button btnExportToExcel;
    @FXML private Button btnClose;
    
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    private ObservableList<Order> transactionList = FXCollections.observableArrayList();
    private ObservableList<Order> allTransactions = FXCollections.observableArrayList();
    private Map<Integer, String> customerMap = new HashMap<>();
    private List<String> allCustomerNames = new ArrayList<>();
    private ContextMenu customerSuggestions = new ContextMenu();
    
    private Runnable refreshCallback;
    
    @FXML
    public void initialize() {
        NavigationManager.applyRoleBasedAccess(this);
        System.out.println("RecentTransactionsController initializing...");
        
        setupTableColumns();
        setupFilterControls();
        loadCustomers();
        
        // Add listeners to table selection
        tableTransactions.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                // Future use: load item details if needed for a details pane
            }
        });

        loadRecentTransactions();
        
        // Register to listen for global refresh
        refreshCallback = () -> {
            System.out.println("Global refresh triggered: Updating Recent Transactions data");
            loadRecentTransactions();
        };
        Model.SharedDataModel.getInstance().addRefreshCallback(refreshCallback);
    }

    private void loadRecentTransactions() {
        transactionList.clear();
        allTransactions.clear();
        
        String query = "SELECT o.order_id, o.order_number, o.customer_id, o.user_id, " +
                      "o.order_date, o.total_amount, o.discount_amount, o.final_amount, " +
                      "o.payment_method, o.payment_status, o.order_status, o.notes, o.updated_at, " +
                      "c.customer_name, u.username " +
                      "FROM orders o " +
                      "LEFT JOIN customers c ON o.customer_id = c.customer_id " +
                      "LEFT JOIN users u ON o.user_id = u.user_id " +
                      "UNION ALL " +
                      "SELECT sb.booking_id as order_id, sb.booking_number as order_number, sb.customer_id, sb.user_id, " +
                      "sb.booking_date as order_date, COALESCE(sb.subtotal_amount, ROUND(sb.estimated_cost / 1.12, 2)) as total_amount, COALESCE(sb.discount_amount, 0) as discount_amount, sb.estimated_cost as final_amount, " +
                      "COALESCE(sb.payment_method, 'Cash') as payment_method, sb.status as payment_status, sb.status as order_status, sb.notes, sb.updated_at, " +
                      "c.customer_name, COALESCE(u.username, 'Admin') as username " +
                      "FROM service_bookings sb " + 
                      "LEFT JOIN customers c ON sb.customer_id = c.customer_id " +
                      "LEFT JOIN users u ON sb.user_id = u.user_id " +
                      "WHERE sb.status = 'Completed' " +
                      "ORDER BY order_date DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                double totalAmount = rs.getDouble("total_amount");
                double discountAmount = rs.getDouble("discount_amount");
                double finalAmount = rs.getDouble("final_amount");
                double taxAmount = 0.0;
                
                // Standard order calculation handles both POS orders and properly fetched service_bookings now
                taxAmount = finalAmount - (totalAmount - discountAmount);
                if (taxAmount < 0) taxAmount = 0.0;
                
                Order order = new Order(
                    rs.getInt("order_id"),
                    rs.getString("order_number"),
                    rs.getInt("customer_id"),
                    rs.getInt("user_id"),
                    rs.getString("order_date"),
                    totalAmount,
                    discountAmount,
                    taxAmount,
                    finalAmount,
                    rs.getString("payment_method"),
                    rs.getString("payment_status"),
                    rs.getString("order_status"),
                    rs.getString("notes"),
                    rs.getString("updated_at"),
                    rs.getString("customer_name"),
                    rs.getString("username")
                );
                allTransactions.add(order);
                transactionList.add(order);
            }
            
            updateSummaryStats();
            
        } catch (SQLException e) {
            showError("Error loading transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupTableColumns() {
        // Bind columns to Order properties
        colOrderNumber.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colOrderDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colTotalAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colDiscountAmount.setCellValueFactory(new PropertyValueFactory<>("discountAmount"));
        colTaxAmount.setCellValueFactory(new PropertyValueFactory<>("taxAmount"));
        colFinalAmount.setCellValueFactory(new PropertyValueFactory<>("finalAmount"));
        colPaymentMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colPaymentStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        colUserName.setCellValueFactory(new PropertyValueFactory<>("userName"));
        
        // Format currency columns
        formatCurrencyColumn(colTotalAmount);
        formatCurrencyColumn(colDiscountAmount);
        formatCurrencyColumn(colTaxAmount);
        formatCurrencyColumn(colFinalAmount);
        
        tableTransactions.setItems(transactionList);
        
        // Set placeholder for empty table
        tableTransactions.setPlaceholder(new Label("No transactions found"));
    }
    
    private void formatCurrencyColumn(TableColumn<Order, Double> column) {
        column.setCellFactory(col -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText("₱" + df.format(value));
                }
            }
        });
    }
    
    private void setupFilterControls() {
        // Setup payment method filter
        cmbPaymentMethodFilter.getItems().addAll("All Methods", "Cash", "Credit Card", "Debit Card", "GCash", "PayMaya");
        cmbPaymentMethodFilter.getSelectionModel().selectFirst();
        
        // Setup customer filter autocomplete
        txtCustomerFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                customerSuggestions.hide();
                return;
            }
            
            List<String> filtered = new ArrayList<>();
            String lowerCaseFilter = newValue.toLowerCase();
            for (String name : allCustomerNames) {
                if (name.toLowerCase().contains(lowerCaseFilter)) {
                    filtered.add(name);
                }
            }
            
            customerSuggestions.getItems().clear();
            if (filtered.isEmpty()) {
                MenuItem noMatch = new MenuItem("No customer found");
                noMatch.setDisable(true);
                customerSuggestions.getItems().add(noMatch);
            } else {
                for (String name : filtered) {
                    MenuItem item = new MenuItem(name);
                    item.setOnAction(event -> {
                        txtCustomerFilter.setText(name);
                        customerSuggestions.hide();
                    });
                    customerSuggestions.getItems().add(item);
                }
            }
            
            // Show suggestions under the text field
            if (!customerSuggestions.isShowing()) {
                customerSuggestions.show(txtCustomerFilter, javafx.geometry.Side.BOTTOM, 0, 0);
            }
        });
        
        // Handle keyboard events: Escape to close suggestions
        txtCustomerFilter.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                customerSuggestions.hide();
            }
        });
        
        // Hide when focus is lost
        txtCustomerFilter.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                customerSuggestions.hide();
            }
        });
    }
    
    private void loadCustomers() {
        allCustomerNames.clear();
        customerMap.clear();
        
        String query = "SELECT customer_id, customer_name FROM customers ORDER BY customer_name";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                int customerId = rs.getInt("customer_id");
                String customerName = rs.getString("customer_name");
                customerMap.put(customerId, customerName);
                allCustomerNames.add(customerName);
            }
            
        } catch (SQLException e) {
            showError("Error loading customers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleFilter() {
        transactionList.clear();
        
        String searchText = txtSearchOrderNumber.getText().trim().toLowerCase();
        String selectedCustomer = txtCustomerFilter.getText().trim();
        String selectedPaymentMethod = cmbPaymentMethodFilter.getValue();
        LocalDate fromDate = dateFrom.getValue();
        LocalDate toDate = dateTo.getValue();
        
        for (Order order : allTransactions) {
            boolean matches = true;
            
            // Filter by order number
            if (!searchText.isEmpty() && !order.getOrderNumber().toLowerCase().contains(searchText)) {
                matches = false;
            }
            
            // Filter by customer
            if (!selectedCustomer.isEmpty()) {
                if (order.getCustomerName() == null || !order.getCustomerName().toLowerCase().contains(selectedCustomer.toLowerCase())) {
                    matches = false;
                }
            }
            
            // Filter by payment method
            if (selectedPaymentMethod != null && !selectedPaymentMethod.equals("All Methods")) {
                if (order.getPaymentMethod() == null || !order.getPaymentMethod().equals(selectedPaymentMethod)) {
                    matches = false;
                }
            }
            
            // Filter by date range
            if (fromDate != null || toDate != null) {
                try {
                    String orderDateStr = order.getOrderDate();
                    if (orderDateStr != null && !orderDateStr.isEmpty()) {
                        // Parse the date (assuming format: yyyy-MM-dd HH:mm:ss)
                        LocalDate orderDate = LocalDate.parse(orderDateStr.substring(0, 10));
                        
                        if (fromDate != null && orderDate.isBefore(fromDate)) {
                            matches = false;
                        }
                        
                        if (toDate != null && orderDate.isAfter(toDate)) {
                            matches = false;
                        }
                    }
                } catch (Exception e) {
                    // If date parsing fails, skip this filter
                }
            }
            
            if (matches) {
                transactionList.add(order);
            }
        }
        
        updateSummaryStats();
    }
    
    @FXML
    private void handleClearFilter() {
        txtSearchOrderNumber.clear();
        txtCustomerFilter.clear();
        cmbPaymentMethodFilter.getSelectionModel().selectFirst();
        dateFrom.setValue(null);
        dateTo.setValue(null);
        
        transactionList.clear();
        transactionList.addAll(allTransactions);
        updateSummaryStats();
    }
    
    @FXML
    private void handleViewDetails() {
        Order selectedOrder = tableTransactions.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showWarning("Please select a transaction to view details.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Transaction Details");
        alert.setHeaderText("Order # " + selectedOrder.getOrderNumber());

        StringBuilder details = new StringBuilder();
        details.append("Customer: ").append(selectedOrder.getCustomerName() != null ? selectedOrder.getCustomerName() : "Walk-in").append("\n");
        details.append("Date: ").append(selectedOrder.getOrderDate()).append("\n");
        details.append("Subtotal: ₱").append(String.format("%.2f", selectedOrder.getTotalAmount())).append("\n");
        details.append("Discount: ₱").append(String.format("%.2f", selectedOrder.getDiscountAmount())).append("\n");
        details.append("Tax: ₱").append(String.format("%.2f", selectedOrder.getTaxAmount())).append("\n");
        details.append("Total: ₱").append(String.format("%.2f", selectedOrder.getFinalAmount())).append("\n");
        details.append("Payment Method: ").append(selectedOrder.getPaymentMethod()).append("\n");
        details.append("Status: ").append(selectedOrder.getPaymentStatus()).append("\n");
        details.append("Cashier: ").append(selectedOrder.getUserName()).append("\n");

        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    @FXML
    private void handleReprintReceipt() {
        Order selectedOrder = tableTransactions.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showWarning("Please select a transaction to reprint the receipt.");
            return;
        }
        
        try {
            // Load order items for receipt
            List<OrderItem> orderItems = loadOrderItems(selectedOrder.getOrderId());
            
            // Generate receipt
            generateReceipt(selectedOrder, orderItems);
            showInfo("Receipt reprinted successfully!");
            
        } catch (Exception e) {
            showError("Error reprinting receipt: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportToExcel() {
        if (transactionList.isEmpty()) {
            showWarning("No transactions to export.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Transaction Report");
        fileChooser.setInitialFileName("transactions_" + LocalDate.now().toString() + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File file = fileChooser.showSaveDialog(btnExportToExcel.getScene().getWindow());
        if (file != null) {
            exportToCSV(file);
        }
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
    
    // Navigation Methods
    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        NavigationManager.navigateTo(event, "/View/FXML/homepage.fxml", "Tomas Car Accessories - Dashboard");
    }
    
    @FXML
    private void handleUserManagement(javafx.event.ActionEvent event) {
        NavigationManager.navigateTo(event, "/View/FXML/usermanagement.fxml", "Tomas Car Accessories - User Management");
    }
    
    @FXML
    private void handleEmployeeManagement(javafx.event.ActionEvent event) {
        NavigationManager.navigateTo(event, "/View/FXML/employeemanagement.fxml", "Tomas Car Accessories - Employee Management");
    }
    
    @FXML
    private void handleCustomerManagement(javafx.event.ActionEvent event) {
        NavigationManager.navigateTo(event, "/View/FXML/customermanagement.fxml", "Tomas Car Accessories - Customer Management");
    }
    
    @FXML
    private void handleServiceBooking(javafx.event.ActionEvent event) {
        NavigationManager.navigateTo(event, "/View/FXML/servicebooking.fxml", "Tomas Car Accessories - Service Booking");
    }
    
    @FXML
    private void handleInventoryManagement(javafx.event.ActionEvent event) {
        NavigationManager.navigateTo(event, "/View/FXML/inventorymanagement.fxml", "Tomas Car Accessories - Inventory Management");
    }
    
    @FXML
    private void handleSalesManagement(javafx.event.ActionEvent event) {
        NavigationManager.navigateTo(event, "/View/FXML/salesmanagement.fxml", "Tomas Car Accessories - Sales Management");
    }
    
    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Confirm Logout");
        alert.setContentText("Are you sure you want to logout?");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            NavigationManager.navigateTo(event, "/View/FXML/loginpage.fxml", "Tomas Car Accessories - Login");
        }
    }
    
    private List<OrderItem> loadOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String query = "SELECT * FROM order_items WHERE order_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                OrderItem item = new OrderItem(
                    rs.getInt("order_item_id"),
                    rs.getInt("order_id"),
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("unit_price"),
                    rs.getDouble("subtotal"),
                    0.0, // discount
                    rs.getDouble("total")
                );
                items.add(item);
            }
            rs.close();
            
        } catch (SQLException e) {
            showError("Error loading order items: " + e.getMessage());
            e.printStackTrace();
        }
        
        return items;
    }
    
    private void generateReceipt(Order order, List<OrderItem> items) {
        StringBuilder receipt = new StringBuilder();
        int width = 45;
        
        receipt.append("=============================================\n");
        receipt.append(centerText("TOMAS CAR ACCESSORIES", width)).append("\n");
        receipt.append(centerText("Brgy. San Pablo, Ormoc City, Leyte", width)).append("\n");
        receipt.append(centerText("(Palo-Carigara-Ormoc City Rd)", width)).append("\n");
        receipt.append(centerText("Ormoc City, Philippines, 6541", width)).append("\n");
        receipt.append(centerText("0964 745 1841", width)).append("\n");
        receipt.append(centerText("karwashnitomas2016@yahoo.com", width)).append("\n");
        receipt.append("=============================================\n\n");
        
        receipt.append(String.format("%-15s: %s\n", "Order Number", order.getOrderNumber()));
        receipt.append(String.format("%-15s: %s\n", "Date", order.getOrderDate()));
        receipt.append(String.format("%-15s: %s\n", "Customer", (order.getCustomerName() != null ? order.getCustomerName() : "Walk-in")));
        receipt.append(String.format("%-15s: %s\n", "Cashier", order.getUserName()));
        receipt.append("---------------------------------------------\n");
        receipt.append(String.format("%-22s %5s %16s\n", "DESCRIPTION", "QTY", "AMOUNT"));
        receipt.append("---------------------------------------------\n");
        
        for (OrderItem item : items) {
            String name = item.getProductName();
            if (name.length() > 20) name = name.substring(0, 19) + ".";
            receipt.append(String.format("%-22s %5d   %14s\n", name, item.getQuantity(), "P" + df.format(item.getTotal())));
        }
        
        receipt.append("---------------------------------------------\n");
        receipt.append(String.format("%-28s %16s\n", "SUBTOTAL:", "P" + df.format(order.getTotalAmount())));
        if (order.getDiscountAmount() > 0) {
            receipt.append(String.format("%-28s %16s\n", "DISCOUNT:", "-P" + df.format(order.getDiscountAmount())));
        }
        receipt.append(String.format("%-28s %16s\n", "VAT (12%):", "P" + df.format(order.getTaxAmount())));
        receipt.append("---------------------------------------------\n");
        receipt.append(String.format("%-28s %16s\n", "GRAND TOTAL:", "P" + df.format(order.getFinalAmount())));
        receipt.append("---------------------------------------------\n\n");
        
        receipt.append(String.format("%-18s: %s\n", "Payment Method", order.getPaymentMethod()));
        receipt.append(String.format("%-18s: %s\n", "Status", order.getPaymentStatus()));
        
        receipt.append("\n=============================================\n");
        receipt.append(centerText("Thank you for your business!", width)).append("\n");
        receipt.append(centerText("Drive Safely!", width)).append("\n");
        receipt.append("=============================================\n");
        
        System.out.println(receipt.toString());
        showReceiptDialog(receipt.toString());
    }
    
    private String centerText(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int padding = (width - text.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < padding; i++) {
            sb.append(" ");
        }
        sb.append(text);
        return sb.toString();
    }
    
    private void showReceiptDialog(String receipt) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Receipt");
        alert.setHeaderText("Receipt Preview");
        
        TextArea textArea = new TextArea(receipt);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 13px;");
        
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(450, 600);
        alert.showAndWait();
    }

    private void exportToCSV(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.append("Order Number,Order Date,Customer Name,Total Amount,Discount Amount,Tax Amount,Final Amount,Payment Method,Payment Status,User Name\n");
            
            // Write data rows
            for (Order order : transactionList) {
                writer.append(order.getOrderNumber()).append(",");
                writer.append(order.getOrderDate()).append(",");
                writer.append(order.getCustomerName() != null ? order.getCustomerName() : "").append(",");
                writer.append(String.valueOf(order.getTotalAmount())).append(",");
                writer.append(String.valueOf(order.getDiscountAmount())).append(",");
                writer.append(String.valueOf(order.getTaxAmount())).append(",");
                writer.append(String.valueOf(order.getFinalAmount())).append(",");
                writer.append(order.getPaymentMethod()).append(",");
                writer.append(order.getPaymentStatus()).append(",");
                writer.append(order.getUserName()).append("\n");
            }
            
            showInfo("Exported to " + file.getAbsolutePath());
            
        } catch (IOException e) {
            showError("Error exporting to CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateSummaryStats() {
        lblTransactionCount.setText("Total Transactions: " + transactionList.size());
        
        double totalSales = 0.0;
        double totalDiscount = 0.0;
        double totalTax = 0.0;
        double netTotal = 0.0;
        
        for (Order order : transactionList) {
            totalSales += order.getTotalAmount();
            totalDiscount += order.getDiscountAmount();
            totalTax += order.getTaxAmount();
            netTotal += order.getFinalAmount();
        }
        
        String role = Model.UserSession.getInstance().getUserRole();
        if ("Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role)) {
            lblTotalSales.setText("₱" + df.format(totalSales));
            lblTotalDiscount.setText("₱" + df.format(totalDiscount));
            lblTotalTax.setText("₱" + df.format(totalTax));
            lblNetTotal.setText("₱" + df.format(netTotal));
        } else {
            lblTotalSales.setText("Hidden");
            lblTotalDiscount.setText("Hidden");
            lblTotalTax.setText("Hidden");
            lblNetTotal.setText("Hidden");
        }
    }
    
    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
    }
    
    private void showWarning(String message) {
        showAlert(Alert.AlertType.WARNING, "Warning", message);
    }
    
    private void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Info", message);
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        alert.showAndWait();
    }
}
