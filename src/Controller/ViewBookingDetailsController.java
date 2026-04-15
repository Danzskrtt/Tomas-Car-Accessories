package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import Model.ServiceBooking;
import java.io.File;
import java.text.DecimalFormat;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewBookingDetailsController {

    @FXML private Label lblHeaderSubtitle;
    @FXML private Label lblStatusBadge;
    
    @FXML private Label lblCustomerName;
    @FXML private Label lblVehicleInfo;
    @FXML private FlowPane flowServiceTypes;
    @FXML private Label lblBookingDate;
    @FXML private Label lblNotes;
    
    @FXML private ImageView imgVehiclePhoto;
    @FXML private Label lblNoImage;
    
    @FXML private ListView<String> listStaff;
    @FXML private ListView<String> listProducts;
    
    @FXML private Label lblTotalCost;
    @FXML private Label lblDownpayment;
    @FXML private Label lblBalance;
    
    private ServiceBooking booking;
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    
    public void initData(ServiceBooking booking) {
        this.booking = booking;
        
        lblHeaderSubtitle.setText("Booking # " + booking.getBookingNumber());
        
        lblCustomerName.setText(booking.getCustomerName() != null ? booking.getCustomerName() : "-");
        lblVehicleInfo.setText(booking.getCarDescription() != null ? booking.getCarDescription() : "-");
        
        flowServiceTypes.getChildren().clear();
        String svcType = booking.getServiceType();
        if (svcType != null && !svcType.isEmpty()) {
            String[] services = svcType.split(",");
            for (String service : services) {
                if (!service.trim().isEmpty()) {
                    Label chip = new Label(service.trim());
                    chip.setStyle("-fx-background-color: #f1f2f6; -fx-text-fill: #2c3e50; -fx-padding: 5 10; -fx-background-radius: 12; -fx-font-size: 13px; -fx-border-color: #dcdde1; -fx-border-radius: 12;");
                    flowServiceTypes.getChildren().add(chip);
                }
            }
        } else {
            Label noService = new Label("No services specified");
            noService.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic; -fx-font-size: 13px;");
            flowServiceTypes.getChildren().add(noService);
        }
        
        lblBookingDate.setText(booking.getBookingDate() + " " + booking.getBookingTime());
        
        String notes = booking.getNotes();
        lblNotes.setText(notes != null && !notes.trim().isEmpty() ? notes : "No notes provided");
        
        setupStatusBadge(booking.getStatus());
        
        lblTotalCost.setText("₱" + df.format(booking.getEstimatedCost()));
        lblDownpayment.setText("₱" + df.format(booking.getDownpayment()));
        lblBalance.setText("₱" + df.format(booking.getBalance()));
        
        loadVehiclePhoto(booking.getCarId());
        loadAssignedStaff(booking.getBookingId());
        loadProductsUsed(booking.getBookingId());
    }
    
    private void setupStatusBadge(String status) {
        if (status == null) status = "Pending";
        lblStatusBadge.setText(status.toUpperCase());
        
        switch (status) {
            case "Scheduled":
                lblStatusBadge.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #2980b9; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 15;");
                break;
            case "In Progress":
                lblStatusBadge.setStyle("-fx-background-color: #fff8e1; -fx-text-fill: #f39c12; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 15;");
                break;
            case "Completed":
                lblStatusBadge.setStyle("-fx-background-color: #e8f8f5; -fx-text-fill: #27ae60; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 15;");
                break;
            case "Cancelled":
                lblStatusBadge.setStyle("-fx-background-color: #fadedb; -fx-text-fill: #e74c3c; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 15;");
                break;
            default:
                lblStatusBadge.setStyle("-fx-background-color: #f2f3f4; -fx-text-fill: #7f8c8d; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 15;");
        }
    }
    
    private void loadAssignedStaff(int bookingId) {
        ObservableList<String> staff = FXCollections.observableArrayList();
        
        // 1. Process staff directly assigned as a comma-separated string on the booking
        String assignedStr = booking.getAssignedTechnician();
        if (assignedStr != null && !assignedStr.trim().isEmpty()) {
            String[] names = assignedStr.split(",");
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement("SELECT position as role FROM employee WHERE employee_name = ? COLLATE NOCASE")) {
                
                for (String name : names) {
                    pstmt.setString(1, name.trim());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            String role = rs.getString("role");
                            if (role != null && !role.isEmpty()) {
                                staff.add(name.trim() + " - " + role);
                            } else {
                                staff.add(name.trim() + " - No position assigned");
                            }
                        } else {
                            staff.add(name.trim() + " - No position assigned");
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error loading employee roles: " + e.getMessage());
            }
        }
        
        // 2. Fetch specific mappings from booking_technicians if available
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT technician_name, role FROM booking_technicians WHERE booking_id = ?")) {
            pstmt.setInt(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String role = rs.getString("role");
                    if (role == null || role.isEmpty()) role = "No position assigned";
                    String techName = rs.getString("technician_name");
                    
                    String entryStr = techName + " - " + role;
                    
                    // Remove fallback versions to prevent duplicates
                    staff.removeIf(s -> s.startsWith(techName + " -"));
                    if (!staff.contains(entryStr)) {
                        staff.add(entryStr);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading specific task staff: " + e.getMessage());
        }
        
        if (staff.isEmpty()) {
            listStaff.setItems(FXCollections.observableArrayList("No staff assigned"));
            listStaff.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
        } else {
            listStaff.setItems(staff);
            listStaff.setStyle("-fx-text-fill: #34495e;");
        }
    }
    
    private void loadProductsUsed(int bookingId) {
        ObservableList<String> products = FXCollections.observableArrayList();
        
        // Products are inherently linked via booking_products table
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT product_name, quantity, total_price FROM booking_products WHERE booking_id = ?")) {
            pstmt.setInt(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int quantity = rs.getInt("quantity");
                    double price = rs.getDouble("total_price");
                    products.add(quantity + "x " + rs.getString("product_name") + " - ₱" + df.format(price));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading products: " + e.getMessage());
        }
        
        if (products.isEmpty()) {
            listProducts.setItems(FXCollections.observableArrayList("No product/parts"));
            listProducts.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
        } else {
            listProducts.setItems(products);
            listProducts.setStyle("-fx-text-fill: #34495e;");
        }
    }

    private void loadVehiclePhoto(int carId) {
        String imagePath = null;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT car_image_path FROM customer_cars WHERE car_id = ?")) {
            pstmt.setInt(1, carId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    imagePath = rs.getString("car_image_path");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading vehicle photo: " + e.getMessage());
        }
        
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                imgVehiclePhoto.setImage(image);
                lblNoImage.setVisible(false);
            } else {
                setPlaceholderImage();
            }
        } else {
            setPlaceholderImage();
        }
    }

    private void setPlaceholderImage() {
        imgVehiclePhoto.setImage(null);
        lblNoImage.setVisible(true);
    }

    @FXML
    private void handleClose() {
        ((Stage) lblCustomerName.getScene().getWindow()).close();
    }
}
