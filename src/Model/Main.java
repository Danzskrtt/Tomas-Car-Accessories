package Model;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/View/FXML/loginpage.fxml"));
			Scene scene = new Scene(root, 1547, 832);
			
		
			primaryStage.setTitle("Tomas Car Accessories - Login");
			primaryStage.setScene(scene);
			primaryStage.setMaximized(false);
			primaryStage.setWidth(1547);
			primaryStage.setHeight(832);
			primaryStage.centerOnScreen();
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
