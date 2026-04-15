package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import Model.CustomerCar;

import java.io.File;

public class VehicleCardController {
    
    @FXML private StackPane imageContainer;
    @FXML private ImageView carImageView;
    @FXML private Button btnChangeImage;
    @FXML private Label lblMakeModel;
    @FXML private Label lblYear;
    @FXML private Label lblColor;
    @FXML private Label lblPlate;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    
    private CustomerCar vehicle;
    private VehicleCardActionListener listener;
    
    // Interface for parent to handle actions
    public interface VehicleCardActionListener {
        void onChangeImage(CustomerCar vehicle);
        void onEditVehicle(CustomerCar vehicle);
        void onDeleteVehicle(CustomerCar vehicle);
    }
    
    @FXML
    public void initialize() {
        // Setup hover effect for change image button
        imageContainer.setOnMouseEntered(e -> btnChangeImage.setOpacity(1));
        imageContainer.setOnMouseExited(e -> btnChangeImage.setOpacity(0));
    }
    
    // Set the vehicle data and display it
    public void setVehicle(CustomerCar vehicle) {
        this.vehicle = vehicle;
        displayVehicleInfo();
        loadVehicleImage();
    }
    
    // Set the action listener for parent communication
    public void setListener(VehicleCardActionListener listener) {
        this.listener = listener;
    }
    
    // Display vehicle information on labels
    private void displayVehicleInfo() {
        lblMakeModel.setText(vehicle.getMake() + " " + vehicle.getModel());
        lblYear.setText(String.valueOf(vehicle.getYear()));
        lblColor.setText(vehicle.getColor());
        lblPlate.setText("🚗 " + vehicle.getLicensePlate());
    }
    
    // Load vehicle image or default placeholder
    private void loadVehicleImage() {
        if (vehicle.getCarImagePath() != null && !vehicle.getCarImagePath().isEmpty()) {
            try {
                File imageFile = new File(vehicle.getCarImagePath());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    carImageView.setImage(image);
                } else {
                    setDefaultCarImage();
                }
            } catch (Exception e) {
                setDefaultCarImage();
            }
        } else {
            setDefaultCarImage();
        }
    }
    
    // Set default placeholder image
    private void setDefaultCarImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/View/pics/default-car.png"));
            carImageView.setImage(defaultImage);
        } catch (Exception e) {
            // If default image not found, set gray background
            carImageView.setStyle("-fx-background-color: #e9ecef;");
        }
    }
    
    // Refresh the image display (called after image update)
    public void refreshImage() {
        loadVehicleImage();
    }
    
    // Handle change image button click
    @FXML
    private void handleChangeImage() {
        if (listener != null) {
            listener.onChangeImage(vehicle);
        }
    }
    
    // Handle edit button click
    @FXML
    private void handleEdit() {
        if (listener != null) {
            listener.onEditVehicle(vehicle);
        }
    }
    
    // Handle delete button click
    @FXML
    private void handleDelete() {
        if (listener != null) {
            listener.onDeleteVehicle(vehicle);
        }
    }
}
