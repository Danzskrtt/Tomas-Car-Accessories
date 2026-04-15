package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import Model.Customer;
import Model.ActivityManager;
import Model.UserSession;
import java.sql.*;

public class CustomerDialogController {
    
    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextArea txtAddress;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    
    private Customer customer;
    private Object parentController;
    private boolean saved = false;
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            txtName.setText(customer.getCustomerName());
            txtEmail.setText(customer.getCustomerEmail());
            txtPhone.setText(customer.getCustomerPhone());
            txtAddress.setText(customer.getCustomerAddress());
        }
    }
    
    public void setParentController(Object controller) {
        this.parentController = controller;
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }
        
        if (customer == null) {
            insertCustomer();
        } else {
            updateCustomer();
        }
    }
    
    private boolean validateInput() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        
        if (name.isEmpty()) {
            showError("Customer name is required!");
            return false;
        }
        
        if (phone.isEmpty()) {
            showError("Phone number is required!");
            return false;
        } else if (isPhoneDuplicate(phone)) {
            showError("Phone number '" + phone + "' is already registered to another customer.");
            return false;
        }
        
        if (!email.isEmpty() && isEmailDuplicate(email)) {
            showError("Email address '" + email + "' is already registered to another customer.");
            return false;
        }
        
        return true;
    }
    
    private boolean isPhoneDuplicate(String phone) {
        String query = "SELECT COUNT(*) FROM customers WHERE customer_phone = ?";
        if (customer != null) {
            query += " AND customer_id != ?";
        }
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, phone);
            if (customer != null) {
                pstmt.setInt(2, customer.getCustomerId());
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean isEmailDuplicate(String email) {
        String query = "SELECT COUNT(*) FROM customers WHERE customer_email = ?";
        if (customer != null) {
            query += " AND customer_id != ?";
        }
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            if (customer != null) {
                pstmt.setInt(2, customer.getCustomerId());
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private void insertCustomer() {
        String query = "INSERT INTO customers (customer_name, customer_email, customer_phone, customer_address) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, txtName.getText().trim());
            pstmt.setString(2, txtEmail.getText().trim());
            pstmt.setString(3, txtPhone.getText().trim());
            pstmt.setString(4, txtAddress.getText().trim());
            
            pstmt.executeUpdate();
            
            saved = true;
            showInfo("Customer added successfully!");
            // Log activity for new customer
            String userName = UserSession.getInstance().getUsername();
            String description = String.format("Added new customer: %s (Phone: %s)", 
                    txtName.getText().trim(), txtPhone.getText().trim());
            ActivityManager.logActivity("CUSTOMER_ADDED", description, userName);
            

            closeDialog();
            
        } catch (SQLException e) {
            showError("Error adding customer: " + e.getMessage());
        }
    }
    
    private void updateCustomer() {
        String query = "UPDATE customers SET customer_name = ?, customer_email = ?, customer_phone = ?, customer_address = ? WHERE customer_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, txtName.getText().trim());
            pstmt.setString(2, txtEmail.getText().trim());
            pstmt.setString(3, txtPhone.getText().trim());
            pstmt.setString(4, txtAddress.getText().trim());
            pstmt.setInt(5, customer.getCustomerId());
            
            pstmt.executeUpdate();
            
            saved = true;
            showInfo("Customer updated successfully!");

            closeDialog();
            
        } catch (SQLException e) {
            showError("Error updating customer: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    private void closeDialog() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
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