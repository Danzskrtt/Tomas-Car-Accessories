package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import Model.ServiceFee;

import java.sql.*;

public class ServiceFeeMaintenanceController {

    @FXML private TableView<ServiceFee> tableServiceFees;
    @FXML private TableColumn<ServiceFee, Integer> colId;
    @FXML private TableColumn<ServiceFee, String> colServiceName;
    @FXML private TableColumn<ServiceFee, String> colCategory;
    @FXML private TableColumn<ServiceFee, Double> colBaseFee;
    @FXML private TableColumn<ServiceFee, String> colStatus;

    @FXML private TextField txtServiceName;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private TextField txtBaseFee;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cmbStatus;

    @FXML private Button btnSave;
    @FXML private Button btnClear;
    @FXML private Button btnDelete;

    private ObservableList<ServiceFee> serviceList = FXCollections.observableArrayList();
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    private ServiceFee selectedService = null;

    @FXML
    public void initialize() {
        setupTable();
        loadCategories();
        loadStatuses();
        loadServiceFees();
        setupSelectionListener();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
        colServiceName.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colBaseFee.setCellValueFactory(new PropertyValueFactory<>("baseFee"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        tableServiceFees.setItems(serviceList);
    }

    private void loadCategories() {
        cmbCategory.setItems(FXCollections.observableArrayList(
            "General", "Oil Change", "Tires", "Brakes", "Engine", "Detailing", "Electrical"
        ));
    }

    private void loadStatuses() {
        cmbStatus.setItems(FXCollections.observableArrayList("Active", "Inactive"));
        cmbStatus.setValue("Active");
    }

    private void setupSelectionListener() {
        tableServiceFees.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedService = newSelection;
                txtServiceName.setText(selectedService.getServiceName());
                cmbCategory.setValue(selectedService.getCategory());
                txtBaseFee.setText(String.valueOf(selectedService.getBaseFee()));
                txtDescription.setText(selectedService.getDescription());
                cmbStatus.setValue(selectedService.getStatus());
                
                btnDelete.setDisable(false);
            } else {
                handleClear();
            }
        });
    }

    private void loadServiceFees() {
        serviceList.clear();
        String query = "SELECT * FROM service_fees ORDER BY category, service_name";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
             
            while (rs.next()) {
                ServiceFee sf = new ServiceFee(
                    rs.getInt("service_id"),
                    rs.getString("service_name"),
                    rs.getString("description"),
                    rs.getDouble("base_fee"),
                    rs.getInt("estimated_duration"),
                    rs.getString("category"),
                    rs.getString("status"),
                    rs.getString("updated_at"),
                    rs.getString("updated_by")
                );
                serviceList.add(sf);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load service fees: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSave() {
        if (txtServiceName.getText() == null || txtServiceName.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Service Name is required.", Alert.AlertType.WARNING);
            return;
        }

        double fee = 0;
        int duration = 60; // Keep default data behavior for db constraints
        try {
            if (!txtBaseFee.getText().isEmpty()) fee = Double.parseDouble(txtBaseFee.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Fee must be numeric.", Alert.AlertType.WARNING);
            return;
        }

        String username = Model.UserSession.getInstance().getUsername();
        if(username == null) username = "Admin";
        
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (selectedService == null) {
                // INSERT
                String sql = "INSERT INTO service_fees (service_name, category, description, base_fee, estimated_duration, status, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, txtServiceName.getText().trim());
                    pstmt.setString(2, cmbCategory.getValue() != null ? cmbCategory.getValue() : "General");
                    pstmt.setString(3, txtDescription.getText());
                    pstmt.setDouble(4, fee);
                    pstmt.setInt(5, duration);
                    pstmt.setString(6, cmbStatus.getValue());
                    pstmt.setString(7, username);
                    pstmt.executeUpdate();
                }
            } else {
                // UPDATE
                String sql = "UPDATE service_fees SET service_name=?, category=?, description=?, base_fee=?, estimated_duration=?, status=?, updated_at=CURRENT_TIMESTAMP, updated_by=? WHERE service_id=?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, txtServiceName.getText().trim());
                    pstmt.setString(2, cmbCategory.getValue() != null ? cmbCategory.getValue() : "General");
                    pstmt.setString(3, txtDescription.getText());
                    pstmt.setDouble(4, fee);
                    pstmt.setInt(5, duration);
                    pstmt.setString(6, cmbStatus.getValue());
                    pstmt.setString(7, username);
                    pstmt.setInt(8, selectedService.getServiceId());
                    pstmt.executeUpdate();
                }
            }
            handleClear();
            loadServiceFees();
        } catch (SQLException e) {
            showAlert("Error", "Failed to save service fee: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedService == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Service");
        confirm.setHeaderText("Are you sure you want to delete this service?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "DELETE FROM service_fees WHERE service_id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedService.getServiceId());
                pstmt.executeUpdate();
                handleClear();
                loadServiceFees();
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete service fee: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleClear() {
        selectedService = null;
        tableServiceFees.getSelectionModel().clearSelection();
        txtServiceName.clear();
        cmbCategory.setValue(null);
        txtBaseFee.clear();
        txtDescription.clear();
        cmbStatus.setValue("Active");
        btnDelete.setDisable(true);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}