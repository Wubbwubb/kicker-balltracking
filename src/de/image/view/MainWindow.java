package de.image.view;

import org.apache.log4j.Logger;

import de.image.model.settings.Settings;
import de.image.test.ImageTest;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainWindow extends Application {

	private final static Logger logger = Logger.getLogger(MainWindow.class);
	private final static Settings settings = Settings.getInstance();

	public static void main(String[] args) {
		logger.info("Start Application");
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Hello World");
		Button btn = new Button();
		btn.setText("Say \"Hello World\"");
		btn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				logger.info("print \"Hello World!\"");
				ImageTest.test();
			}
		});

		StackPane root = new StackPane();
		root.getChildren().add(btn);

		primaryStage.setScene(new Scene(root, settings.getWindowWidth(),
				settings.getWindowHeight()));

		primaryStage.setWidth(settings.getWindowWidth());
		primaryStage.setHeight(settings.getWindowHeight());
		primaryStage.setTitle(settings.getWindowTitle());

		primaryStage.show();
	}

}
