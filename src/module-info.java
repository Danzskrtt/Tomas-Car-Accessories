module Tomas_Car_Accessories {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.sql;
	
	opens Model to javafx.graphics, javafx.fxml, javafx.base;
	opens Controller to javafx.fxml;
}

