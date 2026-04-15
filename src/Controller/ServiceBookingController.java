package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import Model.ServiceBooking;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;

public class ServiceBookingController {
    
    // Navigation buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnUserManagement;
    @FXML private Button btnCustomerManagement;
    @FXML private Button btnServiceBooking;
    @FXML private Button btnInventory;
    @FXML private Button btnSales;
    @FXML private Button btnEmployeeManagement;
    @FXML private Button btnTransactions;
    @FXML private Button btnLogout;
    
    @FXML private Button btnAddBooking;
    @FXML private Button btnRefresh;
    @FXML private DatePicker datePicker;
    
    @FXML private TableView<ServiceBooking> tableBookings;
    @FXML private TableColumn<ServiceBooking, Integer> colBookingId;
    @FXML private TableColumn<ServiceBooking, String> colBookingNumber;
    @FXML private TableColumn<ServiceBooking, String> colCustomerName;
    @FXML private TableColumn<ServiceBooking, String> colCarInfo;
    @FXML private TableColumn<ServiceBooking, String> colBookingDate;
    @FXML private TableColumn<ServiceBooking, String> colBookingTime;
    @FXML private TableColumn<ServiceBooking, String> colStatus;
    
    @FXML private Button btnViewDetails;
    @FXML private Button btnEditBooking;
    @FXML private Button btnCancelBooking;
    @FXML private Button btnCompleteBooking;
    
    @FXML private Label lblTotalBookings;
    @FXML private Label lblScheduled;
    @FXML private Label lblInProgress;
    @FXML private Label lblCompleted;
    @FXML private Label lblCancelled;
    @FXML private Label lblBookingCount;

    // View Details labels
    @FXML private Label lblDetailBookingNumber;
    @FXML private Label lblDetailCustomer;
    @FXML private Label lblDetailVehicle;
    @FXML private Label lblDetailService;
    @FXML private Label lblDetailDateTime;
    @FXML private Label lblDetailDuration;
    @FXML private Label lblDetailTechnician;
    @FXML private Label lblDetailCost;
    @FXML private Label lblDetailStatus;
    @FXML private Label lblDetailNotes;
    
    private ObservableList<ServiceBooking> bookingList = FXCollections.observableArrayList();
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    
    private Runnable refreshCallback;
    
    @FXML
    public void initialize() {
        NavigationManager.applyRoleBasedAccess(this);
        // Initialize database tables if they don't exist
        initializeDatabaseTables();
        
        setupTableColumns();
        setupDatePicker();
        loadBookings();
        updateStats();
        setupTableSelectionListener();
        updateButtonStates();
        
        System.out.println("Service Booking UI Initialized");
        
        // Register with SharedDataModel for global refreshes
        refreshCallback = () -> {
            System.out.println("Global refresh triggered: Updating Service Booking data");
            loadBookings();
            updateStats();
        };
        Model.SharedDataModel.getInstance().addRefreshCallback(refreshCallback);
    }
    
    private void initializeDatabaseTables() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Create indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_booking_service_details_booking_id ON booking_service_details(booking_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_booking_products_booking_id ON booking_products(booking_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_booking_products_product_id ON booking_products(product_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_booking_technicians_booking_id ON booking_technicians(booking_id)");
            
        } catch (SQLException e) {
            System.err.println("Note: Error initializing database tables: " + e.getMessage());
        }
    }
    
    private void setupTableColumns() {
        colBookingId.setCellValueFactory(cellData -> cellData.getValue().bookingIdProperty().asObject());
        colBookingNumber.setCellValueFactory(cellData -> cellData.getValue().bookingNumberProperty());
        colCustomerName.setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        colCarInfo.setCellValueFactory(cellData -> cellData.getValue().carDescriptionProperty());
        colBookingDate.setCellValueFactory(cellData -> cellData.getValue().bookingDateProperty());
        colBookingTime.setCellValueFactory(cellData -> cellData.getValue().bookingTimeProperty());
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        
        // Color code status
        colStatus.setCellFactory(col -> new TableCell<ServiceBooking, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                
                // Add listener to update style when selection changes
                if (getTableRow() != null) {
                    getTableRow().selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                        updateStyle(getItem(), empty, isNowSelected);
                    });
                }
                
                updateStyle(status, empty, getTableRow() != null && getTableRow().isSelected());
            }

