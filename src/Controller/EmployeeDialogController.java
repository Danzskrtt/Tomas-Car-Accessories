package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import Model.Employee;
import Model.SharedDataModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EmployeeDialogController {
    
    @FXML private TextField txtEmployeeName;
    @FXML private TextField txtPosition;
    @FXML private TextField txtDepartment;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private DatePicker dateHireDate;
    @FXML private TextField txtSalary;
    @FXML private ImageView imgEmployeePhoto;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Button btnChooseImage;
    
    private Employee employee;
    private EmployeeManagementController parentController;
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    private static final String IMAGES_FOLDER = "employee_images";
    private String selectedImagePath = null;
    
    @FXML
    public void initialize() {
        // Set default hire date to today
        dateHireDate.setValue(LocalDate.now());
        
        // Add numeric validation to salary field
        txtSalary.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                txtSalary.setText(oldValue);
            }
        });
        
        // Create images folder if it doesn't exist
        createImagesFolderIfNotExists();
    }
    
    public void setEmployee(Employee employee) {
        this.employee = employee;
        
        if (employee != null) {
            txtEmployeeName.setText(employee.getEmployeeName());
            txtPosition.setText(employee.getPosition());
            txtDepartment.setText(employee.getDepartment());
            txtEmail.setText(employee.getEmail());
            txtPhone.setText(employee.getPhone());
            txtSalary.setText(String.valueOf(employee.getSalary()));
            
            // Parse hire date
            if (employee.getHireDate() != null && !employee.getHireDate().isEmpty()) {
                try {
                    LocalDate hireDate = LocalDate.parse(employee.getHireDate());
                    dateHireDate.setValue(hireDate);
                } catch (Exception e) {
                    dateHireDate.setValue(LocalDate.now());
                }
            }
            
            // Load employee image
            if (employee.getImagePath() != null && !employee.getImagePath().isEmpty()) {
                selectedImagePath = employee.getImagePath();
                loadEmployeeImage(employee.getImagePath());
            }
        }
    }
    
    public void setParentController(EmployeeManagementController controller) {
        this.parentController = controller;
    }
    
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }
        
        String employeeName = txtEmployeeName.getText().trim();
        String position = txtPosition.getText().trim();
        String department = txtDepartment.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String hireDate = dateHireDate.getValue().toString();
        double salary = Double.parseDouble(txtSalary.getText().trim());
        
        if (employee == null) {
            // Add new employee
            insertEmployee(employeeName, position, department, email, phone, hireDate, salary, selectedImagePath);
        } else {
            // Update existing employee
            updateEmployee(employee.getEmployeeId(), employeeName, position, department, email, phone, hireDate, salary, selectedImagePath);
        }
    }
    
    private boolean validateInput() {
        if (txtEmployeeName.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Employee name is required!");
            return false;
        }
        
        String employeeName = txtEmployeeName.getText().trim();
        if (isNameDuplicate(employeeName)) {
            showAlert("Validation Error", "Employee name '" + employeeName + "' is already registered.");
            return false;
        }
        
        if (txtPosition.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Position is required!");
            return false;
        }
        
        if (txtDepartment.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Department is required!");
            return false;
        }
        
        if (txtSalary.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Salary is required!");
            return false;
        }
        
        try {
            double salary = Double.parseDouble(txtSalary.getText().trim());
            if (salary < 0) {
                showAlert("Validation Error", "Salary must be a positive number!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Invalid salary format!");
            return false;
        }
        
        if (dateHireDate.getValue() == null) {
            showAlert("Validation Error", "Hire date is required!");
            return false;
        }
        
        return true;
    }
    
    private boolean isNameDuplicate(String name) {
        String query = "SELECT COUNT(*) FROM employee WHERE employee_name COLLATE NOCASE = ?";
        if (employee != null) {
            query += " AND employee_id != ?";
        }
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            if (employee != null) {
                pstmt.setInt(2, employee.getEmployeeId());
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
    
    private void insertEmployee(String name, String position, String department, 
                               String email, String phone, String hireDate, 
                               double salary, String imagePath) {
        String query = "INSERT INTO employee (employee_name, position, department, email, phone, hire_date, salary, image_path) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, position);
            pstmt.setString(3, department);
            pstmt.setString(4, email);
            pstmt.setString(5, phone);
            pstmt.setString(6, hireDate);
            pstmt.setDouble(7, salary);
            pstmt.setString(8, imagePath);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                showAlert("Success", "Employee added successfully!");
                if (parentController != null) {
                    parentController.refreshTable();
                    SharedDataModel.getInstance().triggerRefresh(); // Trigger refresh
                }
                handleCancel();
            }
            
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to add employee: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateEmployee(int employeeId, String name, String position, 
                               String department, String email, String phone, 
                               String hireDate, double salary, String imagePath) {
        String query = "UPDATE employee SET employee_name = ?, position = ?, department = ?, " +
                      "email = ?, phone = ?, hire_date = ?, salary = ?, image_path = ?, " +
                      "updated_at = CURRENT_TIMESTAMP WHERE employee_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, position);
            pstmt.setString(3, department);
            pstmt.setString(4, email);
            pstmt.setString(5, phone);
            pstmt.setString(6, hireDate);
            pstmt.setDouble(7, salary);
            pstmt.setString(8, imagePath);
            pstmt.setInt(9, employeeId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                showAlert("Success", "Employee updated successfully!");
                if (parentController != null) {
                    parentController.refreshTable();
                    SharedDataModel.getInstance().triggerRefresh(); // Trigger refresh
                }
                handleCancel();
            }
            
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to update employee: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) txtEmployeeName.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Employee Photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        
        Stage stage = (Stage) btnChooseImage.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {
                // Create a unique filename using timestamp
                String timestamp = String.valueOf(System.currentTimeMillis());
                String extension = getFileExtension(selectedFile.getName());
                String newFileName = "employee_" + timestamp + extension;
                
                // Copy file to employee_images folder
                Path sourcePath = selectedFile.toPath();
                Path destPath = Paths.get(IMAGES_FOLDER, newFileName);
                Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Store relative path
                selectedImagePath = IMAGES_FOLDER + "/" + newFileName;
                
                // Load and display the image
                loadEmployeeImage(selectedImagePath);
                
            } catch (IOException e) {
                showAlert("Error", "Failed to copy image file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleUploadPhoto() {
        handleChooseImage();
    }
    
    private void createImagesFolderIfNotExists() {
        try {
            Path imagesPath = Paths.get(IMAGES_FOLDER);
            if (!Files.exists(imagesPath)) {
                Files.createDirectories(imagesPath);
            }
        } catch (IOException e) {
            System.err.println("Failed to create images folder: " + e.getMessage());
        }
    }
    
    private void loadEmployeeImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                imgEmployeePhoto.setImage(image);
            } else {
                // Load default avatar if image not found
                try {
                    Image defaultImage = new Image(getClass().getResourceAsStream("/View/pics/default-avatar.png"));
                    imgEmployeePhoto.setImage(defaultImage);
                } catch (Exception ex) {
                    System.err.println("Default avatar not found");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load image: " + e.getMessage());
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return fileName.substring(lastIndexOf);
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
