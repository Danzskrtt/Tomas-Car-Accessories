package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import Model.CustomerCar;
import java.sql.*;

public class CarDialogController {
    
    @FXML private TextField txtMake;
    @FXML private TextField txtModel;
    @FXML private TextField txtYear;
    @FXML private TextField txtColor;
    @FXML private TextField txtPlateNumber;
    @FXML private TextArea txtNotes;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    
    private CustomerCar car;
    private int customerId;
    private CustomerManagementController parentController;
    private boolean saved = false;
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    
    public void setCar(CustomerCar car, int customerId) {
        this.car = car;
        this.customerId = customerId;
        
        if (car != null) {
            txtMake.setText(car.getMake());
            txtModel.setText(car.getModel());
            txtYear.setText(String.valueOf(car.getYear()));
            txtColor.setText(car.getColor());
            txtPlateNumber.setText(car.getPlateNumber());
            txtNotes.setText(car.getNotes());
        }
    }
    
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    
    public void setParentController(CustomerManagementController controller) {
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
        
        if (car == null) {
            insertCar();
        } else {
            updateCar();
        }
    }
    
    private boolean validateInput() {
        // Reset styles
        txtMake.setStyle("");
        txtModel.setStyle("");
        txtYear.setStyle("");
        txtPlateNumber.setStyle("");
        
        boolean isValid = true;
        TextField firstInvalidField = null;
        
        String make = txtMake.getText().trim();
        String model = txtModel.getText().trim();
        String yearStr = txtYear.getText().trim();
        String plateNumber = txtPlateNumber.getText().trim();
        
        if (make.isEmpty()) {
            txtMake.setStyle("-fx-border-color: #DC143C; -fx-border-width: 2; -fx-border-radius: 4;");
            isValid = false;
            if (firstInvalidField == null) firstInvalidField = txtMake;
        }
        
        if (model.isEmpty()) {
            txtModel.setStyle("-fx-border-color: #DC143C; -fx-border-width: 2; -fx-border-radius: 4;");
            isValid = false;
            if (firstInvalidField == null) firstInvalidField = txtModel;
        }
        
        if (yearStr.isEmpty()) {
            txtYear.setStyle("-fx-border-color: #DC143C; -fx-border-width: 2; -fx-border-radius: 4;");
            isValid = false;
            if (firstInvalidField == null) firstInvalidField = txtYear;
        } else {
            try {
                int year = Integer.parseInt(yearStr);
                if (year < 1900 || year > 2100) {
                    txtYear.setStyle("-fx-border-color: #DC143C; -fx-border-width: 2; -fx-border-radius: 4;");
                    isValid = false;
                    if (firstInvalidField == null) firstInvalidField = txtYear;
                }
            } catch (NumberFormatException e) {
                txtYear.setStyle("-fx-border-color: #DC143C; -fx-border-width: 2; -fx-border-radius: 4;");
                isValid = false;
                if (firstInvalidField == null) firstInvalidField = txtYear;
            }
        }
        
        if (plateNumber.isEmpty()) {
            txtPlateNumber.setStyle("-fx-border-color: #DC143C; -fx-border-width: 2; -fx-border-radius: 4;");
            isValid = false;
            if (firstInvalidField == null) firstInvalidField = txtPlateNumber;
        } else if (isPlateNumberDuplicate(plateNumber)) {
            txtPlateNumber.setStyle("-fx-border-color: #DC143C; -fx-border-width: 2; -fx-border-radius: 4;");
            isValid = false;
            if (firstInvalidField == null) firstInvalidField = txtPlateNumber;
            firstInvalidField.requestFocus();
            showStyledError("Vehicle with plate number '" + plateNumber + "' already exists!", "Duplicate Plate Number");
            return false;
        }
        
        if (!isValid) {
            firstInvalidField.requestFocus();
            showStyledError("Fill in the information!", "Validation Error");
            return false;
        }
        
        return true;
    }
    
    private boolean isPlateNumberDuplicate(String plateNumber) {
        String query = "SELECT COUNT(*) FROM customer_cars WHERE plate_number = ?";
        if (car != null) {
            query += " AND car_id != ?";
        }
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, plateNumber);
            if (car != null) {
                pstmt.setInt(2, car.getCarId());
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
    
    private void insertCar() {
        String query = "INSERT INTO customer_cars (customer_id, car_brand, model, year, color, plate_number, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, customerId);
            pstmt.setString(2, txtMake.getText().trim());
            pstmt.setString(3, txtModel.getText().trim());
            pstmt.setInt(4, Integer.parseInt(txtYear.getText().trim()));
            pstmt.setString(5, txtColor.getText().trim());
            pstmt.setString(6, txtPlateNumber.getText().trim());
            pstmt.setString(7, txtNotes.getText().trim());
            
            pstmt.executeUpdate();
            
            saved = true;
            showInfo("Vehicle added successfully!");
            if (parentController != null) {
                parentController.refreshData();
            }
            closeDialog();
            
        } catch (SQLException e) {
            showError("Error adding vehicle: " + e.getMessage());
        }
    }
    
    private void updateCar() {
        String query = "UPDATE customer_cars SET car_brand = ?, model = ?, year = ?, color = ?, plate_number = ?, notes = ? WHERE car_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, txtMake.getText().trim());
            pstmt.setString(2, txtModel.getText().trim());
            pstmt.setInt(3, Integer.parseInt(txtYear.getText().trim()));
            pstmt.setString(4, txtColor.getText().trim());
            pstmt.setString(5, txtPlateNumber.getText().trim());
            pstmt.setString(6, txtNotes.getText().trim());
            pstmt.setInt(7, car.getCarId());
            
            pstmt.executeUpdate();
            
            saved = true;
            showInfo("Vehicle updated successfully!");
            if (parentController != null) {
                parentController.refreshData();
            }
            closeDialog();
            
        } catch (SQLException e) {
            showError("Error updating vehicle: " + e.getMessage());
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
        styleDialog(alert);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        showStyledError(message, "Error");
    }

    /**
     * Reusable method for showing styled error alerts
     */
    private void showStyledError(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        
        Label contentLabel = new Label(message);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #DC143C; -fx-font-weight: bold;");
        alert.getDialogPane().setContent(contentLabel);
        
        styleDialog(alert);
        alert.showAndWait();
    }

    private void styleDialog(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        
        // Add some padding and shadow similar to the main form
        dialogPane.setPadding(new javafx.geometry.Insets(10));
        dialogPane.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.rgb(0,0,0,0.1)));
    }
}