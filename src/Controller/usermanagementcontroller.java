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
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
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
    @FXML private TableColumn<User, Integer> colIsActive;
    @FXML private TableColumn<User, String> colCreatedAt;
    @FXML private TableColumn<User, String> colLastLogin;
    
    @FXML private TextField searchField;
    @FXML private Label lblTotalUsers;
    
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnView;
    @FXML private Button btnSearch;
    @FXML private Button btnRefresh;
    @FXML private Button btnDashboard;
    @FXML private Button btnUserManagement;
    @FXML private Button btnCustomerManagement;
    @FXML private Button btnServiceBooking;
    @FXML private Button btnInventory;
    @FXML private Button btnSales;
    @FXML private Button btnReports;
    @FXML private Button btnLogout;
    
    private UserManagementModel model;
    
    @FXML
    public void initialize() {
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
        colIsActive.setCellValueFactory(new PropertyValueFactory<>("isActive"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colLastLogin.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));
        
        // Custom cell factory for Active column with color coding
        colIsActive.setCellFactory(column -> new TableCell<User, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    if (item == 1) {
                        setText("Active");
                        setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else {
                        setText("Inactive");
                        setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    }
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
        Optional<User> result = showUserDialog(null, "Add New User");
        if (result.isPresent()) {
            User newUser = result.get();
            if (model.addUser(newUser)) {
                loadUsers();
                showAlert(Alert.AlertType.INFORMATION, "Success", "User added successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add user. Username may already exist.");
            }
        }
    }
    
    // Handle Edit button
    @FXML
    private void handleEdit() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to edit.");
            return;
        }
        
        Optional<User> result = showUserDialog(selectedUser, "Edit User");
        if (result.isPresent()) {
            User updatedUser = result.get();
            if (model.updateUser(updatedUser)) {
                loadUsers();
                showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user.");
            }
        }
    }
    
    // Handle Delete button
    @FXML
    private void handleDelete() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to delete.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete User");
        confirmAlert.setContentText("Are you sure you want to delete user: " + selectedUser.getUsername() + "?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (model.deleteUser(selectedUser.getUserId())) {
                loadUsers();
                showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user.");
            }
        }
    }
    
    // Handle View button
    @FXML
    private void handleView() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to view.");
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("User ID: ").append(selectedUser.getUserId()).append("\n");
        details.append("Username: ").append(selectedUser.getUsername()).append("\n");
        details.append("First Name: ").append(selectedUser.getFirstName()).append("\n");
        details.append("Last Name: ").append(selectedUser.getLastName()).append("\n");
        details.append("Email: ").append(selectedUser.getEmail()).append("\n");
        details.append("Role: ").append(selectedUser.getRole()).append("\n");
        details.append("Phone: ").append(selectedUser.getPhone()).append("\n");
        details.append("Status: ").append(selectedUser.getIsActive() == 1 ? "Active" : "Inactive").append("\n");
        details.append("Created At: ").append(selectedUser.getCreatedAt()).append("\n");
        details.append("Last Login: ").append(selectedUser.getLastLogin());
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Details");
        alert.setHeaderText("Details for: " + selectedUser.getUsername());
        alert.setContentText(details.toString());
        alert.showAndWait();
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
    
    // Show user dialog for add/edit
    private Optional<User> showUserDialog(User user, String title) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(user == null ? "Enter new user information" : "Edit user information");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField txtUsername = new TextField(user != null ? user.getUsername() : "");
        txtUsername.setPromptText("Username");
        PasswordField txtPassword = new PasswordField();
        txtPassword.setText(user != null ? user.getPassword() : "");
        txtPassword.setPromptText("Password");
        TextField txtFirstName = new TextField(user != null ? user.getFirstName() : "");
        txtFirstName.setPromptText("First Name");
        TextField txtLastName = new TextField(user != null ? user.getLastName() : "");
        txtLastName.setPromptText("Last Name");
        TextField txtEmail = new TextField(user != null ? user.getEmail() : "");
        txtEmail.setPromptText("Email");
        ComboBox<String> cmbRole = new ComboBox<>();
        cmbRole.getItems().addAll("Admin", "Manager", "Staff");
        cmbRole.setValue(user != null ? user.getRole() : "Staff");
        TextField txtPhone = new TextField(user != null ? user.getPhone() : "");
        txtPhone.setPromptText("Phone");
        CheckBox chkActive = new CheckBox("Active");
        chkActive.setSelected(user == null || user.getIsActive() == 1);
        
        grid.add(new Label("Username:"), 0, 0);
        grid.add(txtUsername, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(txtPassword, 1, 1);
        grid.add(new Label("First Name:"), 0, 2);
        grid.add(txtFirstName, 1, 2);
        grid.add(new Label("Last Name:"), 0, 3);
        grid.add(txtLastName, 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(txtEmail, 1, 4);
        grid.add(new Label("Role:"), 0, 5);
        grid.add(cmbRole, 1, 5);
        grid.add(new Label("Phone:"), 0, 6);
        grid.add(txtPhone, 1, 6);
        grid.add(new Label("Status:"), 0, 7);
        grid.add(chkActive, 1, 7);
        
        dialog.getDialogPane().setContent(grid);
       
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                User resultUser = new User();
                if (user != null) {
                    resultUser.setUserId(user.getUserId());
                }
                resultUser.setUsername(txtUsername.getText());
                resultUser.setPassword(txtPassword.getText());
                resultUser.setFirstName(txtFirstName.getText());
                resultUser.setLastName(txtLastName.getText());
                resultUser.setEmail(txtEmail.getText());
                resultUser.setRole(cmbRole.getValue());
                resultUser.setPhone(txtPhone.getText());
                resultUser.setIsActive(chkActive.isSelected() ? 1 : 0);
                return resultUser;
            }
            return null;
        });
        
        return dialog.showAndWait();
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
        lblTotalUsers.setText("Total Users: " + count);
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
            System.out.println(title + " loaded successfully!");
        } catch (Exception e) {
            System.err.println("Error loading page: " + e.getMessage());
            e.printStackTrace();
        }
    }
}