package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import Model.Customer;
import Model.CustomerCar;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDetailsDialogController {
    
    @FXML private Label lblCustomerId;
    @FXML private Label lblCustomerName;
    @FXML private Label lblCustomerEmail;
    @FXML private Label lblCustomerPhone;
    @FXML private Label lblCustomerAddress;
    @FXML private Label lblVehicleCount;
    @FXML private Label lblNoVehicles;
    @FXML private Button btnClose;
    @FXML private Button btnCloseBottom;
    @FXML private Button btnEditCustomer;
    @FXML private Button btnAddVehicle;
    @FXML private Button btnViewServiceHistory;
    @FXML private FlowPane vehicleCardsContainer;
    
    private Customer customer;
    private List<CustomerCar> vehicles;
    private Runnable onDataChanged;
    
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            displayCustomerInfo();
            loadVehicles();
        }
    }
    
    public void setOnDataChanged(Runnable callback) {
        this.onDataChanged = callback;
    }
    
    private void displayCustomerInfo() {
        lblCustomerId.setText("Customer ID: #" + customer.getCustomerId());
        lblCustomerName.setText(customer.getCustomerName());
        lblCustomerEmail.setText(customer.getCustomerEmail());
        lblCustomerPhone.setText(customer.getCustomerPhone());
        lblCustomerAddress.setText(customer.getCustomerAddress());
    }
    
    private void loadVehicles() {
        vehicles = new ArrayList<>();
        String query = "SELECT * FROM customer_cars WHERE customer_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, customer.getCustomerId());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                CustomerCar car = new CustomerCar(
                    rs.getInt("car_id"),
                    rs.getInt("customer_id"),
                    rs.getString("car_brand"),
                    rs.getString("model"),
                    rs.getInt("year"),
                    rs.getString("color"),
                    rs.getString("plate_number"),
                    rs.getString("car_image_path")
                );
                vehicles.add(car);
            }
            
            displayVehicles();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load vehicles: " + e.getMessage());
        }
    }
    
    private void displayVehicles() {
        vehicleCardsContainer.getChildren().clear();
        
        if (vehicles.isEmpty()) {
            lblNoVehicles.setVisible(true);
            lblNoVehicles.setManaged(true);
            vehicleCardsContainer.getChildren().add(lblNoVehicles);
            lblVehicleCount.setText("0");
        } else {
            lblNoVehicles.setVisible(false);
            lblNoVehicles.setManaged(false);
            lblVehicleCount.setText(String.valueOf(vehicles.size()));
            
            for (CustomerCar car : vehicles) {
                try {
                    // Load the vehicle card FXML
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/vehiclecard.fxml"));
                    Parent cardNode = loader.load();
                    
                    // Get the controller and set up the vehicle
                    VehicleCardController cardController = loader.getController();
                    cardController.setVehicle(car);
                    
                    // Set up action listener
                    cardController.setListener(new VehicleCardController.VehicleCardActionListener() {
                        @Override
                        public void onChangeImage(CustomerCar vehicle) {
                            handleChangeCarImage(vehicle);
                        }
                        
                        @Override
                        public void onEditVehicle(CustomerCar vehicle) {
                            handleEditVehicle(vehicle);
                        }
                        
                        @Override
                        public void onDeleteVehicle(CustomerCar vehicle) {
                            handleDeleteVehicle(vehicle);
                        }
                    });
                    
                    vehicleCardsContainer.getChildren().add(cardNode);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to load vehicle card: " + e.getMessage());
                }
            }
        }
    }
    
    private void handleChangeCarImage(CustomerCar car) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Car Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        
        Stage stage = (Stage) vehicleCardsContainer.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            String imagePath = selectedFile.getAbsolutePath();
            
            // Update database
            String query = "UPDATE customer_cars SET car_image_path = ? WHERE car_id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, imagePath);
                stmt.setInt(2, car.getCarId());
                stmt.executeUpdate();
                
                car.setCarImagePath(imagePath);
                loadVehicles();
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Car image updated successfully!");
                
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update car image: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleEditCustomer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/customerdialog.fxml"));
            Parent root = loader.load();
            
            CustomerDialogController controller = loader.getController();
            controller.setCustomer(customer);
            
            Stage stage = new Stage();
            stage.setTitle("Edit Customer");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            if (controller.isSaved()) {
                // Reload customer data
                reloadCustomerData();
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open edit customer dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAddVehicle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/cardialog.fxml"));
            Parent root = loader.load();
            
            CarDialogController controller = loader.getController();
            controller.setCustomerId(customer.getCustomerId());
            
            Stage stage = new Stage();
            stage.setTitle("Add Vehicle");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            if (controller.isSaved()) {
                loadVehicles();
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open add vehicle dialog: " + e.getMessage());
        }
    }
    
    private void handleEditVehicle(CustomerCar car) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/cardialog.fxml"));
            Parent root = loader.load();
            
            CarDialogController controller = loader.getController();
            controller.setCar(car, customer.getCustomerId());
            
            Stage stage = new Stage();
            stage.setTitle("Edit Vehicle");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            if (controller.isSaved()) {
                loadVehicles();
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open edit vehicle dialog: " + e.getMessage());
        }
    }
    
    private void handleDeleteVehicle(CustomerCar car) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Vehicle");
        confirmAlert.setContentText("Are you sure you want to delete this vehicle?\n" + 
                                   car.getMake() + " " + car.getModel() + " (" + car.getLicensePlate() + ")");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String query = "DELETE FROM customer_cars WHERE car_id = ?";
            
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setInt(1, car.getCarId());
                stmt.executeUpdate();
                
                loadVehicles();
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Vehicle deleted successfully!");
                
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete vehicle: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleViewServiceHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/ServiceHistoryDialog.fxml"));
            Parent root = loader.load();
            
            // Assume ServiceHistoryDialogController exists and takes customer
            ServiceHistoryDialogController controller = loader.getController();
            controller.setCustomer(customer);
            
            Stage stage = new Stage();
            stage.setTitle("Service History - " + customer.getCustomerName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open service history: " + e.getMessage());
        }
    }
    
    private void reloadCustomerData() {
        String query = "SELECT * FROM customers WHERE customer_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, customer.getCustomerId());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                customer = new Customer(
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
                displayCustomerInfo();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleClose() {
        Stage stage = null;
        if (btnClose != null) {
            stage = (Stage) btnClose.getScene().getWindow();
        } else if (btnCloseBottom != null) {
            stage = (Stage) btnCloseBottom.getScene().getWindow();
        }
        
        if (stage != null) {
            stage.close();
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
