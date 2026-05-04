package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import Model.UserSession;

/**
 * Handles consistent FXML page navigation across all controllers.
 * - Enforces identical window sizes
 * - Keeps Stage titles uniform
 * - Abstracts the root swapping boilerplate
 */
public class NavigationManager {

    private static final List<String> MANAGER_ALLOWED = Arrays.asList(
        "employeemanagement.fxml", "customermanagement.fxml", "servicebooking.fxml", 
        "inventorymanagement.fxml", "salesmanagement.fxml", "recenttransactions.fxml"
    );

    private static final List<String> STAFF_ALLOWED = Arrays.asList(
        "salesmanagement.fxml", "recenttransactions.fxml", 
        "inventorymanagement.fxml", "servicebooking.fxml"
    );

    public static boolean hasAccess(String fxmlPath) {
        String role = UserSession.getInstance().getUserRole();
        if (role == null) role = "Staff";
        
        String path = fxmlPath.toLowerCase();
        
        if (role.equalsIgnoreCase("Admin")) {
            return true;
        }
        
        if (path.contains("login")) return true;
        
        String filename = path.substring(path.lastIndexOf('/') + 1);
        
        if (role.equalsIgnoreCase("Manager")) {
            return MANAGER_ALLOWED.contains(filename);
        }
        
        if (role.equalsIgnoreCase("Staff")) {
            return STAFF_ALLOWED.contains(filename);
        }
        
        return false;
    }

    public static void applyRoleBasedAccess(Object controller) {
        String role = UserSession.getInstance().getUserRole();
        if (role == null) role = "Staff";
        
        if (role.equalsIgnoreCase("Admin")) return;
        
        List<String> allowed = role.equalsIgnoreCase("Manager") ? MANAGER_ALLOWED : STAFF_ALLOWED;
        
        // Hide permissions based on allowed models
        hideButton(controller, "btnDashboard", false); // Dash removed for both based on spec
        hideButton(controller, "btnEmployeeManagement", allowed.contains("employeemanagement.fxml"));
        hideButton(controller, "btnCustomerManagement", allowed.contains("customermanagement.fxml"));
        hideButton(controller, "btnUserManagement", false);
        hideButton(controller, "btnUserManagementQuick", false);
        hideButton(controller, "btnServiceBooking", allowed.contains("servicebooking.fxml"));
        hideButton(controller, "btnInventory", allowed.contains("inventorymanagement.fxml"));
        hideButton(controller, "btnSales", allowed.contains("salesmanagement.fxml"));
        hideButton(controller, "btnTransactions", allowed.contains("recenttransactions.fxml"));
        hideButton(controller, "btnReports", false);
    }
    
    private static void hideButton(Object controller, String fieldName, boolean allowed) {
        if (allowed) return;
        try {
            java.lang.reflect.Field field = controller.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object obj = field.get(controller);
            if (obj != null && obj instanceof javafx.scene.Node) {
                javafx.scene.Node node = (javafx.scene.Node) obj;
                node.setVisible(false);
                node.setManaged(false);
            }
        } catch (NoSuchFieldException e) {
            // Ignore if controller doesn't have this field
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    /**
     * Navigates to a generic FXML page, given a Node (like a clicked Button or TableView)
     * to resolve the current Stage.
     */
    public static void navigateTo(Node sourceNode, String fxmlPath, String title) {
        if (!hasAccess(fxmlPath)) {
            showError("Access Denied", "You do not have permission to access this page.");
            return;
        }

        try {
            Parent root = FXMLLoader.load(NavigationManager.class.getResource(fxmlPath));
            Stage currentStage = (Stage) sourceNode.getScene().getWindow();
            
            // Swap the root of the existing scene instead of building a whole new scene container
            currentStage.getScene().setRoot(root);
            currentStage.setTitle(title);
            currentStage.setMaximized(false);
            currentStage.setWidth(1547);
            currentStage.setHeight(832);
            currentStage.centerOnScreen();
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not load the page: " + fxmlPath + "\n\nError: " + e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            showError("Navigation Error", "FXML file not found: " + fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation Error", "An unexpected error occurred while navigating.");
        }
    }

    /**
     * Navigates to a generic FXML page natively grabbing the Node from an ActionEvent.
     */
    public static void navigateTo(ActionEvent event, String fxmlPath, String title) {
        Node sourceNode = (Node) event.getSource();
        navigateTo(sourceNode, fxmlPath, title);
    }
    
    /**
     * Reusable fallback method for displaying basic errors safely.
     */
    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
