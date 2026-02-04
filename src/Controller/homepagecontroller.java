package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import Model.UserManagementModel;
import Model.UserSession;
import java.sql.*;

public class homepagecontroller {
    
    @FXML
    private Button btnDashboard;
    
    @FXML
    private Button btnUserManagement;
    
    @FXML
    private Button btnUserManagementQuick;
    
    @FXML
    private Button btnCustomerManagement;
    
    @FXML
    private Button btnServiceBooking;
    
    @FXML
    private Button btnInventory;
    
    @FXML
    private Button btnSales;
    
    @FXML
    private Button btnReports;
    
    @FXML
    private Button btnLogout;
    
    @FXML
    private Label lblTotalUsers;
    
    @FXML
    private Label lblTotalCustomers;
    
    private UserManagementModel userModel;
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    
    @FXML
    public void initialize() {
        System.out.println("Homepage loaded successfully!");
        
        // Initialize the user model
        userModel = new UserManagementModel();
        
        // Load and display the total user count
        loadUserCount();
        
        // Load and display the total customer count
        loadCustomerCount();
        
        // Apply role-based access control
        applyRoleBasedAccess();
    }
    
    // Method to apply role-based access control
    private void applyRoleBasedAccess() {
        String role = UserSession.getInstance().getUserRole();
        System.out.println("Applying role-based access for role: " + role);
        
        if (role == null) {
            role = "Staff"; // Default
        }
        
        // Admin: Full access to all pages
        if (role.equalsIgnoreCase("Admin")) {
            // All buttons visible (default state)
            System.out.println("Admin access granted - all features accessible");
        }
        // Manager: Remove User Management access
        else if (role.equalsIgnoreCase("Manager")) {
            btnUserManagement.setVisible(false);
            btnUserManagement.setManaged(false);
            if (btnUserManagementQuick != null) {
                btnUserManagementQuick.setVisible(false);
                btnUserManagementQuick.setManaged(false);
            }
            System.out.println("Manager access - User Management hidden");
        }
        // Staff: Remove Dashboard, User Management, Sales, and Reports access
        else if (role.equalsIgnoreCase("Staff")) {
            btnDashboard.setVisible(false);
            btnDashboard.setManaged(false);
            btnUserManagement.setVisible(false);
            btnUserManagement.setManaged(false);
            if (btnUserManagementQuick != null) {
                btnUserManagementQuick.setVisible(false);
                btnUserManagementQuick.setManaged(false);
            }
            btnSales.setVisible(false);
            btnSales.setManaged(false);
            btnReports.setVisible(false);
            btnReports.setManaged(false);
            System.out.println("Staff access - Dashboard, User Management, Sales, and Reports hidden");
        }
    }
    
    // Method to load and display the total user count
    private void loadUserCount() {
        try {
            int totalUsers = userModel.getTotalUserCount();
            lblTotalUsers.setText(String.valueOf(totalUsers));
            System.out.println("Total users loaded: " + totalUsers);
        } catch (Exception e) {
            System.err.println("Error loading user count: " + e.getMessage());
            lblTotalUsers.setText("0");
        }
    }
    
    // Method to load and display the total customer count
    private void loadCustomerCount() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM customers")) {
            
            if (rs.next()) {
                int totalCustomers = rs.getInt("count");
                lblTotalCustomers.setText(String.valueOf(totalCustomers));
                System.out.println("Total customers loaded: " + totalCustomers);
            }
        } catch (SQLException e) {
            System.err.println("Error loading customer count: " + e.getMessage());
            lblTotalCustomers.setText("0");
        }
    }
    
    @FXML
    private void handleUserManagement(ActionEvent event) {
        navigateTo("/View/FXML/usermanagement.fxml", "Tomas Car Accessories - User Management", btnUserManagement);
    }
    
    @FXML
    private void handleLogout(ActionEvent event) {
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
