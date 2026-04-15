package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import Model.Employee;
import Model.SharedDataModel;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;

public class EmployeeManagementController {
    
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
    @FXML private Button btnAddEmployee;
    @FXML private Button btnRefreshEmployees;
    
    @FXML private TableView<Employee> tableEmployees;
    @FXML private TableColumn<Employee, Integer> colEmployeeId;
    @FXML private TableColumn<Employee, Void> colPhoto;
    @FXML private TableColumn<Employee, String> colEmployeeName;
    @FXML private TableColumn<Employee, String> colPosition;
    @FXML private TableColumn<Employee, String> colDepartment;
    @FXML private TableColumn<Employee, String> colEmail;
    @FXML private TableColumn<Employee, String> colPhone;
    @FXML private TableColumn<Employee, String> colHireDate;
    @FXML private TableColumn<Employee, Double> colSalary;
    @FXML private TableColumn<Employee, Void> colActions;
    
    @FXML private Label lblEmployeeCount;
    
    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    
    @FXML
    public void initialize() {
        NavigationManager.applyRoleBasedAccess(this);
        setupTableColumns();
        loadEmployees();
    }
    
    private void setupTableColumns() {
        colEmployeeId.setCellValueFactory(cellData -> cellData.getValue().employeeIdProperty().asObject());
        
        // Photo column with ImageView
        colPhoto.setCellFactory(param -> new TableCell<Employee, Void>() {
            private final ImageView imageView = new ImageView();
            
            {
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);
                imageView.setStyle("-fx-background-radius: 25; -fx-border-radius: 25;");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Employee employee = getTableView().getItems().get(getIndex());
                    if (employee.getImagePath() != null && !employee.getImagePath().isEmpty()) {
                        File imageFile = new File(employee.getImagePath());
                        if (imageFile.exists()) {
                            Image image = new Image(imageFile.toURI().toString());
                            imageView.setImage(image);
                        } else {
                            loadDefaultAvatar(imageView);
                        }
                    } else {
                        loadDefaultAvatar(imageView);
                    }
                    setGraphic(imageView);
                }
            }
        });
        
        colEmployeeName.setCellValueFactory(cellData -> cellData.getValue().employeeNameProperty());
        colPosition.setCellValueFactory(cellData -> cellData.getValue().positionProperty());
        colDepartment.setCellValueFactory(cellData -> cellData.getValue().departmentProperty());
        colEmail.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        colPhone.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        colHireDate.setCellValueFactory(cellData -> cellData.getValue().hireDateProperty());
        colSalary.setCellValueFactory(cellData -> cellData.getValue().salaryProperty().asObject());
        
        // Add action buttons to each row
        colActions.setCellFactory(param -> new javafx.scene.control.TableCell<Employee, Void>() {
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
                btnDelete.setTooltip(new javafx.scene.control.Tooltip("Delete Employee"));
                
                // View button action
                btnView.setOnAction(event -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    handleViewEmployee(employee);
                });
                
                // Delete button action
                btnDelete.setOnAction(event -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    handleDeleteEmployee(employee);
                });
                
                pane.getChildren().addAll(btnView, btnDelete);
                pane.setStyle("-fx-alignment: center;");
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
    
    private void loadEmployees() {
        employeeList.clear();
        String query = "SELECT * FROM employee ORDER BY employee_id DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Employee employee = new Employee(
                    rs.getInt("employee_id"),
                    rs.getString("employee_name"),
                    rs.getString("position"),
                    rs.getString("department"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("hire_date"),
                    rs.getDouble("salary"),
                    rs.getString("image_path")
                );
                employeeList.add(employee);
            }
            
            tableEmployees.setItems(employeeList);
            updateEmployeeCount();
            
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load employees: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAddEmployee() {
        showEmployeeDialog(null);
    }
    
    private void handleEditEmployee(Employee employee) {
        showEmployeeDialog(employee);
    }
    
    private void handleViewEmployee(Employee employee) {
        showEmployeeDialog(employee);
    }
    
    private void showEmployeeDialog(Employee employee) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/employeedialog.fxml"));
            Parent root = loader.load();
            
            EmployeeDialogController controller = loader.getController();
            if (employee != null) {
                controller.setEmployee(employee);
            }
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.setTitle(employee == null ? "Add New Employee" : "Edit Employee");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            
        } catch (Exception e) {
            showAlert("Error", "Failed to open employee dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleDeleteEmployee(Employee employee) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Employee");
        confirmAlert.setContentText("Are you sure you want to delete " + employee.getEmployeeName() + "?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteEmployee(employee.getEmployeeId());
            }
        });
    }
    
    private void deleteEmployee(int employeeId) {
        String query = "DELETE FROM employee WHERE employee_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, employeeId);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                showAlert("Success", "Employee deleted successfully!");
                loadEmployees();
                SharedDataModel.getInstance().triggerRefresh(); // Trigger refresh
            }
            
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to delete employee: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSearch() {
        String searchText = txtSearch.getText().trim().toLowerCase();
        
        if (searchText.isEmpty()) {
            loadEmployees();
            return;
        }
        
        ObservableList<Employee> filteredList = FXCollections.observableArrayList();
        
        for (Employee employee : employeeList) {
            if (employee.getEmployeeName().toLowerCase().contains(searchText) ||
                employee.getPosition().toLowerCase().contains(searchText) ||
                employee.getDepartment().toLowerCase().contains(searchText) ||
                employee.getEmail().toLowerCase().contains(searchText) ||
                employee.getPhone().contains(searchText)) {
                filteredList.add(employee);
            }
        }
        
        tableEmployees.setItems(filteredList);
        updateEmployeeCount();
    }
    
    @FXML
    private void handleRefresh() {
        txtSearch.clear();
        loadEmployees();
    }
    
    @FXML
    private void handleBack() {
        navigateTo("/View/FXML/homepage.fxml", "Tomas Car Accessories - Dashboard", btnDashboard);
    }
    
    @FXML
    private void handleUserManagement() {
        navigateTo("/View/FXML/usermanagement.fxml", "Tomas Car Accessories - User Management", btnUserManagement);
    }
    
    @FXML
    private void handleCustomerManagement() {
        navigateTo("/View/FXML/customermanagement.fxml", "Tomas Car Accessories - Customer Management", btnCustomerManagement);
    }
    
    @FXML
    private void handleServiceBooking() {
        navigateTo("/View/FXML/servicebooking.fxml", "Tomas Car Accessories - Service Booking", btnServiceBooking);
    }
    
    @FXML
    private void handleInventoryManagement() {
        navigateTo("/View/FXML/inventorymanagement.fxml", "Tomas Car Accessories - Inventory Management", btnInventory);
    }
    
    @FXML
    private void handleSalesManagement() {
        navigateTo("/View/FXML/salesmanagement.fxml", "Tomas Car Accessories - Sales Management", btnSales);
    }
    
    @FXML
    private void handleTransactions() {
        navigateTo("/View/FXML/recenttransactions.fxml", "Tomas Car Accessories - Transaction History", btnTransactions);
    }
    
    @FXML
    private void handleLogout() {
        navigateTo("/View/FXML/loginpage.fxml", "Tomas Car Accessories - Login", btnLogout);
    }
    
    private void navigateTo(String fxmlPath, String title, Button sourceButton) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage currentStage = (javafx.stage.Stage) sourceButton.getScene().getWindow();
            currentStage.getScene().setRoot(root);
            currentStage.setTitle(title);
            currentStage.setMaximized(false);
            currentStage.setWidth(1547);
            currentStage.setHeight(832);
            currentStage.centerOnScreen();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to navigate: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateEmployeeCount() {
        if (lblEmployeeCount != null) {
            lblEmployeeCount.setText("Total: " + tableEmployees.getItems().size());
        }
    }
    
    public void refreshTable() {
        loadEmployees();
    }
    
    private void loadDefaultAvatar(ImageView imageView) {
        try {
            // Try to load default avatar from resources
            Image defaultImage = new Image(getClass().getResourceAsStream("/View/pics/default-avatar.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            // If default avatar not found, create a simple placeholder
            // You can also use a Unicode character as fallback
            System.err.println("Default avatar not found");
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}