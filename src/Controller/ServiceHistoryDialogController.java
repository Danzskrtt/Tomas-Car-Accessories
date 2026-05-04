package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import Model.Customer;
import Model.ServiceBooking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ServiceHistoryDialogController {

    @FXML private Label lblTitle;
    @FXML private TableView<ServiceBooking> tblBookings;
    @FXML private TableColumn<ServiceBooking, String> colBookingId;
    @FXML private TableColumn<ServiceBooking, String> colServiceType;
    @FXML private TableColumn<ServiceBooking, String> colBookingDate;
    @FXML private TableColumn<ServiceBooking, String> colCar;
    @FXML private TableColumn<ServiceBooking, Double> colTotalCost;
    @FXML private TableColumn<ServiceBooking, String> colStatus;

    private Customer customer;
    private final ObservableList<ServiceBooking> bookings = FXCollections.observableArrayList();
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";

    @FXML
    public void initialize() {
        colBookingId.setCellValueFactory(new PropertyValueFactory<>("bookingNumber"));
        colCar.setCellValueFactory(new PropertyValueFactory<>("carDescription"));
        colServiceType.setCellValueFactory(new PropertyValueFactory<>("serviceType"));
        colBookingDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        colTotalCost.setCellValueFactory(new PropertyValueFactory<>("estimatedCost"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            lblTitle.setText("Service History - " + customer.getCustomerName());
            loadServiceHistory();
        }
    }

    private void loadServiceHistory() {
        bookings.clear();
        String query = "SELECT sb.*, cc.car_brand, cc.model, cc.plate_number " +
                       "FROM service_bookings sb " +
                       "LEFT JOIN customer_cars cc ON sb.car_id = cc.car_id " +
                       "WHERE sb.customer_id = ? ORDER BY sb.booking_date DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, customer.getCustomerId());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ServiceBooking booking = new ServiceBooking();
                booking.setBookingId(rs.getInt("booking_id"));
                booking.setBookingNumber(rs.getString("booking_number"));
                
                String brand = rs.getString("car_brand");
                String model = rs.getString("model");
                String plate = rs.getString("plate_number");
                String carDesc = "";
                if (brand != null && model != null) {
                    carDesc = brand + " " + model;
                    if (plate != null && !plate.isEmpty()) {
                        carDesc += " (" + plate + ")";
                    }
                } else {
                    carDesc = "Unknown Car";
                }
                booking.setCarDescription(carDesc);
                
                booking.setServiceType(rs.getString("service_type"));
                booking.setBookingDate(rs.getString("booking_date"));
                booking.setEstimatedCost(rs.getDouble("estimated_cost"));
                booking.setStatus(rs.getString("status"));
                // other fields can be set as well if needed
                
                bookings.add(booking);
            }
            tblBookings.setItems(bookings);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) tblBookings.getScene().getWindow();
        stage.close();
    }
}