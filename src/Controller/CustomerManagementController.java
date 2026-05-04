package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import Model.Customer;
import java.sql.*;

public class CustomerManagementController {
    
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
    @FXML private Button btnAddCustomer;
    @FXML private Button btnRefreshCustomers;
    
    @FXML private TableView<Customer> tableCustomers;
    @FXML private TableColumn<Customer, Integer> colCustomerId;
    @FXML private TableColumn<Customer, String> colCustomerName;
    @FXML private TableColumn<Customer, String> colCustomerEmail;
    @FXML private TableColumn<Customer, String> colCustomerPhone;
    @FXML private TableColumn<Customer, String> colCustomerAddress;
    @FXML private TableColumn<Customer, Void> colActions;
    
    @FXML private Label lblCustomerCount;
    
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    
    @FXML
    public void initialize() {
        NavigationManager.applyRoleBasedAccess(this);
        setupTableColumns();
        loadCustomers();
    }
    
    private void setupTableColumns() {
        colCustomerId.setCellValueFactory(cellData -> cellData.getValue().customerIdProperty().asObject());
        colCustomerName.setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        colCustomerEmail.setCellValueFactory(cellData -> cellData.getValue().customerEmailProperty());
        colCustomerPhone.setCellValueFactory(cellData -> cellData.getValue().customerPhoneProperty());
        colCustomerAddress.setCellValueFactory(cellData -> cellData.getValue().customerAddressProperty());
        
        // Add action buttons to each row
        colActions.setCellFactory(param -> new javafx.scene.control.TableCell<Customer, Void>() {
            private final Button btnView = new Button();
            private final Button btnDelete = new Button();
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(8);
            
            {
                // Load icons for buttons
                try {
                    javafx.scene.image.ImageView viewIcon = new javafx.scene.image.ImageView(
                        new javafx.scene.image.Image(getClass().getResourceAsStream("/View/pics/eye-icon-1457.png"))
                    );
                    viewIcon.setFitWidth(22);
                    viewIcon.setFitHeight(22);
                    viewIcon.setPreserveRatio(true);
                    btnView.setGraphic(viewIcon);
                    
                    javafx.scene.image.ImageView deleteIcon = new javafx.scene.image.ImageView(
                        new javafx.scene.image.Image(getClass().getResourceAsStream("/View/pics/trash-can-icon-28689.png"))
                    );
                    deleteIcon.setFitWidth(22);
                    deleteIcon.setFitHeight(22);
                    deleteIcon.setPreserveRatio(true);
                    btnDelete.setGraphic(deleteIcon);
                } catch (Exception e) {
                    // Fallback to emoji if images not found
                    btnView.setText("👁️");
                    btnDelete.setText("🗑️");
                }
                
                // Style View button
                btnView.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; " +
                               "-fx-padding: 8 12; -fx-background-radius: 6; " +
                               "-fx-cursor: hand;");
                btnView.setTooltip(new javafx.scene.control.Tooltip("View Details"));
                
                // Style Delete button
                btnDelete.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                                 "-fx-padding: 8 12; -fx-background-radius: 6; " +
                                 "-fx-cursor: hand;");
                btnDelete.setTooltip(new javafx.scene.control.Tooltip("Delete Customer"));

                
                
                // View button action
                btnView.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    handleViewCustomerDetails(customer);
                });
                
                // Delete button action
                btnDelete.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    handleDeleteCustomer(customer);
                });
                
                pane.setAlignment(javafx.geometry.Pos.CENTER);
                pane.getChildren().addAll(btnView, btnDelete);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }
    
    

    private void loadCustomers() {
        customerList.clear();
        String query = "SELECT customer_id, customer_name, customer_address, customer_email, customer_phone, " +
                      "plate_number, vehicle_type, brand, model FROM customers ORDER BY customer_name";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Customer customer = new Customer(
                    rs.getInt("customer_id"),
                    rs.getString("customer_name"),
                    rs.getString("customer_address") != null ? rs.getString("customer_address") : "",
                    rs.getString("customer_email") != null ? rs.getString("customer_email") : "",
                    rs.getString("customer_phone") != null ? rs.getString("customer_phone") : "",
                    rs.getString("plate_number") != null ? rs.getString("plate_number") : "",
                    rs.getString("vehicle_type") != null ? rs.getString("vehicle_type") : "",
                    rs.getString("brand") != null ? rs.getString("brand") : "",
                    rs.getString("model") != null ? rs.getString("model") : "",
                    ""
                );
                customerList.add(customer);
            }
            
            tableCustomers.setItems(customerList);
            lblCustomerCount.setText("Total: " + customerList.size());
            
        } catch (SQLException e) {
            showError("Error loading customers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAddCustomer() {
        showCustomerDialog(null);
    }
    
    private void handleViewCustomerDetails(Customer customer) {
        if (customer == null) {
            showWarning("Invalid customer selection.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/customerdetailsdialog.fxml"));
            Parent root = loader.load();
            
            CustomerDetailsDialogController controller = loader.getController();
            controller.setCustomer(customer);
            controller.setOnDataChanged(() -> loadCustomers());
            
            Stage stage = new Stage();
            stage.setTitle("Customer Details - " + customer.getCustomerName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Refresh data after dialog closes
            loadCustomers();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open customer details: " + e.getMessage());
        }
    }
    
    private void handleDeleteCustomer(Customer customer) {
        if (customer == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Customer");
        alert.setHeaderText("Delete " + customer.getCustomerName() + "?");
        alert.setContentText("This will also delete all associated vehicles and bookings. This action cannot be undone.");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            String query = "DELETE FROM customers WHERE customer_id = ?";
            
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setInt(1, customer.getCustomerId());
                pstmt.executeUpdate();
                
                showInfo("Customer deleted successfully!");
                loadCustomers();
                
            } catch (SQLException e) {
                showError("Error deleting customer: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleRefreshCustomers() {
        loadCustomers();
        showInfo("Customer list refreshed!");
    }
    
    @FXML
    private void handleSearch() {
        String searchTerm = txtSearch.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadCustomers();
            return;
        }
        
        ObservableList<Customer> filteredList = FXCollections.observableArrayList();
        for (Customer customer : customerList) {
            if (customer.getCustomerName().toLowerCase().contains(searchTerm) ||
                (customer.getCustomerEmail() != null && customer.getCustomerEmail().toLowerCase().contains(searchTerm)) ||
                (customer.getCustomerPhone() != null && customer.getCustomerPhone().toLowerCase().contains(searchTerm))) {
                filteredList.add(customer);
            }
        }
        
        tableCustomers.setItems(filteredList);
        lblCustomerCount.setText("Total: " + filteredList.size());
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
    private void handleServiceBooking() {
        navigateTo("/View/FXML/servicebooking.fxml", "Service Booking");
    }
    
    @FXML
    private void handleInventoryManagement() {
        navigateTo("/View/FXML/inventorymanagement.fxml", "Inventory Management");
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
    
    private void showCustomerDialog(Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/customerdialog.fxml"));
            Parent root = loader.load();
            
            CustomerDialogController controller = loader.getController();
            controller.setCustomer(customer);
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.setTitle(customer == null ? "Add Customer" : "Edit Customer");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (Exception e) {
            showError("Error opening customer dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void refreshData() {
        loadCustomers();
        tableCustomers.refresh();
    }
    
    private void navigateTo(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage currentStage = (Stage) tableCustomers.getScene().getWindow();
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
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
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
