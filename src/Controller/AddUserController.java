package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.*;

public class AddUserController {
    
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private ComboBox<String> cmbRole;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    private boolean saved = false;
    
    @FXML
    public void initialize() {
        // Populate role combo box
        cmbRole.getItems().addAll("Admin", "Manager", "Staff");
        cmbRole.getSelectionModel().select("Staff");
    }
    
    @FXML
    private void handleSave() {
        if (validateInput()) {
            if (saveUser()) {
                saved = true;
                showAlert("Success", "User has been created successfully!", Alert.AlertType.INFORMATION);
                closeDialog();
            }
        }
    }
    
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    private boolean validateInput() {
        if (txtUsername.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a username.", Alert.AlertType.ERROR);
            return false;
        } else if (isUsernameDuplicate(txtUsername.getText().trim())) {
            showAlert("Validation Error", "Username '" + txtUsername.getText().trim() + "' is already taken.", Alert.AlertType.ERROR);
            return false;
        }
        
        if (txtPassword.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a password.", Alert.AlertType.ERROR);
            return false;
        }
        
        if (txtFirstName.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a first name.", Alert.AlertType.ERROR);
            return false;
        }
        
        if (txtLastName.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a last name.", Alert.AlertType.ERROR);
            return false;
        }
        
        if (txtEmail.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter an email.", Alert.AlertType.ERROR);
            return false;
        }
        
        if (cmbRole.getValue() == null) {
            showAlert("Validation Error", "Please select a role.", Alert.AlertType.ERROR);
            return false;
        }
        
        return true;
    }
    
    private boolean isUsernameDuplicate(String username) {
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean saveUser() {
        String sql = "INSERT INTO Users (username, password, first_name, last_name, email, role, phone) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, txtUsername.getText().trim());
            pstmt.setString(2, txtPassword.getText().trim());
            pstmt.setString(3, txtFirstName.getText().trim());
            pstmt.setString(4, txtLastName.getText().trim());
            pstmt.setString(5, txtEmail.getText().trim());
            pstmt.setString(6, cmbRole.getValue());
            pstmt.setString(7, txtPhone.getText().trim());
            
            pstmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            showAlert("Database Error", "Error saving user: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
    
    public boolean isSaved() {
        return saved;
    }
}