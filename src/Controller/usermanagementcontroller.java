package Controller;

import Model.User;
import Model.UserManagementModel;
import Model.UserSession;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.util.Optional;

public class usermanagementcontroller {
    
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colPassword;
    @FXML private TableColumn<User, String> colFirstName;
    @FXML private TableColumn<User, String> colLastName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, Void> colActions;
    
    @FXML private TextField searchField;
    @FXML private Label lblTotalUsers;
    
    @FXML private Button btnAdd;
    @FXML private Button btnSearch;
    @FXML private Button btnRefresh;
    @FXML private Button btnDashboard;
    @FXML private Button btnUserManagement;
    @FXML private Button btnCustomerManagement;
    @FXML private Button btnServiceBooking;
    @FXML private Button btnInventory;
    @FXML private Button btnSales;
    @FXML private Button btnEmployeeManagement;
    @FXML private Button btnTransactions;
    @FXML private Button btnReports;
    @FXML private Button btnLogout;
    
    private UserManagementModel model;
    
    @FXML
    public void initialize() {
        NavigationManager.applyRoleBasedAccess(this);
        model = new UserManagementModel();
        
        // Setup table columns
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        // Add action buttons to each row
        colActions.setCellFactory(param -> new TableCell<User, Void>() {
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
                btnView.setTooltip(new Tooltip("View Details"));
                
                // Style Delete button
                btnDelete.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                                 "-fx-padding: 8 12; -fx-background-radius: 6; " +
                                 "-fx-cursor: hand;");
                btnDelete.setTooltip(new Tooltip("Delete User"));
                
                // View button action
                btnView.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleViewUser(user);
                });
                
                // Delete button action
                btnDelete.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
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
        
        // Load initial data
        loadUsers();
        
        // Apply role-based access control
        applyRoleBasedAccess();
    }
    
    //Apply role-based access control
    private void applyRoleBasedAccess() {
        String role = UserSession.getInstance().getUserRole();
        System.out.println("User Management - Applying role-based access for role: " + role);
        
        if (role == null) {
            role = "Staff";
        }
        
        if (role.equalsIgnoreCase("Manager")) {
            System.out.println("Manager should not have access to User Management");
        } else if (role.equalsIgnoreCase("Staff")) {
            btnDashboard.setVisible(false);
            btnDashboard.setManaged(false);
            btnSales.setVisible(false);
            btnSales.setManaged(false);
            btnReports.setVisible(false);
            btnReports.setManaged(false);
            System.out.println("Staff access - Dashboard, Sales, and Reports hidden");
        }
    }
    
    //Logout
    @FXML
    private void handleBack() {
        try {
            // Load the homepage FXML
            Parent root = FXMLLoader.load(getClass().getResource("/View/FXML/homepage.fxml"));
            
            // Get the current stage (window) from the dashboard button
            Stage currentStage = (Stage) btnDashboard.getScene().getWindow();
            
            currentStage.getScene().setRoot(root);
            currentStage.setTitle("Tomas Car Accessories - Homepage");
            
            System.out.println("Returned to homepage successfully!");
            
        } catch (Exception e) {
            System.err.println("Error loading homepage: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Load all users from database
    private void loadUsers() {
        ObservableList<User> users = model.getAllUsers();
        userTable.setItems(users);
        updateTotalUsers(users.size());
    }
    
    // Handle Add button
    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/adduser.fxml"));
            Parent root = loader.load();
            
            AddUserController controller = loader.getController();
            
            Stage stage = new Stage();
            stage.setTitle("Add New User");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            if (controller.isSaved()) {
                loadUsers();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open add user dialog: " + e.getMessage());
        }
    }
    
    // Handle Delete button
    private void handleDeleteUser(User user) {
        if (user == null) {
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete User");
        confirmAlert.setContentText("Are you sure you want to delete user: " + user.getUsername() + "?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (model.deleteUser(user.getUserId())) {
                loadUsers();
                showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user.");
            }
        }
    }
    
    // Handle View button
    private void handleViewUser(User user) {
        if (user == null) {
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/viewuser.fxml"));
            Parent root = loader.load();
            
            ViewUserController controller = loader.getController();
            controller.setUser(user);
            
            // Set callback to reload users when edited from view dialog
            controller.setOnUserUpdated(() -> loadUsers());
            
            Stage stage = new Stage();
            stage.setTitle("User Details");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open view user dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadUsers();
        } else {
            ObservableList<User> searchResults = model.searchUsers(searchTerm);
            userTable.setItems(searchResults);
            updateTotalUsers(searchResults.size());
        }
    }
    
    // Handle Refresh button
    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadUsers();
    }
    

    
    // Helper method to show alerts
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Update total users label
    private void updateTotalUsers(int count) {
        lblTotalUsers.setText("Total: " + count);
    }
    
    // Navigation handlers
    @FXML
    private void handleCustomerManagement() {
        navigateTo("/View/FXML/customermanagement.fxml", "Customer Management", btnCustomerManagement);
    }
    
    @FXML
    private void handleServiceBooking() {
        navigateTo("/View/FXML/servicebooking.fxml", "Service Booking", btnServiceBooking);
    }
    
    @FXML
    private void handleInventoryManagement() {
        navigateTo("/View/FXML/inventorymanagement.fxml", "Inventory Management", btnInventory);
    }
    
    @FXML
    private void handleSalesManagement() {
        navigateTo("/View/FXML/salesmanagement.fxml", "Sales Management", btnSales);
    }
    
    @FXML
    private void handleTransactions() {
        navigateTo("/View/FXML/recenttransactions.fxml", "Transaction History", btnTransactions);
    }
    
    @FXML
    private void handleEmployeeManagement() {
        navigateTo("/View/FXML/employeemanagement.fxml", "Employee Management", btnEmployeeManagement);
    }
    
    // Handle logout
    @FXML
    private void handleLogout() {
        navigateTo("/View/FXML/loginpage.fxml", "Tomas Car Accessories - Login", btnLogout);
    }
    
    private void navigateTo(String fxmlPath, String title, Button sourceButton) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage currentStage = (Stage) sourceButton.getScene().getWindow();
            currentStage.getScene().setRoot(root);
            currentStage.setTitle(title);
            currentStage.setMaximized(false);
            currentStage.setWidth(1547);
            currentStage.setHeight(832);
            currentStage.centerOnScreen();
            System.out.println(title + " loaded successfully!");
        } catch (Exception e) {
            System.err.println("Error loading page: " + e.getMessage());
            e.printStackTrace();
        }
    }
}