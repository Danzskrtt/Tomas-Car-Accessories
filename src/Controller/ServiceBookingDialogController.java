package Controller;

import Model.Customer;
import Model.CustomerCar;
import Model.ServiceBooking;
import Model.BookingProduct;
import Model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.TableCell;

public class ServiceBookingDialogController {

    @FXML private TextField txtCustomerName;
    @FXML private ListView<String> listCustomerSuggestions;
    
    @FXML private ComboBox<String> cmbCarDetails;
    @FXML private ListView<String> listCarSuggestions;

    @FXML private ComboBox<String> cmbServiceTypeSearch;
    @FXML private TableView<ServiceItem> tblServiceTypes;
    @FXML private TableColumn<ServiceItem, String> colServiceName;
    @FXML private TableColumn<ServiceItem, Void> colServiceAction;

    @FXML private ComboBox<String> cmbEmployeeSearch;
    @FXML private ListView<String> listEmployeeSuggestions;
    @FXML private TableView<EmployeeItem> tblAssignedEmployees;
    @FXML private TableColumn<EmployeeItem, String> colEmployeeName;
    @FXML private TableColumn<EmployeeItem, String> colEmployeeRole;
    @FXML private TableColumn<EmployeeItem, Void> colEmployeeAction;

    @FXML private DatePicker datePicker;
    @FXML private TextArea txtNotes;
    @FXML private ComboBox<String> cmbStatus;
    
    @FXML private TextField txtTotalAmount;
    @FXML private TextField txtDownpayment;
    @FXML private ComboBox<String> cmbPaymentMethod;
    @FXML private TextField txtBalance;
    
    @FXML private TableView<BookingProduct> tblProducts;
    @FXML private TableColumn<BookingProduct, String> colProductName;
    @FXML private TableColumn<BookingProduct, Integer> colQuantity;
    @FXML private TableColumn<BookingProduct, Double> colUnitPrice;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private ServiceBooking booking;
    private ServiceBookingController parentController;
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    
    private int selectedCustomerId = -1;
    private int selectedCarId = -1;
    
    private ObservableList<BookingProduct> bookingProducts = FXCollections.observableArrayList();
    private ObservableList<ServiceItem> serviceItems = FXCollections.observableArrayList();
    private ObservableList<EmployeeItem> employeeItems = FXCollections.observableArrayList();
    
    // Internal class for customer search suggestions
    private class CustomerSuggestion {
        int customerId;
        String customerName;
        
        CustomerSuggestion(int customerId, String customerName) {
            this.customerId = customerId;
            this.customerName = customerName;
        }
        
        @Override
        public String toString() {
            return customerName;
        }
    }

    private class CarSuggestion {
        int carId;
        String carDetails;

        CarSuggestion(int carId, String carDetails) {
            this.carId = carId;
            this.carDetails = carDetails;
        }

        @Override
        public String toString() {
            return carDetails;
        }
    }
    
    private class EmployeeSuggestion {
        int employeeId;
        String employeeName;
        String position;
        
        EmployeeSuggestion(int employeeId, String employeeName, String position) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.position = position;
        }
        
