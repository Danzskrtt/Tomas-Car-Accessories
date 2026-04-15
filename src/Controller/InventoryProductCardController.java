package Controller;

import Model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;

public class InventoryProductCardController {

    @FXML
    private VBox cardBox;

    @FXML
    private ImageView imgProduct;

    @FXML
    private Label lblProductName;

    @FXML
    private Label lblProductCode;

    @FXML
    private Label lblPrice;

    @FXML
    private Label lblStock;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    private Product product;
    private InventoryManagementController parentController;

    public void setData(Product product, InventoryManagementController parentController) {
        this.product = product;
        this.parentController = parentController;

        lblProductName.setText(product.getProductName());
        lblProductCode.setText("Code: " + product.getProductCode());
        lblPrice.setText(String.format("₱%.2f", product.getPrice()));
        
        lblStock.setText(String.valueOf(product.getStockQuantity()));
        
        // Highlight low stock
        if (product.getStockQuantity() <= product.getReorderLevel()) {
            lblStock.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
        } else {
            lblStock.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
        }

        // Load image
        if (product.getImagePath() != null && !product.getImagePath().trim().isEmpty()) {
            File file = new File(product.getImagePath());
            if (file.exists() && file.isFile()) {
                try {
                    imgProduct.setImage(new Image(file.toURI().toString()));
                } catch (Exception e) {
                    setDefaultImage();
                }
            } else {
                setDefaultImage();
            }
        } else {
            setDefaultImage();
        }
    }
    
    private void setDefaultImage() {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/View/pics/default_product.png");
            if (is != null) {
                imgProduct.setImage(new Image(is));
            }
        } catch (Exception e) {
            // Ignore if default image is not available
        }
    }

    @FXML
    private void handleEdit() {
        if (parentController != null) {
            parentController.showProductDialog(product);
        }
    }

    @FXML
    private void handleDelete() {
        if (parentController != null) {
            parentController.deleteProduct(product);
        }
    }
}
