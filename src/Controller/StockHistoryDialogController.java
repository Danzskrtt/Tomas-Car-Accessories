package Controller;

import Model.InventoryTransaction;
import Model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import java.sql.*;
import java.text.DecimalFormat;

public class StockHistoryDialogController {
    
    @FXML private TableView<InventoryTransaction> tvTransactions;
    @FXML private TableColumn<InventoryTransaction, String> colProductName;
    @FXML private TableColumn<InventoryTransaction, Integer> colQuantity;
    @FXML private TableColumn<InventoryTransaction, String> colReferenceNumber;
    @FXML private TableColumn<InventoryTransaction, String> colSupplier;
    @FXML private TableColumn<InventoryTransaction, String> colTransactionDate;
    @FXML private TableColumn<InventoryTransaction, String> colNotes;
    
    @FXML private ComboBox<String> cmbProductFilter;
    @FXML private ComboBox<String> cmbTransactionType;
    @FXML private TextField txtSearchReference;
    @FXML private Button btnFilter;
    @FXML private Button btnClear;
    @FXML private Button btnClose;
    
    private ObservableList<InventoryTransaction> allTransactions = FXCollections.observableArrayList();
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    
    @FXML
    public void initialize() {
        setupTableColumns();
        loadTransactions();
        setupFilters();
    }
    
    private void setupTableColumns() {
        colProductName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
        
        colQuantity.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        colQuantity.setCellFactory(column -> new TableCell<InventoryTransaction, Integer>() {
            @Override
            protected void updateItem(Integer qty, boolean empty) {
                super.updateItem(qty, empty);
                if (empty || qty == null) {
                    setText(null);
                } else {
                    InventoryTransaction transaction = getTableView().getItems().get(getIndex());
                    if (transaction.getTransactionType().equals("IN")) {
                        setText("+" + qty);
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setText("-" + qty);
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        colReferenceNumber.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReferenceNumber()));
        colSupplier.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSupplier()));
        colTransactionDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransactionDate()));
        colNotes.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNotes()));
    }
    
    private void setupFilters() {
        cmbProductFilter.getItems().add("All Products");
        cmbTransactionType.getItems().addAll("All Types", "IN", "OUT");
        cmbTransactionType.getSelectionModel().select("All Types");
        
        // Load unique product names
        String query = "SELECT DISTINCT p.product_name FROM products p " +
                      "JOIN stock_transactions st ON p.product_id = st.product_id " +
                      "ORDER BY p.product_name";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                cmbProductFilter.getItems().add(rs.getString("product_name"));
            }
        } catch (SQLException e) {
            System.err.println("Error loading products: " + e.getMessage());
        }
        
        cmbProductFilter.getSelectionModel().select("All Products");
    }
    
    private void loadTransactions() {
        allTransactions.clear();
        String query = "SELECT st.transaction_id, st.product_id, p.product_name, " +
                      "st.transaction_type, st.quantity, st.reference_number, " +
                      "st.supplier, st.transaction_date, st.user_id, st.notes " +
                      "FROM stock_transactions st " +
                      "JOIN products p ON st.product_id = p.product_id " +
                      "ORDER BY st.transaction_date DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                InventoryTransaction transaction = new InventoryTransaction(
                    rs.getInt("transaction_id"),
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("transaction_type"),
                    rs.getInt("quantity"),
                    getString(rs, "reference_number"),
                    getString(rs, "supplier"),
                    rs.getString("transaction_date"),
                    rs.getInt("user_id"),
                    getString(rs, "notes")
                );
                allTransactions.add(transaction);
            }
            
            applyFilters();
            
        } catch (SQLException e) {
            showError("Error loading transactions: " + e.getMessage());
        }
    }
    
    private String getString(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value != null ? value : "";
    }
    
    @FXML
    private void handleFilter() {
        applyFilters();
    }
    
    @FXML
    private void handleClear() {
        cmbProductFilter.getSelectionModel().select("All Products");
        cmbTransactionType.getSelectionModel().select("All Types");
        txtSearchReference.clear();
        applyFilters();
    }
    
    private void applyFilters() {
        ObservableList<InventoryTransaction> filtered = FXCollections.observableArrayList();
        
        String selectedProduct = cmbProductFilter.getValue();
        String selectedType = cmbTransactionType.getValue();
        String referenceSearch = txtSearchReference.getText().toLowerCase();
        
        for (InventoryTransaction transaction : allTransactions) {
            boolean matchesProduct = selectedProduct == null || 
                                    selectedProduct.equals("All Products") || 
                                    transaction.getProductName().equals(selectedProduct);
            
            boolean matchesType = selectedType == null || 
                                 selectedType.equals("All Types") || 
                                 transaction.getTransactionType().equals(selectedType);
            
            boolean matchesReference = referenceSearch.isEmpty() || 
                                      transaction.getReferenceNumber().toLowerCase().contains(referenceSearch);
            
            if (matchesProduct && matchesType && matchesReference) {
                filtered.add(transaction);
            }
        }
        
        tvTransactions.setItems(filtered);
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}