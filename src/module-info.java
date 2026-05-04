module Tomas_Car_Accessories {
	requires transitive javafx.graphics;
	requires javafx.controls;
	requires javafx.fxml;
	requires java.sql;
	requires org.slf4j; // Add comment to trigger Eclipse auto-build
	requires javafx.base;
	
	opens Controller to javafx.fxml;
	opens Model to javafx.base, javafx.fxml;
	
	exports Model;
	exports Controller;
}