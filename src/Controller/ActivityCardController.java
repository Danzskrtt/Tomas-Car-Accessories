package Controller;

import Model.Activity;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ActivityCardController {

    @FXML
    private Label activityInitial;

    @FXML
    private Label activityDescription;

    @FXML
    private Label activityTimestamp;

    public void setActivity(Activity activity) {
        activityDescription.setText(activity.getDescription());
        activityTimestamp.setText(activity.getTimestamp().toString());
        // Set an icon emoji based on the activity type
        String description = activity.getDescription().toLowerCase();
        if (description.contains("user") || description.contains("customer")) {
            activityInitial.setText("👤");
        } else if (description.contains("sale") || description.contains("order")) {
            activityInitial.setText("💰");
        } else if (description.contains("inventory") || description.contains("product")) {
            activityInitial.setText("📦");
        } else if (description.contains("login") || description.contains("logout")) {
            activityInitial.setText("🔐");
        } else {
            activityInitial.setText("📝");
        }
    }
}
