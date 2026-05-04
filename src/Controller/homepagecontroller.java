package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import javafx.event.ActionEvent;
import Model.UserManagementModel;
import Model.UserSession;
import Model.ActivityManager;
import Model.Activity;
import Model.SharedDataModel;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

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
    private Button btnTransactions;
    
    @FXML
    private Button btnEmployeeManagement;
    
    @FXML
    private Button btnReports;
    
    @FXML
    private Button btnLogout;
    
    @FXML
    private Label lblTotalUsers;
    
    @FXML
    private Label lblTotalCustomers;
    
    @FXML
    private Label lblTotalEmployees;
    
    @FXML
    private Label lblTotalProducts;
    
    @FXML
    private Label lblTotalSales;
    
    @FXML
    private LineChart<String, Number> incomeChart;
    
    @FXML
    private VBox activityVBox;
    
    @FXML
    private VBox bookingRemindersVBox;
    
    private UserManagementModel userModel;
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    
    @FXML
    public void initialize() {
        System.out.println("Homepage loaded successfully!");
        
        // Initialize the activity table
        ActivityManager.initializeActivityTable();
        
        // Initialize the user model
        userModel = new UserManagementModel();
        
        loadAllData();
        
        // Apply role-based access control
        applyRoleBasedAccess();

        // Register for refresh events
        SharedDataModel.getInstance().addRefreshCallback(this::loadEmployeeCount);
    }
    
    private void loadAllData() {
        // Load and display all counts and data
        loadUserCount();
        loadCustomerCount();
        loadProductCount();
        loadEmployeeCount();
        loadUpcomingBookings();
        loadIncomeChartData();
        loadRecentActivities();
    }
    
    // Method to apply role-based access control
    private void applyRoleBasedAccess() {
        NavigationManager.applyRoleBasedAccess(this);
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
    
    // Method to load and display the total employee count
    private void loadEmployeeCount() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM employee")) {
            
            if (rs.next()) {
                int totalEmployees = rs.getInt("count");
                lblTotalEmployees.setText(String.valueOf(totalEmployees));
                System.out.println("Total employees loaded: " + totalEmployees);
            }
        } catch (SQLException e) {
            System.err.println("Error loading employee count: " + e.getMessage());
            lblTotalEmployees.setText("0");
        }
    }
    
    // Method to load and display the total product count
    private void loadProductCount() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM products")) {
            
            if (rs.next()) {
                int totalProducts = rs.getInt("count");
                lblTotalProducts.setText(String.valueOf(totalProducts));
                System.out.println("Total products loaded: " + totalProducts);
            }
        } catch (SQLException e) {
            System.err.println("Error loading product count: " + e.getMessage());
            lblTotalProducts.setText("0");
        }
    }
    
    // Method to load and display upcoming booking reminders
    private void loadUpcomingBookings() {
        bookingRemindersVBox.getChildren().clear();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT sb.booking_id, c.customer_name, sb.booking_date, sb.booking_time, " +
                 "cc.car_brand || ' ' || cc.model as vehicle_type, sb.service_type as description " +
                "FROM service_bookings sb " +
                "JOIN customers c ON sb.customer_id = c.customer_id " +
                "LEFT JOIN customer_cars cc ON sb.car_id = cc.car_id " +
                "WHERE sb.booking_date >= DATE('now') " +
                "ORDER BY sb.booking_date ASC, sb.booking_time ASC " +
                "LIMIT 10")) {
            
            int bookingCount = 0;
            while (rs.next()) {
                bookingCount++;
                String bookingId = rs.getString("booking_id");
                String customerName = rs.getString("customer_name");
                String bookingDate = rs.getString("booking_date");
                String bookingTime = rs.getString("booking_time");
                String vehicleType = rs.getString("vehicle_type");
                String description = rs.getString("description");
                
                // Implement null safety for vehicleType
                if (vehicleType == null || vehicleType.trim().isEmpty()) {
                    vehicleType = "No vehicle assigned";
                }
                if (description == null || description.trim().isEmpty()) {
                    description = "No description";
                }
                
                // Create booking reminder card
                VBox bookingCard = createBookingReminderCard(bookingId, customerName, bookingDate, bookingTime, vehicleType, description);
                bookingRemindersVBox.getChildren().add(bookingCard);
            }
            
            if (bookingCount == 0) {
                Label noBookingsLabel = new Label("No upcoming bookings");
                noBookingsLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 13px; -fx-padding: 20;");
                bookingRemindersVBox.getChildren().add(noBookingsLabel);
            }
            
            System.out.println("Upcoming bookings loaded: " + bookingCount);
        } catch (SQLException e) {
            System.err.println("Error loading upcoming bookings: " + e.getMessage());
            Label errorLabel = new Label("Error loading bookings");
            errorLabel.setStyle("-fx-text-fill: #DC143C; -fx-font-size: 13px; -fx-padding: 20;");
            bookingRemindersVBox.getChildren().add(errorLabel);
        }
    }
    
    // Method to create a booking reminder card
    private VBox createBookingReminderCard(String bookingId, String customerName, String bookingDate, 
                                           String bookingTime, String vehicleType, String description) {
        VBox card = new VBox();
        card.setStyle("-fx-border-color: #E8F4F8; -fx-border-width: 1; -fx-border-radius: 6; " +
                     "-fx-background-color: #F0F8FB; -fx-background-radius: 6; -fx-padding: 12;");
        card.setSpacing(6);
        
        // Booking header with customer name and time
        javafx.scene.layout.HBox headerBox = new javafx.scene.layout.HBox();
        headerBox.setSpacing(10);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label customerLabel = new Label("👤 " + customerName);
        customerLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        
        Label timeLabel = new Label(bookingTime);
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-font-style: italic;");
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Label dateLabel = new Label("📅 " + bookingDate);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #DC143C; -fx-font-weight: bold;");
        
        headerBox.getChildren().addAll(customerLabel, spacer, dateLabel);
        card.getChildren().add(headerBox);
        
        // Vehicle and description details
        Label detailsLabel = new Label("🚗 " + vehicleType + " - " + (description != null && !description.isEmpty() ? description : "No description"));
        detailsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555; -fx-wrap-text: true;");
        detailsLabel.setWrapText(true);
        card.getChildren().add(detailsLabel);
        
        // Booking ID in smaller text
        Label idLabel = new Label("ID: " + bookingId);
        idLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999999;");
        card.getChildren().add(idLabel);
        
        return card;
    }
    
    // Method to load data into the income chart
    private void loadIncomeChartData() {
        if (incomeChart != null) {
            incomeChart.getData().clear();
        }
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Income");

        // Use a TreeMap to store data sorted by month
        Map<Integer, Double> monthlyIncome = new TreeMap<>();
        
        String sql = "SELECT order_date, final_amount FROM orders WHERE strftime('%Y', order_date) = strftime('%Y', 'now') " +
                     "UNION ALL " +
                     "SELECT booking_date as order_date, estimated_cost as final_amount FROM service_bookings WHERE status = 'Completed' AND strftime('%Y', booking_date) = strftime('%Y', 'now')";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String dateStr = rs.getString("order_date").substring(0, 10); // Extract yyyy-MM-dd
                double amount = rs.getDouble("final_amount");
                
                // Parse the date to get the month
                LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                int month = date.getMonthValue();
                
                // Aggregate income by month
                monthlyIncome.put(month, monthlyIncome.getOrDefault(month, 0.0) + amount);
            }

            // Populate the series with data for all months of the year
            for (int i = 1; i <= 12; i++) {
                String monthName = java.time.Month.of(i).name();
                monthName = monthName.substring(0,1).toUpperCase() + monthName.substring(1).toLowerCase(); // Capitalize first letter
                double income = monthlyIncome.getOrDefault(i, 0.0);
                series.getData().add(new XYChart.Data<>(monthName.substring(0, 3), income));
            }
            
            incomeChart.getData().add(series);
            System.out.println("Income chart loaded successfully.");

        } catch (SQLException e) {
            System.err.println("Error loading income chart data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Method to load and display recent activities
    private void loadRecentActivities() {
        activityVBox.getChildren().clear(); // Clear existing activities
        ObservableList<Activity> recentActivities = ActivityManager.getRecentActivities(10); // Get last 10 activities

        // Check for out of stock products and add a notification activity at the top
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT product_name FROM products WHERE stock_quantity <= 0")) {
            while (rs.next()) {
                String productName = rs.getString("product_name");
                Activity outOfStockActivity = new Activity(0, "SYSTEM_ALERT", "⚠️ OUT OF STOCK: " + productName + " has 0 stock remaining.", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), "System");
                recentActivities.add(0, outOfStockActivity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (Activity activity : recentActivities) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/ActivityCard.fxml"));
                Parent activityCard = loader.load();
                ActivityCardController controller = loader.getController();
                controller.setActivity(activity);
                activityVBox.getChildren().add(activityCard);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleUserManagement(ActionEvent event) {
        navigateTo("/View/FXML/usermanagement.fxml", "Tomas Car Accessories - User Management", btnUserManagement);
    }
    
    @FXML
    private void handleCustomerManagement(ActionEvent event) {
        navigateTo("/View/FXML/customermanagement.fxml", "Tomas Car Accessories - Customer Management", btnCustomerManagement);
    }
    
    @FXML
    private void handleInventoryManagement(ActionEvent event) {
        navigateTo("/View/FXML/inventorymanagement.fxml", "Tomas Car Accessories - Inventory Management", btnInventory);
    }
    
    @FXML
    private void handleSalesManagement(ActionEvent event) {
        navigateTo("/View/FXML/salesmanagement.fxml", "Tomas Car Accessories - Sales Management", btnSales);
    }
    
    @FXML
    private void handleTransactions(ActionEvent event) {
        navigateTo("/View/FXML/recenttransactions.fxml", "Tomas Car Accessories - Transaction History", btnTransactions);
    }
    
    @FXML
    private void handleServiceBooking(ActionEvent event) {
        navigateTo("/View/FXML/servicebooking.fxml", "Tomas Car Accessories - Service Booking", btnServiceBooking);
    }
    
    @FXML
    private void handleEmployeeManagement(ActionEvent event) {
        navigateTo("/View/FXML/employeemanagement.fxml", "Tomas Car Accessories - Employee Management", btnEmployeeManagement);
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
            currentStage.setMaximized(false);
            currentStage.setWidth(1547);
            currentStage.setHeight(832);
            currentStage.centerOnScreen();
            System.out.println(title + " loaded successfully!");
        } catch (Exception e) {
            System.err.println("Error loading page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void handleResetDashboard(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Dashboard Reset");
        confirm.setHeaderText("Clear all dashboard data?");
        confirm.setContentText("This will permanently delete all recent activities, active bookings, and recent transactions. Are you sure you want to proceed?");
        
        java.util.Optional<ButtonType> result = confirm.showAndWait();
        if (!result.isPresent() || result.get() != ButtonType.OK) {
            return;
        }
        
        System.out.println("Resetting dashboard data and triggering global refresh...");
        
        // 1. Clear Activities
        ActivityManager.clearAllActivities();
        
        // 2. Clear Bookings and Transactions
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Clear Bookings - Keep Completed ones for Service History
            stmt.executeUpdate("DELETE FROM booking_service_details WHERE booking_id IN (SELECT booking_id FROM service_bookings WHERE status != 'Completed')");
            stmt.executeUpdate("DELETE FROM booking_products WHERE booking_id IN (SELECT booking_id FROM service_bookings WHERE status != 'Completed')");
            stmt.executeUpdate("DELETE FROM booking_technicians WHERE booking_id IN (SELECT booking_id FROM service_bookings WHERE status != 'Completed')");
            stmt.executeUpdate("DELETE FROM service_bookings WHERE status != 'Completed'");
            
            // Clear Transactions (Orders)
            stmt.executeUpdate("DELETE FROM orders");
            stmt.executeUpdate("DELETE FROM order_items");
            
            System.out.println("Database tables for activities, bookings, and transactions have been cleared.");
        } catch (SQLException e) {
            System.err.println("Error clearing database tables: " + e.getMessage());
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setContentText("Database error during reset: " + e.getMessage());
            error.showAndWait();
        }
        
        // Reload dashboard views and trigger refresh on other pages
        loadAllData();
        SharedDataModel.getInstance().triggerRefresh();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reset Successful");
        alert.setHeaderText(null);
        alert.setContentText("Dashboard and related views have been successfully reset to zero.");
        alert.showAndWait();
    }
}
