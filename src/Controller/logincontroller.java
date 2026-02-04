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
    
    private loginmodel model = new loginmodel();
    
    
    @FXML
    public void initialize() {
        errorLabel.setText("");
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
            UserSession.getInstance().setUserRole(model.getUserRole());
            UserSession.getInstance().setUsername(username);
            
            System.out.println("User logged in with role: " + model.getUserRole());
            
            // Login successful - navigate to homepage
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/homepage.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) loginButton.getScene().getWindow();
                
                Scene scene = new Scene(root);
                stage.setTitle("Tomas Car Accessories - Home");
                stage.setScene(scene);
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

