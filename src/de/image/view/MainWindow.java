package de.image.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;

import de.image.model.settings.Settings;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class MainWindow extends Application {

	private final static Logger logger = Logger.getLogger(MainWindow.class);
	private final static Settings settings = Settings.getInstance();

	private ImageView imgView;
	private Button btnLeft;
	private Button btnRight;
	private Label lbDirectory;
	private Label lbFilename;

	private String directory;
	private String currentFile;
	private String[] files;
	private int currentIndex = 0;

	private Cursor cursor = Cursor.CROSSHAIR;

	public static void main(String[] args) {
		logger.info("Start Application");
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {

		VBox vboxMain = getVBox(-1, -1, -1);
		BorderPane borderPaneMain = new BorderPane();
		borderPaneMain.setId("borderPaneMain");

		BorderPane borderPaneImage = new BorderPane();
		borderPaneImage.setId("borderPaneImage");

		GridPane gridPane = new GridPane();
		gridPane.setId("gridPane");

		btnLeft = new Button();
		btnLeft.setText("prev");
		btnLeft.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				currentIndex--;
				refreshImageView();
			}

		});
		btnLeft.setPrefWidth(50);

		btnRight = new Button();
		btnRight.setText("next");
		btnRight.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				currentIndex++;
				refreshImageView();
			}

		});
		btnRight.setPrefWidth(50);

		int width = 780;
		int height = 515;

		VBox vboxLeft = getVBox(50, height, 0);
		VBox vboxRight = getVBox(50, height, 0);

		vboxLeft.getChildren().add(btnLeft);
		vboxRight.getChildren().add(btnRight);

		vboxLeft.setAlignment(Pos.CENTER);
		vboxRight.setAlignment(Pos.CENTER);

		HBox hboxTop = getHBox(width - 20, 0, 0);
		HBox hboxBottom = getHBox(width - 20, 25, 50);

		lbDirectory = new Label(directory);
		lbFilename = new Label(currentFile);

		HBox hboxBottomLeft = getHBox(-1, -1, 5);
		hboxBottomLeft.getChildren().add(new Label("folder:"));
		hboxBottomLeft.getChildren().add(lbDirectory);

		HBox hboxBottomRight = getHBox(-1, -1, 5);
		hboxBottomRight.getChildren().add(new Label("file:"));
		hboxBottomRight.getChildren().add(lbFilename);

		hboxBottom.getChildren().add(hboxBottomLeft);
		hboxBottom.getChildren().add(hboxBottomRight);

		hboxBottom.setAlignment(Pos.CENTER);

		imgView = new ImageView();
		imgView.setId("imgView");

		directory = settings.getImageDir();

		refreshDirectory();

		borderPaneImage.setCenter(imgView);
		borderPaneImage.setLeft(vboxLeft);
		borderPaneImage.setRight(vboxRight);
		borderPaneImage.setTop(hboxTop);
		borderPaneImage.setBottom(hboxBottom);

		borderPaneImage.setMaxSize(width, height);

		Button btnCursor = new Button();
		btnCursor.setText("change cursor");
		btnCursor.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				logger.info("cursor change");
				imgView.setCursor(cursor);
				if (cursor.equals(Cursor.DEFAULT)) {
					cursor = Cursor.CROSSHAIR;
				} else {
					cursor = Cursor.DEFAULT;
				}
			}

		});

		gridPane.add(btnCursor, 0, 0);

		borderPaneMain.setCenter(borderPaneImage);
		borderPaneMain.setRight(gridPane);

		MenuBar menuBar = new MenuBar();

		Menu menuFile = new Menu("File");
		Menu menuEdit = new Menu("Edit");

		MenuItem chooseFolder = new MenuItem("open Folder");
		chooseFolder.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				DirectoryChooser chooser = new DirectoryChooser();
				chooser.setInitialDirectory(new File(directory));
				chooser.setTitle(settings.getChooserTitle());
				File selectedDir = chooser.showDialog(primaryStage);

				if (selectedDir == null) {
					logger.warn("no directory chosen");
				} else {
					directory = selectedDir.getAbsolutePath();
					refreshDirectory();
				}

				logger.info("folder " + directory + " chosen");

			}

		});

		menuFile.getItems().add(chooseFolder);

		menuBar.getMenus().addAll(menuFile, menuEdit);

		vboxMain.getChildren().add(menuBar);
		vboxMain.getChildren().add(borderPaneMain);

		Scene scene = new Scene(vboxMain, settings.getWindowWidth(),
				settings.getWindowHeight());
		scene.getStylesheets().add("css/style.css");

		primaryStage.setScene(scene);

		primaryStage.setWidth(settings.getWindowWidth());
		primaryStage.setHeight(settings.getWindowHeight());
		primaryStage.setTitle(settings.getWindowTitle());

		FileInputStream fIn = null;
		try {

			fIn = new FileInputStream(settings.getWindowIcon());
			Image image = new Image(fIn);
			primaryStage.getIcons().add(image);

		} catch (Exception e) {
			if (fIn != null) {
				fIn.close();
			}
		} finally {
			if (fIn != null) {
				fIn.close();
			}
		}

		primaryStage.show();
	}

	private HBox getHBox(int width, int height, int spacing) {
		HBox hbox = new HBox();
		if (width >= 0) {
			hbox.setMinWidth(width);
			hbox.setPrefWidth(width);
			hbox.setMaxWidth(width);
		}
		if (height >= 0) {
			hbox.setMinHeight(height);
			hbox.setPrefHeight(height);
			hbox.setMaxHeight(height);
		}
		if (spacing >= 0) {
			hbox.setSpacing(spacing);
		}
		return hbox;
	}

	private VBox getVBox(int width, int height, int spacing) {
		VBox vbox = new VBox();
		if (width >= 0) {
			vbox.setMinWidth(width);
			vbox.setPrefWidth(width);
			vbox.setMaxWidth(width);
		}
		if (height >= 0) {
			vbox.setMinHeight(height);
			vbox.setPrefHeight(height);
			vbox.setMaxHeight(height);
		}
		if (spacing >= 0) {
			vbox.setSpacing(spacing);
		}
		return vbox;
	}

	private void refreshImageView() {

		FileInputStream fIn = null;

		try {

			fIn = new FileInputStream(directory + File.separator
					+ files[currentIndex]);
			currentFile = files[currentIndex];
			logger.info("changed image to " + directory + File.separator
					+ files[currentIndex]);

		} catch (Exception e) {
			try {
				directory = "-";
				fIn = new FileInputStream(settings.getImagePlaceholder());
				currentFile = settings.getImagePlaceholder();
			} catch (FileNotFoundException e1) {
				logger.error("File: " + settings.getImagePlaceholder()
						+ " not found!", e1);
				currentFile = "-";
			} finally {
				if (fIn != null) {
					try {
						fIn.close();
					} catch (IOException e1) {
						logger.fatal("can not close FileInputStream", e1);
					}
				}
			}
		} finally {

			lbDirectory.setText(directory);
			lbFilename.setText(currentFile);

			Image image = new Image(fIn);
			imgView.setImage(image);

			checkNavBtns();

			if (fIn != null) {
				try {
					fIn.close();
				} catch (IOException e) {
					logger.fatal("can not close FileInputStream", e);
				}
			}
		}
	}

	private void checkNavBtns() {
		if (currentIndex <= 0) {
			btnLeft.setDisable(true);
		} else {
			btnLeft.setDisable(false);
		}
		if (currentIndex >= files.length - 1) {
			btnRight.setDisable(true);
		} else {
			btnRight.setDisable(false);
		}
	}

	private void refreshDirectory() {

		FileInputStream fIn = null;

		try {

			currentIndex = 0;

			File dirFile = new File(directory);

			files = dirFile.list();
			if (files == null) {
				files = new String[0];
			}
			Arrays.sort(files);

			fIn = new FileInputStream(directory + File.separator
					+ files[currentIndex]);
			currentFile = files[currentIndex];

		} catch (Exception e) {
			try {
				directory = "-";
				fIn = new FileInputStream(settings.getImagePlaceholder());
				currentFile = settings.getImagePlaceholder();
			} catch (FileNotFoundException e1) {
				logger.error("File: " + settings.getImagePlaceholder()
						+ " not found!", e1);
				currentFile = "-";
			} finally {
				if (fIn != null) {
					try {
						fIn.close();
					} catch (IOException e1) {
						logger.fatal("can not close FileInputStream", e1);
					}
				}
			}
		} finally {

			lbDirectory.setText(directory);
			lbFilename.setText(currentFile);

			checkNavBtns();

			Image image = new Image(fIn);

			imgView.setImage(image);

			if (fIn != null) {
				try {
					fIn.close();
				} catch (IOException e) {
					logger.fatal("can not close FileInputStream", e);
				}
			}
		}

	}

}
