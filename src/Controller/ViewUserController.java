package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import Model.User;

public class ViewUserController {
    
    @FXML private Label lblUserIdBadge;
    @FXML private Label lblUsername;
    @FXML private Label lblPassword;
    @FXML private Label lblFirstName;
    @FXML private Label lblLastName;
    @FXML private Label lblEmail;
    @FXML private Label lblPhone;
    @FXML private Label lblRole;
    @FXML private Button btnEdit;
    @FXML private Button btnClose;
    
    private User user;
    private Runnable onUserUpdated;
    
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            lblUserIdBadge.setText("ID: " + user.getUserId());
            lblUsername.setText(user.getUsername());
            lblPassword.setText(user.getPassword()); // Show actual password
            lblFirstName.setText(user.getFirstName());
            lblLastName.setText(user.getLastName());
            lblEmail.setText(user.getEmail());
            lblPhone.setText(user.getPhone() != null ? user.getPhone() : "N/A");
            lblRole.setText(user.getRole());
        }
    }
    
    public void setOnUserUpdated(Runnable callback) {
        this.onUserUpdated = callback;
    }
    
    @FXML
    private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/FXML/edituser.fxml"));
            Parent root = loader.load();
            
            EditUserController controller = loader.getController();
            controller.setUser(user);
            
            Stage editStage = new Stage();
            editStage.setTitle("Edit User");
            editStage.setScene(new Scene(root));
            editStage.initModality(Modality.APPLICATION_MODAL);
            editStage.showAndWait();
            
            // If user was updated, refresh the view and notify parent
            if (controller.isUpdated()) {
                // Reload user data (assuming the user object was updated)
                setUser(user);
                
                // Notify parent to reload data if callback is set
                if (onUserUpdated != null) {
                    onUserUpdated.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error opening edit user dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
