package Controller;

import Model.loginmodel;
import Model.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class logincontroller {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private ImageView backgroundImage;
    
    private loginmodel model = new loginmodel();
    
    
    @FXML
    public void initialize() {
        errorLabel.setText("");
        
        // Bind background image to fill entire window
        if (backgroundImage != null && backgroundImage.getParent() != null) {
            StackPane parent = (StackPane) backgroundImage.getParent();
            backgroundImage.fitWidthProperty().bind(parent.widthProperty());
            backgroundImage.fitHeightProperty().bind(parent.heightProperty());
        }
    }
  
    @FXML
    private void handleLogin(ActionEvent event) {
        
        errorLabel.setText("");
      
        String username = usernameField.getText().trim();
        String password = passwordField.getText(); 
        
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password");
            return;
        }
        
        if (model.validateLogin(username, password)) {
            // Store user role in session
            UserSession.getInstance().setUserId(model.getUserId());
            UserSession.getInstance().setUserRole(model.getUserRole());
            UserSession.getInstance().setUsername(username);
            
            System.out.println("User logged in with role: " + model.getUserRole());
            
            // Login successful - navigate to homepage or allowed page
            try {
                String targetPage = "/View/FXML/homepage.fxml";
                String title = "Tomas Car Accessories - Home";
                
                String role = model.getUserRole();
                if ("Manager".equalsIgnoreCase(role)) {
                    targetPage = "/View/FXML/employeemanagement.fxml";
                    title = "Tomas Car Accessories - Employee Management";
                } else if ("Staff".equalsIgnoreCase(role)) {
                    targetPage = "/View/FXML/salesmanagement.fxml";
                    title = "Tomas Car Accessories - Sales Management";
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource(targetPage));
                Parent root = loader.load();

                Stage stage = (Stage) loginButton.getScene().getWindow();
                
                Scene scene = new Scene(root, 1547, 832);
                stage.setTitle("Tomas Car Accessories - Home");
                stage.setScene(scene);
                stage.setMaximized(false);
                stage.setWidth(1547);
                stage.setHeight(832);
                stage.centerOnScreen();
                stage.show();
                
            } catch (Exception e) {
                e.printStackTrace();
                errorLabel.setText("Error loading homepage: " + e.getMessage());
            }
        } else {
            
            errorLabel.setText("Invalid username or password. Please try again.");
            passwordField.clear();
        }
    }
}