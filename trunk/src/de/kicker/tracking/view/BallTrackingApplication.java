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
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyCombination;
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

import de.kicker.tracking.model.BallShape;
import de.kicker.tracking.model.Position;
import de.kicker.tracking.model.TrackingImage;
import de.kicker.tracking.model.balltracking.TrackingFactory;
import de.kicker.tracking.model.settings.Settings;

public class BallTrackingApplication extends Application {

	private final static Logger logger = Logger.getLogger(BallTrackingApplication.class);
	private final static Settings settings = Settings.getInstance();

	private AnchorPane imgAnchor;
	private ImageView imgView;
	private Button btnPrev;
	private Button btnNext;
	private KeyCombination prevCombination = KeyCombination.keyCombination("F3");
	private KeyCombination nextCombination = KeyCombination.keyCombination("F4");
	private Button btnColor;
	private Label lbDirectory;
	private Label lbFilename;
	private CheckMenuItem showPath;
	private MenuItem initializeTracking;

	private File directory;
	private File currentFile;
	private File[] files;
	private int currentIndex = 0;

	private List<Node> pathNodes;
	private Node manualCircle;

	private TrackingFactory tracking;

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
				if (currentIndex > 0) {
					currentIndex--;
					refreshImageView();
				}
			}

		});
		btnPrev.setPrefWidth(50);

		btnNext = new Button();
		btnNext.setText("next");
		btnNext.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (currentIndex < files.length - 1) {
					currentIndex++;
					refreshImageView();
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
		if (directory != null) {
			tracking = new TrackingFactory(directory.getAbsolutePath(), null);
		}

		imgAnchor.getChildren().add(imgView);
		imgView.setLayoutX(10);
		imgView.setLayoutY(10);

		borderPaneImage.setCenter(imgAnchor);
		borderPaneImage.setLeft(vboxLeft);
		borderPaneImage.setRight(vboxRight);
		borderPaneImage.setTop(hboxTop);
		borderPaneImage.setBottom(hboxBottom);

		borderPaneImage.setMaxSize(width, height);

		btnColor = new Button();
		btnColor.setText("Select Ball Color");
		btnColor.setDisable(true);
		btnColor.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				imgView.setCursor(Cursor.CROSSHAIR);
				imgAnchor.setOnMouseClicked(new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {

						double initX = event.getX();
						double initY = event.getY();

						PixelReader pixelReader = imgView.getImage().getPixelReader();
						Color color = pixelReader.getColor((int) (initX - imgView.getLayoutX()),
								(int) (initY - imgView.getLayoutY()));

						System.out.println(color);

						if (tracking != null) {
							tracking.getBallShape().setColor(color);
						}

						logger.info("x: " + initX + "  /  y: " + initY);

						imgAnchor.setOnMouseClicked(null);

						imgView.setCursor(Cursor.DEFAULT);

					}

				});
			}

		});

		gridPane.add(btnColor, 0, 0);

		borderPaneMain.setCenter(borderPaneImage);
		borderPaneMain.setRight(gridPane);

		MenuBar menuBar = createMenuBar(primaryStage);

		vboxMain.getChildren().add(menuBar);
		vboxMain.getChildren().add(borderPaneMain);

		Scene scene = new Scene(vboxMain, settings.getWindowWidth(), settings.getWindowHeight());
		scene.getStylesheets().add("css/style.css");

		scene.getAccelerators().put(prevCombination, new Runnable() {
			@Override
			public void run() {
				btnPrev.fire();
			}
		});
		scene.getAccelerators().put(nextCombination, new Runnable() {
			@Override
			public void run() {
				btnNext.fire();
			}
		});

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

	private MenuBar createMenuBar(final Stage primaryStage) {

		MenuBar menuBar = new MenuBar();

		Menu menuFile = new Menu("File");
		Menu menuEdit = new Menu("Edit");
		Menu menuTrack = new Menu("Track");

		MenuItem chooseFile = new MenuItem("Open Image");
		chooseFile.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				String dir = directory == null ? "" : directory.getAbsolutePath();
				File preDir = new File(dir);

				FileChooser chooser = new FileChooser();

				if (preDir != null && preDir.exists() && preDir.isDirectory()) {
					chooser.setInitialDirectory(preDir);
				}

				chooser.setTitle(settings.getFileChooserTitle());
				ExtensionFilter filter = new ExtensionFilter("image", "*.png", "*.jpg");
				chooser.getExtensionFilters().add(filter);
				File selectedFile = chooser.showOpenDialog(primaryStage);

				if (selectedFile == null) {
					logger.warn("no file chosen");
				} else {
					logger.info("file " + selectedFile + " chosen");
					if (!selectedFile.getParentFile().equals(directory)) {
						directory = selectedFile.getParentFile();
						refreshDirectory();
						createTracking(directory.getAbsolutePath());
					}
					currentFile = selectedFile;
					for (int i = 0; i < files.length; i++) {
						if (files[i].equals(selectedFile)) {
							currentIndex = i;
							break;
						}
					}
					refreshImageView();
				}

			}

		});

		MenuItem chooseFolder = new MenuItem("Open Folder");
		chooseFolder.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				String dir = directory == null ? "" : directory.getAbsolutePath();
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
					logger.info("folder " + directory + " chosen");
					directory = selectedDir;
					refreshDirectory();
					createTracking(directory.getAbsolutePath());
				}

			}

		});

		MenuItem importFromFile = new MenuItem("Import");
		importFromFile.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				String dir = directory == null ? "" : directory.getAbsolutePath();
				File preDir = new File(dir);

				FileChooser chooser = new FileChooser();

				if (preDir != null && preDir.exists() && preDir.isDirectory()) {
					chooser.setInitialDirectory(preDir);
				}

				chooser.setTitle("Open File");
				ExtensionFilter xmlFilter = new ExtensionFilter("xml", "*.xml");
				chooser.getExtensionFilters().addAll(xmlFilter);
				File selectedFile = chooser.showOpenDialog(primaryStage);

				if (selectedFile == null) {
					logger.warn("no file chosen for import");
				} else {
					logger.info("file " + selectedFile + " chosen for import");
					tracking = TrackingFactory.importFromXML(selectedFile);
					initializeTracking.setDisable(false);
					directory = new File(tracking.getDirectory());
					refreshDirectory();
				}

			}

		});

		MenuItem exportToFile = new MenuItem("Export");
		exportToFile.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				if (tracking != null
						&& (tracking.autoBallTracking.getAllTrackedImages().size() > 0 || tracking.manualBallTracking
								.getAllTrackedImages().size() > 0)) {

					String dir = directory == null ? "" : directory.getAbsolutePath();
					File preDir = new File(dir);

					FileChooser chooser = new FileChooser();

					if (preDir != null && preDir.exists() && preDir.isDirectory()) {
						chooser.setInitialDirectory(preDir);
					}

					chooser.setTitle("Save File");
					ExtensionFilter xmlFilter = new ExtensionFilter("xml", "*.xml");
					ExtensionFilter allFilter = new ExtensionFilter("all", "*.*");
					chooser.getExtensionFilters().addAll(xmlFilter, allFilter);
					File selectedFile = chooser.showSaveDialog(primaryStage);

					if (selectedFile == null) {
						logger.warn("no file chosen for export");
					} else {
						logger.info("file " + selectedFile + " chosen for export");
						tracking.exportToXML(selectedFile);
					}

				}

			}

		});

		menuFile.getItems().addAll(chooseFile, chooseFolder, new SeparatorMenuItem(), importFromFile, exportToFile);

		showPath = new CheckMenuItem("Show Path");
		showPath.setSelected(false);
		showPath.setDisable(true);
		showPath.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (tracking != null && showPath.isSelected()) {
					Collection<TrackingImage> images = tracking.autoBallTracking.getAllTrackedImages();
					for (TrackingImage image : images) {
						addMarker(image, Color.RED, Color.WHITE);
					}

					images = tracking.manualBallTracking.getAllTrackedImages();
					for (TrackingImage image : images) {
						addMarker(image, Color.BLUE, Color.WHITE);
					}

					logger.info("show path true");
				} else if (!showPath.isSelected()) {
					removeMarkers();
					logger.info("show path false");
				} else {
					showPath.setSelected(false);
				}
			}
		});

		menuEdit.getItems().add(showPath);

		final MenuItem trackNext = new MenuItem("Track Next");
		trackNext.setDisable(true);
		trackNext.setAccelerator(KeyCombination.keyCombination("F6"));
		trackNext.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (tracking != null) {
					if (currentIndex >= 0 && currentIndex < files.length - 1) {
						tracking.trackAuto(files[++currentIndex]);
					}
					refreshImageView();
				}
			}
		});

		final MenuItem trackAll = new MenuItem("Track All");
		trackAll.setDisable(true);
		trackAll.setAccelerator(KeyCombination.keyCombination("F8"));
		trackAll.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (tracking != null) {
					for (currentIndex = 0; currentIndex < files.length - 1;) {
						tracking.trackAuto(files[++currentIndex]);
					}
					refreshImageView();
				}
			}
		});

		final MenuItem trackManual = new MenuItem("Track Manual");
		trackManual.setDisable(true);
		trackManual.setAccelerator(KeyCombination.keyCombination("F5"));
		trackManual.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				removeManualCircle();
				imgView.setCursor(Cursor.CROSSHAIR);
				imgAnchor.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {

						double initX = event.getX();
						double initY = event.getY();

						int posX = (int) (initX - imgView.getLayoutX());
						int posY = (int) (initY - imgView.getLayoutY());
						Position pos = new Position(posX, posY);

						int radius = 6;

						Circle circle = new Circle(initX, initY, radius);
						circle.setFill(null);
						circle.setStroke(Color.BLUE);
						circle.setStrokeWidth(1);

						tracking.trackManual(currentFile, pos);

						TrackingImage img = tracking.manualBallTracking.getTrackingImage(currentFile);
						if (img != null) {
							addMarker(img, Color.BLUE, Color.WHITE);
						} else {
							manualCircle = circle;
							imgAnchor.getChildren().add(manualCircle);
						}

						logger.info("x: " + posX + "  /  y: " + posY);

						imgAnchor.setOnMouseClicked(null);

						imgView.setCursor(Cursor.DEFAULT);

					}

				});

				logger.info("Track Manual. File: " + currentFile.getName());
			}
		});

		final MenuItem fixBallShape = new MenuItem("Finish Init");
		fixBallShape.setDisable(true);
		fixBallShape.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				initializeTracking.setDisable(true);
				btnColor.setDisable(true);
				fixBallShape.setDisable(true);
				trackNext.setDisable(false);
				trackAll.setDisable(false);
				trackManual.setDisable(false);
				logger.info("Initializing BallTracking finished");
			}
		});

		initializeTracking = new MenuItem("Init Tracking");
		initializeTracking.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				removeManualCircle();
				imgView.setCursor(Cursor.CROSSHAIR);
				imgAnchor.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {

						double initX = event.getX();
						double initY = event.getY();

						int posX = (int) (initX - imgView.getLayoutX());
						int posY = (int) (initY - imgView.getLayoutY());
						Position pos = new Position(posX, posY);

						int radius = 6;

						Circle circle = new Circle(initX, initY, radius);
						circle.setFill(null);
						circle.setStroke(Color.RED);
						circle.setStrokeWidth(1);

						PixelReader pixelReader = imgView.getImage().getPixelReader();
						Color color = pixelReader.getColor(posX, posY);

						System.out.println(color);

						BallShape ballShape = new BallShape(radius, color);

						if (tracking != null) {
							tracking.setBallShape(ballShape);
							tracking.initTracking(currentFile, pos);
							showPath.setDisable(false);
							btnColor.setDisable(false);
							fixBallShape.setDisable(false);
						} else {
							logger.warn("TrackingFactory is null! BallShape could not be assigned.");
						}

						manualCircle = circle;
						imgAnchor.getChildren().add(manualCircle);

						logger.info("x: " + posX + "  /  y: " + posY);

						imgAnchor.setOnMouseClicked(null);

						imgView.setCursor(Cursor.DEFAULT);

					}

				});
			}
		});

		if (tracking == null) {
			initializeTracking.setDisable(true);
		}

		menuTrack.getItems().addAll(initializeTracking, fixBallShape, new SeparatorMenuItem(), trackManual,
				new SeparatorMenuItem(), trackNext, trackAll);

		menuBar.getMenus().addAll(menuFile, menuEdit, menuTrack);

		return menuBar;
	}

	private void addMarker(TrackingImage image, Color colorBorder, Color colorFill) {

		Position pos = image.getBall().getPosition();
		int initX = pos.getX();
		int initY = pos.getY();

		Circle circle = new Circle(initX + imgView.getLayoutX(), initY + imgView.getLayoutY(), image.getBall()
				.getBallShape().getRadius());
		circle.setFill(null);
		circle.setStroke(colorBorder);
		circle.setStrokeWidth(2);

		imgAnchor.getChildren().add(circle);
		pathNodes.add(circle);

	}

	private void removeMarkers() {

		for (Node node : pathNodes) {
			imgAnchor.getChildren().remove(node);
		}
		pathNodes = new LinkedList<>();
		showPath.setSelected(false);

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

			String dirString = directory == null ? "" : directory.getAbsolutePath();
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
				logger.error("File: " + settings.getImagePlaceholder() + " not found!", e1);
				currentFile = null;
			}
		} finally {

			String labelText = directory == null ? "-" : directory.getAbsolutePath();
			lbDirectory.setText(labelText);
			labelText = currentFile == null || settings.getImagePlaceholder().endsWith(currentFile.getName()) ? "-"
					: currentFile.getName();
			lbFilename.setText(labelText);

			checkNavBtns();

			removeManualCircle();

			Image image = new Image(fIn);

			imgView.setImage(image);

			setMarker();

			if (fIn != null) {
				try {
					fIn.close();
				} catch (IOException e) {
					logger.fatal("can not close FileInputStream", e);
				}
			}
		}

	}

	private void refreshImageView() {

		FileInputStream fIn = null;

		try {

			currentFile = files[currentIndex];
			fIn = new FileInputStream(currentFile);
			logger.info("changed image to " + directory + File.separator + files[currentIndex]);

		} catch (Exception e) {
			try {
				directory = null;
				currentFile = new File(settings.getImagePlaceholder());
				fIn = new FileInputStream(currentFile);
			} catch (FileNotFoundException e1) {
				logger.error("File: " + settings.getImagePlaceholder() + " not found!", e1);
				currentFile = null;
			}
		} finally {

			String labelText = directory == null ? "-" : directory.getAbsolutePath();
			lbDirectory.setText(labelText);
			labelText = currentFile == null || settings.getImagePlaceholder().endsWith(currentFile.getName()) ? "-"
					: currentFile.getName();
			lbFilename.setText(labelText);

			removeMarkers();

			removeManualCircle();

			Image image = new Image(fIn, 640, 480, true, true);
			imgView.setImage(image);

			checkNavBtns();

			setMarker();

			if (fIn != null) {
				try {
					fIn.close();
				} catch (IOException e) {
					logger.fatal("can not close FileInputStream", e);
				}
			}
		}
	}

	private void setMarker() {
		if (tracking != null) {
			TrackingImage atrImage = tracking.autoBallTracking.getTrackingImage(currentFile);
			if (atrImage != null) {
				addMarker(atrImage, Color.RED, Color.WHITE);
			}
			TrackingImage mtrImage = tracking.manualBallTracking.getTrackingImage(currentFile);
			if (mtrImage != null) {
				addMarker(mtrImage, Color.BLUE, Color.WHITE);
			}
		}
	}

	private void removeManualCircle() {
		if (manualCircle != null) {
			imgAnchor.getChildren().remove(manualCircle);
			manualCircle = null;
		}
	}

	private void createTracking(String directory) {
		tracking = new TrackingFactory(directory, null);
		initializeTracking.setDisable(false);
	}

}
