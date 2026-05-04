package Controller;

import Model.StockArrival;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class StockArrivalsDialogController {

    @FXML private TextField txtSearchProduct;
    @FXML private ListView<ProductItem> listProductSuggestions;
    @FXML private DatePicker dpArrivalDate;
    @FXML private TextField txtArrivedQty;
    @FXML private TextField txtDefectiveQty;
    @FXML private TextField txtRefNumber;

    @FXML private TableView<StockArrival> tvArrivals;
    @FXML private TableColumn<StockArrival, String> colProduct;
    @FXML private TableColumn<StockArrival, String> colDate;
    @FXML private TableColumn<StockArrival, Integer> colArrived;
    @FXML private TableColumn<StockArrival, Integer> colDefective;
    @FXML private TableColumn<StockArrival, Integer> colGood;
    @FXML private TableColumn<StockArrival, String> colStatus;
    @FXML private TableColumn<StockArrival, String> colRefNumber;
    @FXML private TableColumn<StockArrival, Void> colActions;

    @FXML private Button btnAddArrival;
    @FXML private Button btnAddNewProduct;
    @FXML private Button btnClose;

    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    private ObservableList<StockArrival> arrivalsList = FXCollections.observableArrayList();
    private ObservableList<ProductItem> productsList = FXCollections.observableArrayList();
    private ProductItem selectedProduct = null;

    private static class ProductItem {
        int id;
        String name;

        ProductItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @FXML
    public void initialize() {
        setupDatabase();
        dpArrivalDate.setValue(LocalDate.now());
        setupTableColumns();
        loadProducts();
        setupAutocomplete();
        loadArrivals();
    }

    private void setupDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS stock_arrivals (" +
                     "arrival_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "product_id INTEGER, " +
                     "product_name TEXT, " +
                     "arrival_date TEXT, " +
                     "arrived_qty INTEGER, " +
                     "defective_qty INTEGER, " +
                     "good_qty INTEGER, " +
                     "status TEXT DEFAULT 'Pending', " +
                     "reference_number TEXT" +
                     ")";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            
            // Alter table if the column does not exist
            try {
                stmt.execute("ALTER TABLE stock_arrivals ADD COLUMN reference_number TEXT");
            } catch (SQLException e) {
                // Column might already exist, ignore this exception
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTableColumns() {
        colProduct.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getArrivalDate()));
        colArrived.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getArrivedQty()).asObject());
        colDefective.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getDefectiveQty()).asObject());
        colGood.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getGoodQty()).asObject());
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        colRefNumber.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReferenceNumber()));

        colActions.setCellFactory(param -> new TableCell<StockArrival, Void>() {
            private final Button btnMove = new Button("Move to Stock");
            {
                btnMove.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand;");
                btnMove.setOnAction(e -> {
                    StockArrival arrival = getTableView().getItems().get(getIndex());
                    if ("Pending".equals(arrival.getStatus())) {
                        moveToStock(arrival);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    StockArrival arrival = getTableView().getItems().get(getIndex());
                    if ("Pending".equals(arrival.getStatus())) {
                        btnMove.setDisable(false);
                        btnMove.setText("Move to Stock");
                        setGraphic(btnMove);
                    } else {
                        btnMove.setDisable(true);
                        btnMove.setText("Completed");
                        setGraphic(btnMove);
                    }
                }
            }
        });

        tvArrivals.setItems(arrivalsList);
    }

    private void loadProducts() {
        productsList.clear();
        String sql = "SELECT product_id, product_name FROM products ORDER BY product_name";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                productsList.add(new ProductItem(rs.getInt("product_id"), rs.getString("product_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupAutocomplete() {
        // Set cell factory to wrap text in ListView
        listProductSuggestions.setCellFactory(param -> new ListCell<ProductItem>() {
            @Override
            protected void updateItem(ProductItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(item.name);
                    label.setWrapText(true);
                    label.setPrefWidth(330);
                    setGraphic(label);
                }
            }
        });

        txtSearchProduct.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                listProductSuggestions.setVisible(false);
                listProductSuggestions.setManaged(false);
                selectedProduct = null;
                return;
            }

            ObservableList<ProductItem> filtered = FXCollections.observableArrayList();
            String lowerCaseFilter = newVal.toLowerCase();
            for (ProductItem product : productsList) {
                if (product.name.toLowerCase().contains(lowerCaseFilter)) {
                    filtered.add(product);
                }
            }

            // Show 'No products found' if empty, otherwise show filtered list
            if (filtered.isEmpty()) {
                listProductSuggestions.setItems(FXCollections.observableArrayList(new ProductItem(-1, "No products found")));
                listProductSuggestions.setPrefHeight(30);
                listProductSuggestions.setVisible(true);
                listProductSuggestions.setManaged(true);
            } else if (selectedProduct == null || !selectedProduct.name.equals(newVal)) {
                listProductSuggestions.setItems(filtered);
                listProductSuggestions.setPrefHeight(Math.min(filtered.size() * 32 + 2, 200));
                listProductSuggestions.setVisible(true);
                listProductSuggestions.setManaged(true);
            } else {
                listProductSuggestions.setVisible(false);
                listProductSuggestions.setManaged(false);
            }
        });

        listProductSuggestions.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                if (newSelection.id == -1) {
                    // Ignore clicks on 'No products found'
                    javafx.application.Platform.runLater(() -> listProductSuggestions.getSelectionModel().clearSelection());
                    return;
                }
                selectedProduct = newSelection;
                txtSearchProduct.setText(selectedProduct.name);
                listProductSuggestions.setVisible(false);
                listProductSuggestions.setManaged(false);
            }
        });

        txtSearchProduct.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !listProductSuggestions.isFocused()) {
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> {
                            if (!listProductSuggestions.isFocused()) {
                                listProductSuggestions.setVisible(false);
                                listProductSuggestions.setManaged(false);
                            }
                        });
                    }
                }, 150);
            }
        });
    }

    private void loadArrivals() {
        arrivalsList.clear();
        String sql = "SELECT * FROM stock_arrivals ORDER BY arrival_id DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                arrivalsList.add(new StockArrival(
                        rs.getInt("arrival_id"),
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("arrival_date"),
                        rs.getInt("arrived_qty"),
                        rs.getInt("defective_qty"),
                        rs.getInt("good_qty"),
                        rs.getString("status"),
                        rs.getString("reference_number")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddArrival() {
        if (selectedProduct == null) {
            showAlert("Required", "Please search and select a valid product from the list.", Alert.AlertType.WARNING);
            return;
        }

        LocalDate date = dpArrivalDate.getValue();
        if (date == null) {
            showAlert("Required", "Please select an arrival date.", Alert.AlertType.WARNING);
            return;
        }

        int arrived, defective;
        try {
            arrived = Integer.parseInt(txtArrivedQty.getText());
            defective = txtDefectiveQty.getText().isEmpty() ? 0 : Integer.parseInt(txtDefectiveQty.getText());
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Quantities must be numeric.", Alert.AlertType.WARNING);
            return;
        }

        if (arrived <= 0 || defective < 0 || defective > arrived) {
            showAlert("Invalid Input", "Check arrived and defective quantities.", Alert.AlertType.WARNING);
            return;
        }

        int good = arrived - defective;
        String refNum = txtRefNumber.getText() == null ? "" : txtRefNumber.getText();
        
        String sql = "INSERT INTO stock_arrivals (product_id, product_name, arrival_date, arrived_qty, defective_qty, good_qty, status, reference_number) VALUES (?, ?, ?, ?, ?, ?, 'Pending', ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selectedProduct.id);
            pstmt.setString(2, selectedProduct.name);
            pstmt.setString(3, date.toString());
            pstmt.setInt(4, arrived);
            pstmt.setInt(5, defective);
            pstmt.setInt(6, good);
            pstmt.setString(7, refNum);
            pstmt.executeUpdate();
            
            clearInputs();
            loadArrivals();
            showAlert("Success", "Arrival logged successfully. Pending restock.", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to add arrival.", Alert.AlertType.ERROR);
        }
    }

    private void moveToStock(StockArrival arrival) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Move to Stock");
        confirm.setHeaderText("Add " + arrival.getGoodQty() + " items of " + arrival.getProductName() + " to active stock?");
        confirm.setContentText("This will mark the arrival as Completed and add the good quantity to the main inventory.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Connection conn = null;
            try {
                conn = DriverManager.getConnection(DB_URL);
                conn.setAutoCommit(false);

                // Update products stock
                String updateStock = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE product_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateStock)) {
                    pstmt.setInt(1, arrival.getGoodQty());
                    pstmt.setInt(2, arrival.getProductId());
                    pstmt.executeUpdate();
                }

                // Add to stock_transactions for history
                String insertTrans = "INSERT INTO stock_transactions (product_id, transaction_type, quantity, reference_number, supplier, transaction_date) VALUES (?, 'IN', ?, ?, 'Arrival System', CURRENT_TIMESTAMP)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertTrans)) {
                    pstmt.setInt(1, arrival.getProductId());
                    pstmt.setInt(2, arrival.getGoodQty());
                    
                    String ref = arrival.getReferenceNumber() != null && !arrival.getReferenceNumber().isEmpty() 
                                    ? arrival.getReferenceNumber() 
                                    : "ARR-" + arrival.getArrivalId();
                                    
                    pstmt.setString(3, ref);
                    pstmt.executeUpdate();
                }

                // Update arrival status
                String updateArrival = "UPDATE stock_arrivals SET status = 'Completed' WHERE arrival_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateArrival)) {
                    pstmt.setInt(1, arrival.getArrivalId());
                    pstmt.executeUpdate();
                }

                conn.commit();
                loadArrivals();
                showAlert("Success", "Stock successfully added to inventory.", Alert.AlertType.INFORMATION);
                
                // If the parent needs refresh, ideally we would use a callback. But a manual refresh will do.
                
            } catch (SQLException e) {
                e.printStackTrace();
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
                }
                showAlert("Error", "Failed to update stock.", Alert.AlertType.ERROR);
            } finally {
                if (conn != null) {
                    try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
                }
            }
        }
    }

    private void clearInputs() {
        selectedProduct = null;
        txtSearchProduct.clear();
        dpArrivalDate.setValue(LocalDate.now());
        txtArrivedQty.clear();
        txtDefectiveQty.clear();
        txtRefNumber.clear();
        listProductSuggestions.setVisible(false);
        listProductSuggestions.setManaged(false);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleAddNewProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/productdialog.fxml"));
            Parent root = loader.load();
            
            ProductDialogController controller = loader.getController();
            controller.setProduct(null);
            // Intentionally skip setting parentController here so it doesn't break since it expects InventoryManagementController
            
            Stage stage = newStage("Add New Product");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Reload the products list to show the newly added product
            loadProducts();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open Add Product dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private Stage newStage(String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        return stage;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
