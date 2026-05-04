package Controller;

import Model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;

public class ProductCardController {

    @FXML
    private VBox cardBox;

    @FXML
    private ImageView imgProduct;

    @FXML
    private Label lblProductName;

    @FXML
    private Label lblPrice;

    @FXML
    private Label lblStock;

    private Product product;
    private SalesManagementController parentController;

    public void setData(Product product, SalesManagementController parentController) {
        this.product = product;
        this.parentController = parentController;

        lblProductName.setText(product.getProductName());
        lblPrice.setText(String.format("₱%.2f", product.getPrice()));
        lblStock.setText("Stock: " + product.getStockQuantity());

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

        // Add click listener
        cardBox.setOnMouseClicked(event -> {
            parentController.selectProductFromCard(product);
            
            // Highlight effect
            cardBox.setStyle("-fx-background-color: #f0f8ff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #007bff; -fx-border-width: 2; -fx-effect: dropshadow(gaussian, rgba(0,123,255,0.3), 8, 0, 0, 2); -fx-cursor: hand;");
            
            // Reset other cards (this requires parent controller to manage selection state)
            parentController.resetCardStylesExcept(this);
        });
    }

    public void resetStyle() {
        cardBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-cursor: hand;");
    }
    
    private void setDefaultImage() {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/pics/default_product.png");
            if (is != null) {
                imgProduct.setImage(new Image(is));
            } else {
                java.io.InputStream isAlt = getClass().getResourceAsStream("/View/pics/default_product.png");
                if (isAlt != null) {
                    imgProduct.setImage(new Image(isAlt));
                }
            }
        } catch (Exception e) {
            // Ignore if default image is not available
        }
    }

    public Product getProduct() {
        return product;
    }
}