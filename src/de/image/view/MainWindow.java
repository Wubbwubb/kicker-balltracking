package de.image.view;

import java.io.IOException;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.image.model.settings.Settings;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainWindow extends Application {

	private final static Logger logger = Logger.getLogger(MainWindow.class);

	public static void main(String[] args) {
		Layout layout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
		Appender consoleAppender = new ConsoleAppender(layout);
		logger.addAppender(consoleAppender);

		if (Settings.LOG_TO_FILE) {

			Appender fileAppender = null;

			try {
				fileAppender = new FileAppender(layout, Settings.PATH_LOG
						+ Settings.FILE_LOG);
			} catch (IOException e) {
				logger.error("Fehler beim ertsellen der log Datei");
			}

			logger.removeAppender(consoleAppender);
			logger.addAppender(fileAppender);

		}

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
			}
		});

		StackPane root = new StackPane();
		root.getChildren().add(btn);

		primaryStage.setScene(new Scene(root, 300, 250));

		primaryStage.setWidth(Settings.WINDOW_WIDTH);
		primaryStage.setHeight(Settings.WINDOW_HEIGHT);
		primaryStage.setTitle(Settings.WINDOW_TITLE);

		primaryStage.show();
	}

}