        @Override
        public String toString() {
            return employeeName + " (" + position + ")";
        }
    }
    
    private List<CustomerSuggestion> currentCustomerSuggestions = new ArrayList<>();
    private List<CarSuggestion> currentCarSuggestions = new ArrayList<>();
    private List<EmployeeSuggestion> currentEmployeeSuggestions = new ArrayList<>();
    
    private boolean isInternalCustomerUpdate = false;

    @FXML
    private void initialize() {
        datePicker.setValue(LocalDate.now());
        setupCustomerAutocomplete();
        setupCarAutocomplete();
        setupEmployeeAutocomplete();
        setupServiceTable();
        setupEmployeeTable();
        setupProductTable();
        setupPaymentListeners();
        loadStatuses();
        loadPaymentMethods();
        setupServiceAutocomplete();
    }
    
    private void loadPaymentMethods() {
        if (cmbPaymentMethod != null) {
            ObservableList<String> methods = FXCollections.observableArrayList(
                "Cash", "Credit Card", "Debit Card", "GCash", "PayMaya", "Bank Transfer"
            );
            cmbPaymentMethod.setItems(methods);
            cmbPaymentMethod.setValue("Cash");
        }
    }

    private void setupPaymentListeners() {
        if (txtDownpayment != null) {
            txtDownpayment.textProperty().addListener((obs, oldVal, newVal) -> {
                calculateBalance();
            });
        }
    }
    
    private void calculateBalance() {
        try {
            double total = Double.parseDouble(txtTotalAmount.getText().replace(",", "").replace("₱", "").trim());
            double downpayment = 0.0;
            if (!txtDownpayment.getText().trim().isEmpty()) {
                downpayment = Double.parseDouble(txtDownpayment.getText().replace(",", "").replace("₱", "").trim());
            }
            double balance = total - downpayment;
            
            txtBalance.setText(String.format("%.2f", balance));
        } catch (NumberFormatException e) {
            txtBalance.setText("0.00");
        }
    }
    
    private void setupCustomerAutocomplete() {
        txtCustomerName.textProperty().addListener((obs, oldText, newText) -> {
            if (isInternalCustomerUpdate) return;
            
            if (newText == null || newText.trim().isEmpty()) {
                listCustomerSuggestions.setVisible(false);
                listCustomerSuggestions.setManaged(false);
                selectedCustomerId = -1;
                clearCarSelection();
                return;
            }
            searchCustomers(newText.trim());
        });
        
        listCustomerSuggestions.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                int selectedIndex = listCustomerSuggestions.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0 && selectedIndex < currentCustomerSuggestions.size()) {
                    CustomerSuggestion suggestion = currentCustomerSuggestions.get(selectedIndex);
                    selectCustomer(suggestion);
                }
            }
        });
        
        txtCustomerName.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !listCustomerSuggestions.isFocused()) {
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> {
                            if (!listCustomerSuggestions.isFocused()) {
                                listCustomerSuggestions.setVisible(false);
                                listCustomerSuggestions.setManaged(false);
                            }
                        });
                    }
                }, 150);
            }
        });
    }

    private void setupCarAutocomplete() {
        cmbCarDetails.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (selectedCustomerId == -1) return;
            if (newText == null || newText.trim().isEmpty()) {
                listCarSuggestions.setVisible(false);
                listCarSuggestions.setManaged(false);
                selectedCarId = -1;
                return;
            }
            searchCars(newText.trim());
        });
        
        listCarSuggestions.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                int selectedIndex = listCarSuggestions.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0 && selectedIndex < currentCarSuggestions.size()) {
                    CarSuggestion suggestion = currentCarSuggestions.get(selectedIndex);
                    selectCar(suggestion);
                }
            }
        });
    }
    
    private void setupEmployeeAutocomplete() {
        cmbEmployeeSearch.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.trim().isEmpty()) {
                listEmployeeSuggestions.getSelectionModel().clearSelection();
                listEmployeeSuggestions.setVisible(false);
                listEmployeeSuggestions.setManaged(false);
                cmbEmployeeSearch.setUserData(null);
                return;
            }
            searchEmployees(newText.trim());
        });
        
        listEmployeeSuggestions.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                int selectedIndex = listEmployeeSuggestions.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0 && selectedIndex < currentEmployeeSuggestions.size()) {
                    EmployeeSuggestion suggestion = currentEmployeeSuggestions.get(selectedIndex);
                    cmbEmployeeSearch.getEditor().setText(suggestion.employeeName);
                    // store temporarily to be added
                    cmbEmployeeSearch.setUserData(suggestion);
                    listEmployeeSuggestions.setVisible(false);
                    listEmployeeSuggestions.setManaged(false);
                }
            }
        });
    }
    
    private void searchCustomers(String searchText) {
        currentCustomerSuggestions.clear();
        ObservableList<String> displayList = FXCollections.observableArrayList();
        
        String query = "SELECT customer_id, customer_name FROM customers " +
                      "WHERE customer_name LIKE ? COLLATE NOCASE LIMIT 10";
                      
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setString(1, "%" + searchText + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CustomerSuggestion suggestion = new CustomerSuggestion(
                        rs.getInt("customer_id"), 
                        rs.getString("customer_name")
                    );
                    currentCustomerSuggestions.add(suggestion);
                    displayList.add(suggestion.toString());
                }
            }
            
            if (!displayList.isEmpty()) {
                listCustomerSuggestions.setItems(displayList);
                listCustomerSuggestions.setVisible(true);
                listCustomerSuggestions.setManaged(true);
            } else {
                listCustomerSuggestions.setVisible(false);
                listCustomerSuggestions.setManaged(false);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchCars(String searchText) {
        currentCarSuggestions.clear();
        ObservableList<String> displayList = FXCollections.observableArrayList();
        
        String query = "SELECT car_id, car_brand, model, plate_number FROM customer_cars " +
                      "WHERE customer_id = ? AND (car_brand LIKE ? OR model LIKE ?) COLLATE NOCASE LIMIT 10";
                      
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setInt(1, selectedCustomerId);
            pstmt.setString(2, "%" + searchText + "%");
            pstmt.setString(3, "%" + searchText + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String details = rs.getString("car_brand") + " " + rs.getString("model") + " (" + rs.getString("plate_number") + ")";
                    CarSuggestion suggestion = new CarSuggestion(rs.getInt("car_id"), details);
                    currentCarSuggestions.add(suggestion);
                    displayList.add(suggestion.toString());
                }
            }
            
            if (!displayList.isEmpty()) {
                listCarSuggestions.setItems(displayList);
                listCarSuggestions.setVisible(true);
                listCarSuggestions.setManaged(true);
            } else {
                listCarSuggestions.setVisible(false);
                listCarSuggestions.setManaged(false);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchEmployees(String searchText) {
        currentEmployeeSuggestions.clear();
        ObservableList<String> displayList = FXCollections.observableArrayList();
        
        String query = "SELECT employee_id, employee_name, position FROM employee " +
                      "WHERE employee_name LIKE ? OR position LIKE ? COLLATE NOCASE LIMIT 10";
                      
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setString(1, "%" + searchText + "%");
            pstmt.setString(2, "%" + searchText + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    EmployeeSuggestion suggestion = new EmployeeSuggestion(
                        rs.getInt("employee_id"), 
                        rs.getString("employee_name"),
                        rs.getString("position")
                    );
                    currentEmployeeSuggestions.add(suggestion);
                    displayList.add(suggestion.toString());
                }
            }
            
            if (!displayList.isEmpty()) {
                listEmployeeSuggestions.setItems(displayList);
                listEmployeeSuggestions.setVisible(true);
                listEmployeeSuggestions.setManaged(true);
                // Position adjustment...
            } else {
                listEmployeeSuggestions.setVisible(false);
                listEmployeeSuggestions.setManaged(false);
            }
            
        } catch (SQLException e) {
            // ignore
        }
    }
    
    private void selectCustomer(CustomerSuggestion suggestion) {
        this.selectedCustomerId = suggestion.customerId;
        
        // Remove listener temporarily to avoid trigger
        isInternalCustomerUpdate = true;
        this.txtCustomerName.setText(suggestion.customerName);
        isInternalCustomerUpdate = false;
        
        listCustomerSuggestions.getSelectionModel().clearSelection();
        listCustomerSuggestions.setVisible(false);
        listCustomerSuggestions.setManaged(false);

        clearCarSelection();
        loadPOSProducts(this.selectedCustomerId);
    }

    private void selectCar(CarSuggestion suggestion) {
        this.selectedCarId = suggestion.carId;
        this.cmbCarDetails.getEditor().setText(suggestion.carDetails);
        
        listCarSuggestions.setVisible(false);
        listCarSuggestions.setManaged(false);
    }
    
    private void clearCarSelection() {
        selectedCarId = -1;
        cmbCarDetails.getEditor().clear();
    }
    
    public static class ServiceItem {
        private String serviceName;
        private String category;
        private double estimatedCost;
        
        public ServiceItem(String serviceName, String category, double estimatedCost) {
            this.serviceName = serviceName;
            this.category = category;
            this.estimatedCost = estimatedCost;
        }

        public String getServiceName() { return serviceName; }
        public String getCategory() { return category; }
        public double getEstimatedCost() { return estimatedCost; }
    }

    public static class EmployeeItem {
        private int employeeId;
        private String employeeName;
        private String role;
        
        public EmployeeItem(int employeeId, String employeeName, String role) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.role = role;
        }

        public int getEmployeeId() { return employeeId; }
        public String getEmployeeName() { return employeeName; }
        public String getRole() { return role; }
    }

    private void setupServiceAutocomplete() {
        cmbServiceTypeSearch.getItems().addAll("Oil Change", "Tire Rotation", "Brake Inspection", "Engine Diagnostic", "Full Service");
    }

    @FXML
    public void handleAddService(ActionEvent event) {
        String serviceName = cmbServiceTypeSearch.getEditor().getText().trim();
        if (!serviceName.isEmpty()) {
            boolean exists = serviceItems.stream().anyMatch(s -> s.getServiceName().equalsIgnoreCase(serviceName));
            if (!exists) {
                serviceItems.add(new ServiceItem(serviceName, "General", 0.0));
            }
            cmbServiceTypeSearch.setValue(null);
            cmbServiceTypeSearch.getEditor().clear();
            tblServiceTypes.refresh();
        }
    }

    @FXML
    public void handleAddEmployee(ActionEvent event) {
        EmployeeSuggestion suggestion = (EmployeeSuggestion) cmbEmployeeSearch.getUserData();
        String typed = cmbEmployeeSearch.getEditor().getText().trim();
        
        if (suggestion != null && suggestion.employeeName.equalsIgnoreCase(typed)) {
            boolean exists = employeeItems.stream().anyMatch(e -> e.getEmployeeId() == suggestion.employeeId);
            if (!exists) {
                employeeItems.add(new EmployeeItem(suggestion.employeeId, suggestion.employeeName, suggestion.position));
            }
            cmbEmployeeSearch.setValue(null);
            cmbEmployeeSearch.getEditor().clear();
            cmbEmployeeSearch.setUserData(null);
            listEmployeeSuggestions.getSelectionModel().clearSelection();
            tblAssignedEmployees.setItems(employeeItems);
            tblAssignedEmployees.refresh();
        } else if (!typed.isEmpty()) {
            boolean exists = employeeItems.stream().anyMatch(e -> e.getEmployeeName().equalsIgnoreCase(typed));
            if (!exists) {
                employeeItems.add(new EmployeeItem(0, typed, "Technician")); // Manual fallback
            }
            cmbEmployeeSearch.setValue(null);
            cmbEmployeeSearch.getEditor().clear();
            cmbEmployeeSearch.setUserData(null);
            listEmployeeSuggestions.getSelectionModel().clearSelection();
            tblAssignedEmployees.setItems(employeeItems);
            tblAssignedEmployees.refresh();
        }
    }

    private void setupServiceTable() {
        colServiceName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getServiceName()));

        colServiceAction.setCellFactory(param -> new TableCell<ServiceItem, Void>() {
            private final Button btn = new Button("✕");
            {
                btn.setStyle("-fx-text-fill: #EF4444; -fx-background-color: #FEE2E2; -fx-font-weight: bold; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
                btn.setOnAction(e -> {
                    ServiceItem item = getTableView().getItems().get(getIndex());
                    serviceItems.remove(item);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); } else { setGraphic(btn); }
            }
        });

        tblServiceTypes.setItems(serviceItems);
    }

    private void setupEmployeeTable() {
        colEmployeeName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmployeeName()));
        colEmployeeRole.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole()));
        
        colEmployeeAction.setCellFactory(param -> new TableCell<EmployeeItem, Void>() {
            private final Button btn = new Button("✕");
            {
                btn.setStyle("-fx-text-fill: #EF4444; -fx-background-color: #FEE2E2; -fx-font-weight: bold; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
                btn.setOnAction(e -> {
                    EmployeeItem item = getTableView().getItems().get(getIndex());
                    employeeItems.remove(item);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); } else { setGraphic(btn); }
            }
        });

        tblAssignedEmployees.setItems(employeeItems);
    }
    
    private void loadPOSProducts(int customerId) {
        // Here we link to POS transactions to load products
        bookingProducts.clear();
        String query = "SELECT p.product_name, oi.quantity, oi.price AS unit_price, (oi.quantity * oi.price) AS total " +
                       "FROM order_items oi " +
                       "JOIN orders o ON oi.order_id = o.order_id " +
                       "JOIN products p ON oi.product_id = p.product_id " +
                       "WHERE o.customer_id = ? ORDER BY o.order_date DESC LIMIT 20";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BookingProduct bp = new BookingProduct(
                        0, 0, 0,
                        rs.getString("product_name"), "", rs.getInt("quantity"),
                        rs.getDouble("unit_price"), rs.getDouble("total"), ""
                    );
                    bookingProducts.add(bp);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadSavedBookingProducts(int bookingId) {
        bookingProducts.clear();
        String query = "SELECT product_name, quantity, unit_price, total_price FROM booking_products WHERE booking_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BookingProduct bp = new BookingProduct(
                        0, bookingId, 0,
                        rs.getString("product_name"), "", rs.getInt("quantity"),
                        rs.getDouble("unit_price"), rs.getDouble("total_price"), ""
                    );
                    bookingProducts.add(bp);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void setupProductTable() {
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tblProducts.setItems(bookingProducts);
    }
    
    private void loadStatuses() {
        ObservableList<String> statuses = FXCollections.observableArrayList(
            "Pending", "Confirmed", "In Progress", "Completed", "Cancelled"
        );
        cmbStatus.setItems(statuses);
        cmbStatus.setValue("Confirmed"); // Default value
    }

    public void setDialogDetails(ServiceBookingController parentController, ServiceBooking booking) {
        this.parentController = parentController;
        this.booking = booking;

        if (booking != null) {
            // Edit mode
            selectedCustomerId = booking.getCustomerId();
            selectedCarId = booking.getCarId();

            isInternalCustomerUpdate = true;
            txtCustomerName.setText(booking.getCustomerName());
            isInternalCustomerUpdate = false;

            cmbCarDetails.getEditor().setText(booking.getCarDescription());

            serviceItems.clear();
            if (booking.getServiceType() != null && !booking.getServiceType().isEmpty()) {
                String[] splits = booking.getServiceType().split(", ");
                for (String split : splits) {
                    serviceItems.add(new ServiceItem(split, "General", 0.0));
                }
            }

            employeeItems.clear();
            if (booking.getAssignedTechnician() != null && !booking.getAssignedTechnician().isEmpty()) {
                String[] splits = booking.getAssignedTechnician().split(", ");
                for (String split : splits) {
                    employeeItems.add(new EmployeeItem(0, split, "Technician"));
                }
            }

            if (booking.getBookingDate() != null && !booking.getBookingDate().isEmpty()) {
                datePicker.setValue(LocalDate.parse(booking.getBookingDate()));
            }
            cmbStatus.setValue(booking.getStatus());
            txtNotes.setText(booking.getNotes());
            
            if (txtTotalAmount != null) txtTotalAmount.setText(String.format("%.2f", booking.getEstimatedCost()));
            if (txtDownpayment != null) txtDownpayment.setText(String.format("%.2f", booking.getDownpayment()));
            if (txtBalance != null) txtBalance.setText(String.format("%.2f", booking.getBalance()));
            
            // Reload existing POS-related math metrics so edits don't overwrite discounts to 0
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement("SELECT subtotal_amount, discount_amount, tax_amount FROM service_bookings WHERE booking_id = ?")) {
                pstmt.setInt(1, booking.getBookingId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if(rs.next()) {
                        posSubtotal = rs.getDouble("subtotal_amount");
                        posDiscount = rs.getDouble("discount_amount");
                        posTax = rs.getDouble("tax_amount");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            loadSavedBookingProducts(booking.getBookingId());
        }
    }
    
    private double posSubtotal = 0.0;
    private double posDiscount = 0.0;
    private double posTax = 0.0;
    
    public void setFromPOSData(int posCustomerId, List<SalesManagementController.CartItem> cartItems, double totalAmount, double subtotal, double discount, double tax) {
        // Clear previous bookings
        booking = null;
        this.posSubtotal = subtotal;
        this.posDiscount = discount;
        this.posTax = tax;
        
        // Try getting customer name from ID
        if (posCustomerId > 0) {
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement("SELECT customer_name FROM customers WHERE customer_id = ?")) {
                pstmt.setInt(1, posCustomerId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        selectedCustomerId = posCustomerId;
                        isInternalCustomerUpdate = true;
                        txtCustomerName.setText(rs.getString("customer_name"));
                        isInternalCustomerUpdate = false;
                        
                        listCustomerSuggestions.setVisible(false);
                        listCustomerSuggestions.setManaged(false);
                        
                        loadPOSProducts(selectedCustomerId); // If they already have stuff. But we also append Cart!
                    }
                }
            } catch(Exception e) { e.printStackTrace(); }
        }

        // Fill POS Cart Items explicitly
        bookingProducts.clear();
        for (SalesManagementController.CartItem item : cartItems) {
            BookingProduct bp = new BookingProduct(
                0, 0, 0,
                item.getProductName(), "", item.getQuantity(),
                item.getUnitPrice(), item.getTotal(), ""
            );
            bookingProducts.add(bp);
        }
        
        // Auto fill financial details
        if (txtTotalAmount != null) {
            txtTotalAmount.setText(String.format("%.2f", totalAmount));
        }
        calculateBalance();
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        // Fix: Make sure to hide autocomplete panels
        listCustomerSuggestions.setVisible(false);
        listCustomerSuggestions.setManaged(false);
        listCarSuggestions.setVisible(false);
        listCarSuggestions.setManaged(false);
        listEmployeeSuggestions.setVisible(false);
        listEmployeeSuggestions.setManaged(false);

        if (!validateInput()) {
            return;
        }
        
        List<String> servList = new ArrayList<>();
        for (ServiceItem s : serviceItems) servList.add(s.getServiceName());
        String serviceType = String.join(", ", servList);

        List<String> empList = new ArrayList<>();
        for (EmployeeItem e : employeeItems) empList.add(e.getEmployeeName());
        String assignedTechnician = String.join(", ", empList);
        
        LocalDate bookingDate = datePicker.getValue();
        String status = cmbStatus.getValue();
        String notes = txtNotes.getText();
        
        double estimatedCost = 0.0;
        double downpayment = 0.0;
        double balance = 0.0;
        String paymentMethod = cmbPaymentMethod != null && cmbPaymentMethod.getValue() != null ? cmbPaymentMethod.getValue() : "Cash";
        
        try {
            if (txtTotalAmount != null && !txtTotalAmount.getText().isEmpty()) 
                estimatedCost = Double.parseDouble(txtTotalAmount.getText().replace(",", "").replace("₱", "").trim());
            if (txtDownpayment != null && !txtDownpayment.getText().isEmpty()) 
                downpayment = Double.parseDouble(txtDownpayment.getText().replace(",", "").replace("₱", "").trim());
            if (txtBalance != null && !txtBalance.getText().isEmpty()) 
                balance = Double.parseDouble(txtBalance.getText().replace(",", "").replace("₱", "").trim());
        } catch (NumberFormatException ignored) {}
        
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (booking == null) {
                // Insert new mapping
                String bookingNum = "SB-" + System.currentTimeMillis();
                String query = "INSERT INTO service_bookings (booking_number, customer_id, car_id, service_type, assigned_technician, booking_date, booking_time, status, notes, estimated_cost, downpayment, balance, subtotal_amount, discount_amount, tax_amount, user_id, payment_method) " +
                              "VALUES (?, ?, ?, ?, ?, ?, '09:00:00', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, bookingNum);
                pstmt.setInt(2, selectedCustomerId);
                pstmt.setInt(3, selectedCarId);
                pstmt.setString(4, serviceType);
                pstmt.setString(5, assignedTechnician);
                pstmt.setString(6, bookingDate.toString());
                pstmt.setString(7, status);
                pstmt.setString(8, notes != null ? notes : "");
                pstmt.setDouble(9, estimatedCost);
                pstmt.setDouble(10, downpayment);
                pstmt.setDouble(11, balance);
                pstmt.setDouble(12, posSubtotal > 0 ? posSubtotal : estimatedCost / 1.12);
                pstmt.setDouble(13, posDiscount);
                pstmt.setDouble(14, posTax > 0 ? posTax : estimatedCost - (estimatedCost / 1.12));
                pstmt.setInt(15, Model.UserSession.getInstance().getUserId() > 0 ? Model.UserSession.getInstance().getUserId() : 1);
                pstmt.setString(16, paymentMethod);

                pstmt.executeUpdate();
                
                int finalBookingId = 0;
                try (Statement stmtId = conn.createStatement();
                     ResultSet keys = stmtId.executeQuery("SELECT last_insert_rowid()")) {
                    if (keys.next()) finalBookingId = keys.getInt(1);
                }
                saveBookingRelations(conn, finalBookingId);

            } else {
                // Update
                String query = "UPDATE service_bookings SET customer_id=?, car_id=?, service_type=?, assigned_technician=?, booking_date=?, status=?, notes=?, estimated_cost=?, downpayment=?, balance=?, subtotal_amount=?, discount_amount=?, tax_amount=?, user_id=?, payment_method=? " +
                              "WHERE booking_id=?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, selectedCustomerId);
                pstmt.setInt(2, selectedCarId);
                pstmt.setString(3, serviceType);
                pstmt.setString(4, assignedTechnician);
                pstmt.setString(5, bookingDate.toString());
                pstmt.setString(6, status);
                pstmt.setString(7, notes != null ? notes : "");
                pstmt.setDouble(8, estimatedCost);
                pstmt.setDouble(9, downpayment);
                pstmt.setDouble(10, balance);
                pstmt.setDouble(11, posSubtotal > 0 ? posSubtotal : estimatedCost / 1.12);
                pstmt.setDouble(12, posDiscount);
                pstmt.setDouble(13, posTax > 0 ? posTax : estimatedCost - (estimatedCost / 1.12));
                pstmt.setInt(14, Model.UserSession.getInstance().getUserId() > 0 ? Model.UserSession.getInstance().getUserId() : 1);
                pstmt.setString(15, paymentMethod);
                pstmt.setInt(16, booking.getBookingId());

                pstmt.executeUpdate();
                
                saveBookingRelations(conn, booking.getBookingId());
            }

            if (parentController != null) {
                parentController.loadBookings();
            }
            closeDialog();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to save booking: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void saveBookingRelations(Connection conn, int finalBookingId) throws SQLException {
        if (finalBookingId <= 0) return;
        
        // Save to booking_products
        if (bookingProducts != null) {
            try (PreparedStatement delStmt = conn.prepareStatement("DELETE FROM booking_products WHERE booking_id = ?")) {
                delStmt.setInt(1, finalBookingId);
                delStmt.executeUpdate();
            }
            try (PreparedStatement insStmt = conn.prepareStatement("INSERT INTO booking_products (booking_id, product_id, product_name, quantity, unit_price, total_price) VALUES (?, NULL, ?, ?, ?, ?)")) {
                for (BookingProduct bp : bookingProducts) {
                    insStmt.setInt(1, finalBookingId);
                    insStmt.setString(2, bp.getProductName());
                    insStmt.setInt(3, bp.getQuantity());
                    insStmt.setDouble(4, bp.getUnitPrice());
                    insStmt.setDouble(5, bp.getTotalPrice());
                    insStmt.executeUpdate();
                }
            }
        }
        
        // Save to booking_technicians
        if (employeeItems != null) {
            try (PreparedStatement delStmt = conn.prepareStatement("DELETE FROM booking_technicians WHERE booking_id = ?")) {
                delStmt.setInt(1, finalBookingId);
                delStmt.executeUpdate();
            }
            try (PreparedStatement insStmt = conn.prepareStatement("INSERT INTO booking_technicians (booking_id, technician_name, role) VALUES (?, ?, ?)")) {
                for (EmployeeItem emp : employeeItems) {
                    insStmt.setInt(1, finalBookingId);
                    insStmt.setString(2, emp.getEmployeeName());
                    insStmt.setString(3, emp.getRole());
                    insStmt.executeUpdate();
                }
            }
        }
    }

    private boolean validateInput() {
        if (selectedCustomerId == -1) {
            showAlert("Validation Error", "Please select a valid customer.", Alert.AlertType.WARNING);
            return false;
        }
        if (selectedCarId == -1) {
            showAlert("Validation Error", "Please select a valid vehicle.", Alert.AlertType.WARNING);
            return false;
        }
        if (serviceItems.isEmpty()) {
            showAlert("Validation Error", "Please add at least one service type.", Alert.AlertType.WARNING);
            return false;
        }
        if (datePicker.getValue() == null) {
            showAlert("Validation Error", "Please select a booking date.", Alert.AlertType.WARNING);
            return false;
        }
        if (cmbStatus.getValue() == null) {
            showAlert("Validation Error", "Please select a status.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }
    
    private void closeDialog() {
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
