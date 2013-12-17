package de.kicker.tracking.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import org.apache.log4j.Logger;

import de.kicker.tracking.model.AutomaticBallTracking;
import de.kicker.tracking.model.BallTracking;
import de.kicker.tracking.model.Position;
import de.kicker.tracking.model.TrackingImage;
import de.kicker.tracking.model.XMLLayer;
import de.kicker.tracking.model.settings.Settings;

public class BallTrackingApplication extends Application {

	private final static Logger logger = Logger
			.getLogger(BallTrackingApplication.class);
	private final static Settings settings = Settings.getInstance();

	private AnchorPane imgAnchor;
	private ImageView imgView;
	private Button btnPrev;
	private Button btnNext;
	private Label lbDirectory;
	private Label lbFilename;

	private File directory;
	private File currentFile;
	private File[] files;
	private int currentIndex = 0;

	private List<Node> pathNodes;

	private BallTracking ballTracking;

	private Cursor cursor = Cursor.CROSSHAIR;

	public static void main(String[] args) {
		logger.info("Start Application");
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {

		pathNodes = new LinkedList<>();

		VBox vboxMain = FXUtil.getVBox(-1, -1, -1);
		BorderPane borderPaneMain = new BorderPane();
		borderPaneMain.setId("borderPaneMain");

		BorderPane borderPaneImage = new BorderPane();
		borderPaneImage.setId("borderPaneImage");

		GridPane gridPane = new GridPane();
		gridPane.setId("gridPane");

		btnPrev = new Button();
		btnPrev.setText("prev");
		btnPrev.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				currentIndex--;
				refreshImageView();
			}

		});
		btnPrev.setPrefWidth(50);

		btnNext = new Button();
		btnNext.setText("next");
		btnNext.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				currentIndex++;
				refreshImageView();
				if (ballTracking != null) {
					ballTracking.trackNext();
				}
			}

		});
		btnNext.setPrefWidth(50);

		int width = 780;
		int height = 515;

		VBox vboxLeft = FXUtil.getVBox(50, height, 0);
		VBox vboxRight = FXUtil.getVBox(50, height, 0);

		vboxLeft.getChildren().add(btnPrev);
		vboxRight.getChildren().add(btnNext);

		vboxLeft.setAlignment(Pos.CENTER);
		vboxRight.setAlignment(Pos.CENTER);

		HBox hboxTop = FXUtil.getHBox(width - 20, 0, 0);
		HBox hboxBottom = FXUtil.getHBox(width - 20, 25, 50);

		lbDirectory = new Label("-");
		lbFilename = new Label("-");

		HBox hboxBottomLeft = FXUtil.getHBox(-1, -1, 5);
		hboxBottomLeft.getChildren().add(new Label("folder:"));
		hboxBottomLeft.getChildren().add(lbDirectory);

		HBox hboxBottomRight = FXUtil.getHBox(-1, -1, 5);
		hboxBottomRight.getChildren().add(new Label("file:"));
		hboxBottomRight.getChildren().add(lbFilename);

		hboxBottom.getChildren().add(hboxBottomLeft);
		hboxBottom.getChildren().add(hboxBottomRight);

		hboxBottom.setAlignment(Pos.CENTER);

		imgAnchor = new AnchorPane();

		imgView = new ImageView();
		imgView.setId("imgView");

		String dir = settings.getImageDir();
		if ("".equals(dir.trim())) {
			directory = null;
		} else {
			directory = new File(dir);
		}

		refreshDirectory();

		imgAnchor.getChildren().add(imgView);
		imgView.setLayoutX(10);
		imgView.setLayoutY(10);

		imgAnchor.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				double initX = event.getX();
				double initY = event.getY();

				Circle circle = new Circle(initX, initY, 10);
				circle.setFill(null);
				circle.setStroke(Color.RED);
				circle.setStrokeWidth(2);

				imgAnchor.getChildren().add(circle);
				logger.info("x: " + initX + "  /  y: " + initY);

			}

		});

		borderPaneImage.setCenter(imgAnchor);
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

		Button btnStartTracking = new Button();
		btnStartTracking.setText("Start Tracking");
		btnStartTracking.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				ballTracking = new AutomaticBallTracking(files, directory
						.getParent(), currentIndex);
				while (!ballTracking.endOfFiles()) {
					ballTracking.trackNext();
					if (currentIndex < files.length - 1) {
						currentIndex++;
					}
				}
				refreshImageView();
				logger.info("Start Tracking");
			}

		});

		gridPane.add(btnStartTracking, 0, 1);

		borderPaneMain.setCenter(borderPaneImage);
		borderPaneMain.setRight(gridPane);

		MenuBar menuBar = new MenuBar();

		Menu menuFile = new Menu("File");
		Menu menuEdit = new Menu("Edit");

		MenuItem chooseFile = new MenuItem("Open Image");
		chooseFile.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				String dir = directory == null ? "" : directory
						.getAbsolutePath();
				File preDir = new File(dir);

				FileChooser chooser = new FileChooser();

				if (preDir != null && preDir.exists() && preDir.isDirectory()) {
					chooser.setInitialDirectory(preDir);
				}

				chooser.setTitle(settings.getFileChooserTitle());
				ExtensionFilter filter = new ExtensionFilter("image", "*.png",
						"*.jpg");
				chooser.getExtensionFilters().add(filter);
				File selectedFile = chooser.showOpenDialog(primaryStage);

				if (selectedFile == null) {
					logger.warn("no file chosen");
				} else {
					directory = selectedFile.getParentFile();
					refreshDirectory();
					currentFile = selectedFile;
					for (int i = 0; i < files.length; i++) {
						if (files[i].equals(selectedFile)) {
							currentIndex = i;
							break;
						}
					}
					refreshImageView();
				}

				logger.info("file " + selectedFile + " chosen");

			}

		});

		MenuItem chooseFolder = new MenuItem("Open Folder");
		chooseFolder.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				String dir = directory == null ? "" : directory
						.getAbsolutePath();
				File preDir = new File(dir);

				DirectoryChooser chooser = new DirectoryChooser();

				if (preDir != null && preDir.exists() && preDir.isDirectory()) {
					chooser.setInitialDirectory(preDir);
				}

				chooser.setTitle(settings.getDirChooserTitle());
				File selectedDir = chooser.showDialog(primaryStage);

				if (selectedDir == null) {
					logger.warn("no directory chosen");
				} else {
					directory = selectedDir;
					refreshDirectory();
				}

				logger.info("folder " + directory + " chosen");

			}

		});

		MenuItem export2File = new MenuItem("Export");
		export2File.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				if (ballTracking != null && ballTracking.isTracked()) {

					String dir = directory == null ? "" : directory
							.getAbsolutePath();
					File preDir = new File(dir);

					FileChooser chooser = new FileChooser();

					if (preDir != null && preDir.exists()
							&& preDir.isDirectory()) {
						chooser.setInitialDirectory(preDir);
					}

					chooser.setTitle("Save File");
					ExtensionFilter xmlFilter = new ExtensionFilter("xml",
							"*.xml");
					ExtensionFilter allFilter = new ExtensionFilter("all",
							"*.*");
					chooser.getExtensionFilters().addAll(xmlFilter, allFilter);
					File selectedFile = chooser.showSaveDialog(primaryStage);

					if (selectedFile == null) {
						logger.warn("no file chosen");
					} else {
						XMLLayer.export2XML(ballTracking,
								selectedFile.getAbsolutePath());
					}

					logger.info("file " + selectedFile + " chosen");

				}

			}

		});

		menuFile.getItems().addAll(chooseFile, chooseFolder,
				new SeparatorMenuItem(), export2File);

		final CheckMenuItem showPath = new CheckMenuItem("Show Path");
		showPath.setSelected(false);
		showPath.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				if (ballTracking != null && showPath.isSelected()) {

					Collection<TrackingImage> images = ballTracking
							.getAllTrackedImages();
					for (TrackingImage image : images) {

						Position pos = image.getBall().getPosition();
						int initX = pos.getX();
						int initY = pos.getY();

						Circle circle = new Circle(
								initX + imgView.getLayoutX(), initY
										+ imgView.getLayoutY(), 2);
						circle.setFill(null);
						circle.setStroke(Color.RED);
						circle.setStrokeWidth(2);

						imgAnchor.getChildren().add(circle);
						pathNodes.add(circle);

					}

					logger.info("show path true");
				} else if (!showPath.isSelected()) {
					for (Node node : pathNodes) {
						imgAnchor.getChildren().remove(node);
					}
					pathNodes = new LinkedList<>();
					logger.info("show path false");
				} else {
					showPath.setSelected(false);
				}

			}

		});

		menuEdit.getItems().add(showPath);

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

	private void refreshImageView() {

		FileInputStream fIn = null;

		try {

			fIn = new FileInputStream(files[currentIndex]);
			currentFile = files[currentIndex];
			logger.info("changed image to " + directory + File.separator
					+ files[currentIndex]);

		} catch (Exception e) {
			try {
				directory = null;
				currentFile = new File(settings.getImagePlaceholder());
				fIn = new FileInputStream(currentFile);
			} catch (FileNotFoundException e1) {
				logger.error("File: " + settings.getImagePlaceholder()
						+ " not found!", e1);
				currentFile = null;
			}
		} finally {

			String labelText = directory == null ? "-" : directory
					.getAbsolutePath();
			lbDirectory.setText(labelText);
			labelText = currentFile == null
					|| settings.getImagePlaceholder().endsWith(
							currentFile.getName()) ? "-" : currentFile
					.getName();
			lbFilename.setText(labelText);

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
			btnPrev.setDisable(true);
		} else {
			btnPrev.setDisable(false);
		}
		if (currentIndex >= files.length - 1) {
			btnNext.setDisable(true);
		} else {
			btnNext.setDisable(false);
		}
	}

	private void refreshDirectory() {

		FileInputStream fIn = null;

		try {

			currentIndex = 0;

			String dirString = directory == null ? "" : directory
					.getAbsolutePath();
			File dirFile = new File(dirString);

			files = dirFile.listFiles();
			if (files == null) {
				files = new File[0];
			}
			Arrays.sort(files);

			fIn = new FileInputStream(files[currentIndex]);
			currentFile = files[currentIndex];

		} catch (Exception e) {
			try {
				directory = null;
				currentFile = new File(settings.getImagePlaceholder());
				fIn = new FileInputStream(currentFile);
			} catch (FileNotFoundException e1) {
				logger.error("File: " + settings.getImagePlaceholder()
						+ " not found!", e1);
				currentFile = null;
			}
		} finally {

			String labelText = directory == null ? "-" : directory
					.getAbsolutePath();
			lbDirectory.setText(labelText);
			labelText = currentFile == null
					|| settings.getImagePlaceholder().endsWith(
							currentFile.getName()) ? "-" : currentFile
					.getName();
			lbFilename.setText(labelText);

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