            private void updateStyle(String status, boolean empty, boolean isSelected) {
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    
                    // Let CSS handle the selected state
                    if (isSelected) {
                        setStyle("");
                        return;
                    }

                    switch (status) {
                        case "Pending":
                            setStyle("-fx-background-color: #e2e3e5; -fx-text-fill: #383d41; -fx-alignment: CENTER;"); // Gray
                            break;
                        case "Confirmed":
                        case "Scheduled":
                            setStyle("-fx-background-color: #cce5ff; -fx-text-fill: #004085; -fx-alignment: CENTER;"); // Blue
                            break;
                        case "In Progress":
                            setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-alignment: CENTER;"); // Yellow
                            break;
                        case "Completed":
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-alignment: CENTER;"); // Green
                            break;
                        case "Cancelled":
                            setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-alignment: CENTER;"); // Red
                            break;
                        default:
                            setStyle("-fx-alignment: CENTER;");
                    }
                }
            }
        });
    }
    
    private void setupDatePicker() {
        datePicker.setValue(LocalDate.now());
        datePicker.setOnAction(e -> loadBookingsForDate());
    }
    
    private void setupTableSelectionListener() {
        tableBookings.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayBookingDetails(newSelection);
            } else {
                clearBookingDetails();
            }
            updateButtonStates();
        });
    }
    
    @FXML
    private void handleViewDetails() {
        ServiceBooking selectedBooking = tableBookings.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            showError("Please select a booking to view details.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/viewbookingdetails.fxml"));
            Parent root = loader.load();
            
            ViewBookingDetailsController controller = loader.getController();
            controller.initData(selectedBooking);
            
            Stage stage = new Stage();
            stage.setTitle("Booking Details");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Error showing booking details: " + e.getMessage());
            e.printStackTrace();
            showError("Could not open booking details view.");
        }
    }
    
    public void loadBookings() {
        bookingList.clear();
        String query = "SELECT sb.*, c.customer_name as real_customer_name, " +
                      "cc.car_brand || ' ' || cc.model || ' (' || cc.plate_number || ')' as car_description " +
                      "FROM service_bookings sb " +
                      "JOIN customers c ON sb.customer_id = c.customer_id " +
                      "JOIN customer_cars cc ON sb.car_id = cc.car_id " +
                      "ORDER BY sb.booking_date DESC, sb.booking_time DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                ServiceBooking booking = createBookingFromResultSet(rs);
                bookingList.add(booking);
            }
            
            tableBookings.setItems(bookingList);
            if (lblBookingCount != null) {
                lblBookingCount.setText("Total: " + bookingList.size() + " bookings");
            }
            updateStats();
            
        } catch (SQLException e) {
            showError("Error loading bookings: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadBookingsForDate() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            loadBookings();
            return;
        }
        
        bookingList.clear();
        String query = "SELECT sb.*, c.customer_name as real_customer_name, " +
                      "cc.car_brand || ' ' || cc.model || ' (' || cc.plate_number || ')' as car_description " +
                      "FROM service_bookings sb " +
                      "JOIN customers c ON sb.customer_id = c.customer_id " +
                      "JOIN customer_cars cc ON sb.car_id = cc.car_id " +
                      "WHERE sb.booking_date = ? " +
                      "ORDER BY sb.booking_time";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, selectedDate.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ServiceBooking booking = createBookingFromResultSet(rs);
                bookingList.add(booking);
            }
            
            tableBookings.setItems(bookingList);
            if (lblBookingCount != null) {
                lblBookingCount.setText("Found: " + bookingList.size() + " bookings on " + selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            }
            updateStats();
            
        } catch (SQLException e) {
            showError("Error loading bookings: " + e.getMessage());
        }
    }
    
    private ServiceBooking createBookingFromResultSet(ResultSet rs) throws SQLException {
        return new ServiceBooking(
            rs.getInt("booking_id"),
            getString(rs, "booking_number"),
            rs.getInt("customer_id"),
            rs.getInt("car_id"),
            getString(rs, "service_type"),
            getString(rs, "service_description"),
            getString(rs, "booking_date"),
            getString(rs, "booking_time"),
            getInt(rs, "estimated_duration"),
            getString(rs, "assigned_technician"),
            getString(rs, "status"),
            getDouble(rs, "estimated_cost"),
            getDouble(rs, "actual_cost"),
            getDouble(rs, "downpayment"),
            getDouble(rs, "balance"),
            getString(rs, "notes"),
            getString(rs, "updated_at"),
            getString(rs, "completed_at"),
            getString(rs, "real_customer_name"),
            getString(rs, "car_description"),
            getInt(rs, "order_id")
        );
    }
    
    private String getString(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value != null ? value : "";
    }
    
    private double getDouble(ResultSet rs, String columnName) throws SQLException {
        try {
            return rs.getDouble(columnName);
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    private int getInt(ResultSet rs, String columnName) throws SQLException {
        try {
            return rs.getInt(columnName);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private void updateStats() {
        int total = bookingList.size();
        int scheduled = 0;
        int inProgress = 0;
        int completed = 0;
        int cancelled = 0;
        
        for (ServiceBooking booking : bookingList) {
            String status = booking.getStatus();
            if (status != null) {
                if (status.equalsIgnoreCase("Scheduled")) {
                    scheduled++;
                } else if (status.equalsIgnoreCase("In Progress")) {
                    inProgress++;
                } else if (status.equalsIgnoreCase("Completed")) {
                    completed++;
                } else if (status.equalsIgnoreCase("Cancelled")) {
                    cancelled++;
                }
            }
        }
        
        if (lblTotalBookings != null) lblTotalBookings.setText(String.valueOf(total));
        if (lblScheduled != null) lblScheduled.setText(String.valueOf(scheduled));
        if (lblInProgress != null) lblInProgress.setText(String.valueOf(inProgress));
        if (lblCompleted != null) lblCompleted.setText(String.valueOf(completed));
        if (lblCancelled != null) lblCancelled.setText(String.valueOf(cancelled));
    }
    
    private void displayBookingDetails(ServiceBooking booking) {
        // Check if detail labels exist in FXML before trying to set them
        if (lblDetailBookingNumber != null) {
            lblDetailBookingNumber.setText(booking.getBookingNumber());
        }
        if (lblDetailCustomer != null) {
            lblDetailCustomer.setText(booking.getCustomerName());
        }
        if (lblDetailVehicle != null) {
            lblDetailVehicle.setText(booking.getCarDescription());
        }
        if (lblDetailService != null) {
            lblDetailService.setText(booking.getServiceType());
        }
        if (lblDetailDateTime != null) {
            lblDetailDateTime.setText(booking.getBookingDate() + " at " + booking.getBookingTime());
        }
        if (lblDetailDuration != null) {
            lblDetailDuration.setText(booking.getEstimatedDuration() + " minutes");
        }
        if (lblDetailTechnician != null) {
            lblDetailTechnician.setText(booking.getAssignedTechnician().isEmpty() ? "Not assigned" : booking.getAssignedTechnician());
        }
        if (lblDetailCost != null) {
            lblDetailCost.setText("₱" + df.format(booking.getEstimatedCost()));
        }
        if (lblDetailStatus != null) {
            lblDetailStatus.setText(booking.getStatus());
            
            // Color code status label
            switch (booking.getStatus()) {
                case "Pending":
                    lblDetailStatus.setStyle("-fx-text-fill: #6c757d; -fx-font-weight: bold;"); // Gray
                    break;
                case "Confirmed":
                case "Scheduled":
                    lblDetailStatus.setStyle("-fx-text-fill: #007bff; -fx-font-weight: bold;"); // Blue
                    break;
                case "In Progress":
                    lblDetailStatus.setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;"); // Yellow
                    break;
                case "Completed":
                    lblDetailStatus.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;"); // Green
                    break;
                case "Cancelled":
                    lblDetailStatus.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // Red
                    break;
                default:
                    lblDetailStatus.setStyle("-fx-text-fill: #333333; -fx-font-weight: bold;");
            }
        }
        if (lblDetailNotes != null) {
            lblDetailNotes.setText(booking.getNotes().isEmpty() ? "No notes" : booking.getNotes());
        }
    }
    
    private void clearBookingDetails() {
        if (lblDetailBookingNumber != null) lblDetailBookingNumber.setText("-");
        if (lblDetailCustomer != null) lblDetailCustomer.setText("-");
        if (lblDetailVehicle != null) lblDetailVehicle.setText("-");
        if (lblDetailService != null) lblDetailService.setText("-");
        if (lblDetailDateTime != null) lblDetailDateTime.setText("-");
        if (lblDetailDuration != null) lblDetailDuration.setText("-");
        if (lblDetailTechnician != null) lblDetailTechnician.setText("-");
        if (lblDetailCost != null) lblDetailCost.setText("-");
        if (lblDetailStatus != null) {
            lblDetailStatus.setText("-");
            lblDetailStatus.setStyle("-fx-text-fill: #333333;");
        }
        if (lblDetailNotes != null) lblDetailNotes.setText("-");
    }
    
    private void updateButtonStates() {
        ServiceBooking selected = tableBookings.getSelectionModel().getSelectedItem();
        boolean hasSelection = selected != null;
        
        if (btnViewDetails != null) btnViewDetails.setDisable(!hasSelection);
        if (btnEditBooking != null) btnEditBooking.setDisable(!hasSelection);
        if (btnCancelBooking != null) btnCancelBooking.setDisable(!hasSelection);
        if (btnCompleteBooking != null) btnCompleteBooking.setDisable(!hasSelection);
        
        if (hasSelection) {
            String status = selected.getStatus();
            boolean isCompletedOrCancelled = "Completed".equals(status) || "Cancelled".equals(status);
            btnEditBooking.setDisable(isCompletedOrCancelled);
            btnCancelBooking.setDisable(isCompletedOrCancelled);
            btnCompleteBooking.setDisable(!"In Progress".equals(status));
        }
    }
    

    
    @FXML
    private void handleAddBooking() {
        showBookingDialog(null);
    }
    
    @FXML
    private void handleEditBooking() {
        ServiceBooking selected = tableBookings.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showBookingDialog(selected);
        }
    }
    
    @FXML
    private void handleCancelBooking() {
        ServiceBooking selected = tableBookings.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Booking");
        alert.setHeaderText("Cancel booking for " + selected.getCustomerName() + "?");
        alert.setContentText("This action will cancel the service appointment.");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            updateBookingStatus(selected.getBookingId(), "Cancelled");
        }
    }
    
    @FXML
    private void handleCompleteBooking() {
        ServiceBooking selected = tableBookings.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Complete Booking");
        alert.setHeaderText("Mark booking as completed?");
        alert.setContentText("Service for: " + selected.getCustomerName() + " - " + selected.getCarDescription());
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            updateBookingStatus(selected.getBookingId(), "Completed");
        }
    }
    
    private void updateBookingStatus(int bookingId, String status) {
        String query = "UPDATE service_bookings SET status = ?, updated_at = CURRENT_TIMESTAMP, user_id = ?";
        
        if ("Completed".equals(status)) {
            query += ", completed_at = CURRENT_TIMESTAMP";
        }
        
        query += " WHERE booking_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, Model.UserSession.getInstance().getUserId() > 0 ? Model.UserSession.getInstance().getUserId() : 1);
            pstmt.setInt(3, bookingId);
            pstmt.executeUpdate();
            
            showInfo("Booking status updated to: " + status);
            loadBookings();
            updateStats();
            
        } catch (SQLException e) {
            showError("Error updating booking status: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleRefresh() {
        loadBookings();
        updateStats();
    }
    
    private void showBookingDialog(ServiceBooking booking) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/servicebookingdialog.fxml"));
            Parent root = loader.load();
            
            ServiceBookingDialogController controller = loader.getController();
            controller.setDialogDetails(this, booking);
            
            Stage stage = new Stage();
            stage.setTitle(booking == null ? "New Service Booking" : "Edit Service Booking");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (Exception e) {
            showError("Error opening booking dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void refreshData() {
        loadBookings();
        updateStats();
    }
    
    @FXML
    private void handleBack() {
        navigateTo("/View/FXML/homepage.fxml", "Tomas Car Accessories - Dashboard");
    }
    
    @FXML
    private void handleUserManagement() {
        navigateTo("/View/FXML/usermanagement.fxml", "Tomas Car Accessories - User Management");
    }
    
    @FXML
    private void handleCustomerManagement() {
        navigateTo("/View/FXML/customermanagement.fxml", "Tomas Car Accessories - Customer Management");
    }
    
    @FXML
    private void handleInventoryManagement() {
        navigateTo("/View/FXML/inventorymanagement.fxml", "Tomas Car Accessories - Inventory Management");
    }
    
    @FXML
    private void handleSalesManagement() {
        navigateTo("/View/FXML/salesmanagement.fxml", "Tomas Car Accessories - Sales Management");
    }
    
    @FXML
    private void handleTransactions() {
        navigateTo("/View/FXML/recenttransactions.fxml", "Tomas Car Accessories - Transaction History");
    }
    
    @FXML
    private void handleEmployeeManagement() {
        navigateTo("/View/FXML/employeemanagement.fxml", "Tomas Car Accessories - Employee Management");
    }
    
    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Confirm Logout");
        alert.setContentText("Are you sure you want to logout?");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            navigateTo("/View/FXML/loginpage.fxml", "Tomas Car Accessories - Login");
        }
    }
    
    private void navigateTo(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage currentStage = (Stage) tableBookings.getScene().getWindow();
            currentStage.getScene().setRoot(root);
            currentStage.setTitle(title);
            currentStage.setMaximized(false);
            currentStage.setWidth(1547);
            currentStage.setHeight(832);
            currentStage.centerOnScreen();
        } catch (Exception e) {
            showError("Error loading page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
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
