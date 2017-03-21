package de.kicker.tracking.view;

import de.kicker.tracking.model.BallShape;
import de.kicker.tracking.model.Position;
import de.kicker.tracking.model.TrackingImage;
import de.kicker.tracking.model.balltracking.AutomaticBallTracking;
import de.kicker.tracking.model.balltracking.TrackingFactory;
import de.kicker.tracking.model.settings.Settings;
import de.kicker.tracking.util.FXUtil;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.opencv.core.Core;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class BallTrackingApplication extends Application {

	private final static Logger logger = Logger.getLogger(BallTrackingApplication.class);
	private final static Settings settings = Settings.getInstance();

	private Shape filterShape;
	private Rectangle colorIndicator;
	private AnchorPane imgAnchor;
	private ImageView imgView;
	private Button btnPrev;
	private Button btnNext;
	private Button btnColor;
	private Button btnRadiusInc;
	private Button btnRadiusDec;
	private Button btnPositionTop;
	private Button btnPositionLeft;
	private Button btnPositionRight;
	private Button btnPositionBottom;
	private Label lbDirectory;
	private Label lbFilename;
	private CheckMenuItem showPath;
	private MenuItem showStatistics;
	private MenuItem initializeTracking;

	private File directory;
	private File currentFile;
	private File[] files;
	private int currentIndex = 0;

	private List<Node> pathNodes;
	private Circle manualCircle;

	private TrackingFactory trackingFactory;
	private int ballRadius;
	private Position initBallPosition;

	public static void main(String[] args) {

		System.out.println("java.library.path: " + System.getProperty("java.library.path"));
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		String appenderToDelete = settings.isDebugMode() ? "name.file_appender" : "name.console_appender";
		Logger rootLogger = Logger.getRootLogger();
		ResourceBundle b = ResourceBundle.getBundle("log4j");
		rootLogger.removeAppender(b.getString(appenderToDelete));
		logger.info("Start Application");
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {

		ballRadius = settings.getBallRadius();

		pathNodes = new LinkedList<>();

		VBox vboxMain = FXUtil.getVBox(-1, -1, -1);
		vboxMain.setId("vboxMain");

		BorderPane borderPaneMain = new BorderPane();
		borderPaneMain.setId("borderPaneMain");

		BorderPane borderPaneImage = new BorderPane();
		borderPaneImage.setId("borderPaneImage");

		GridPane gridPane = new GridPane();
		gridPane.setId("gridPane");

		btnPrev = new Button();
		btnPrev.setText("prev");
		btnPrev.setOnAction(event -> {
			if (currentIndex > 0 && (btnColor == null || btnColor.isDisabled())) {
				currentIndex--;
				refreshImageView(!showPath.isSelected());
			}
		});
		btnPrev.setPrefWidth(50);

		btnNext = new Button();
		btnNext.setText("next");
		btnNext.setOnAction(event -> {
			if (currentIndex < files.length - 1 && (btnColor == null || btnColor.isDisabled())) {
				currentIndex++;
				refreshImageView(!showPath.isSelected());
			}
		});
		btnNext.setPrefWidth(50);

		int width = settings.getImageWidth() + 140;
		int height = settings.getImageHeight() + 35;

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

		imgAnchor.getChildren().add(imgView);
		imgView.setLayoutX(10);
		imgView.setLayoutY(10);

		if (settings.getTopBound() >= 0 && settings.getRightBound() >= 0 && settings.getBottomBound() >= 0 && settings.getLeftBound() >= 0) {
			Rectangle r1 = new Rectangle(imgView.getLayoutX(), imgView.getLayoutY(), settings.getImageWidth(), settings
					.getImageHeight());
			Rectangle r2 = new Rectangle(settings.getLeftBound() + imgView.getLayoutX(), settings.getTopBound() + imgView
					.getLayoutY(), settings.getRightBound() - settings.getLeftBound(), settings.getBottomBound() - settings
					.getTopBound());
			filterShape = Shape.subtract(r1, r2);
			Color filterColor = Color.BLACK;
			filterColor = filterColor.deriveColor(0.0, 1.0, 1.0, 0.75);
			filterShape.setFill(filterColor);
			filterShape.setStroke(Color.AQUA);
			filterShape.setStrokeWidth(1);
			filterShape.setSmooth(true);
		} else {
			filterShape = null;
		}

		String dir = settings.getImageDir();
		if ("".equals(dir.trim())) {
			directory = null;
		} else {
			directory = new File(dir);
		}

		refreshDirectory();
		if (directory != null) {
			trackingFactory = new TrackingFactory(directory.getAbsolutePath(), null, 0, 0);
			logger.info("selected directory: " + directory);
		}

		borderPaneImage.setCenter(imgAnchor);
		borderPaneImage.setLeft(vboxLeft);
		borderPaneImage.setRight(vboxRight);
		borderPaneImage.setTop(hboxTop);
		borderPaneImage.setBottom(hboxBottom);

		borderPaneImage.setMaxSize(width, height);

		btnColor = new Button("Select Ball Color");
		btnColor.setDisable(true);
		btnColor.setOnAction(event -> {
			imgView.setCursor(Cursor.CROSSHAIR);
			if (filterShape != null) {
				filterShape.setCursor(Cursor.CROSSHAIR);
			}
			imgAnchor.setOnMouseClicked(event1 -> {

				double initX = event1.getX();
				double initY = event1.getY();

				int posX = (int) (initX - imgView.getLayoutX());
				int posY = (int) (initY - imgView.getLayoutY());

				boolean xFail = posX < 0 || posX >= imgView.getImage().getWidth();
				boolean yFail = posY < 0 || posY >= imgView.getImage().getHeight();
				if (xFail || yFail) {
					String errorMsg = "pos" + (xFail ? "X (" + posX : "Y (" + posY) + ") out of range";
					logger.error(errorMsg);
					return;
				}

				PixelReader pixelReader = imgView.getImage().getPixelReader();
				Color color = pixelReader.getColor(posX, posY);

				if (trackingFactory != null) {
					trackingFactory.getBallShape().setColor(color);
					colorIndicator.setFill(color);
					logger.info("Changed Color to " + FXUtil.getColorString(color));
				}

				imgAnchor.setOnMouseClicked(null);

				imgView.setCursor(Cursor.DEFAULT);
				if (filterShape != null) {
					filterShape.setCursor(Cursor.DEFAULT);
				}

			});
		});

		gridPane.add(btnColor, 0, 0, 3, 1);

		HBox colorBox = FXUtil.getHBox(20, 20, 0);

		colorIndicator = new Rectangle(0, 0, 20, 20);
		colorIndicator.setArcWidth(10);
		colorIndicator.setArcHeight(10);
		colorIndicator.setFill(Color.web("#aeaeae"));
		colorIndicator.setStroke(Color.BLACK);
		colorIndicator.setStrokeWidth(1);
		colorIndicator.setSmooth(true);

		colorBox.getChildren().add(colorIndicator);

		gridPane.add(colorBox, 3, 0);

		Label radiusLabel = new Label("Change Ball Radius");

		btnRadiusDec = new Button("-");
		btnRadiusDec.setDisable(true);
		btnRadiusDec.setOnAction(event -> {
			ballRadius = Math.max(ballRadius - 1, 1);
			trackingFactory.setBallShape(new BallShape(ballRadius, trackingFactory.getBallShape().getFXColor()));
			manualCircle.setRadius(ballRadius);
		});

		btnRadiusInc = new Button("+");
		btnRadiusInc.setDisable(true);
		btnRadiusInc.setOnAction(event -> {
			ballRadius = Math.min(ballRadius + 1, 100);
			trackingFactory.setBallShape(new BallShape(ballRadius, trackingFactory.getBallShape().getFXColor()));
			manualCircle.setRadius(ballRadius);
		});

		gridPane.add(radiusLabel, 0, 2, 4, 1);
		gridPane.add(btnRadiusDec, 0, 3);
		gridPane.add(btnRadiusInc, 2, 3);

		Label positionLabel = new Label("Change Ball Position");

		btnPositionTop = new Button("^");
		btnPositionTop.setDisable(true);
		btnPositionTop.setOnAction(event -> {
			if (btnRadiusInc.isDisable()) {
				// verändere das manuelle Tracking, falls eines vorhanden ist
				TrackingImage img = trackingFactory.getManualBallTracking().getTrackingImage(currentIndex);
				if (img != null && manualCircle != null) {
					img.getPosition().setY(img.getPosition().getY() - 1);
					manualCircle.setCenterY(manualCircle.getCenterY() - 1);
				}
			} else {
				// Initialisierung
				initBallPosition.setY(initBallPosition.getY() - 1);
				trackingFactory.initTracking(currentIndex, currentFile, initBallPosition);
				manualCircle.setCenterX(initBallPosition.getX() + imgView.getLayoutX());
				manualCircle.setCenterY(initBallPosition.getY() + imgView.getLayoutY());
			}
		});

		btnPositionLeft = new Button("<");
		btnPositionLeft.setDisable(true);
		btnPositionLeft.setOnAction(event -> {
			if (btnRadiusInc.isDisable()) {
				// verändere das manuelle Tracking, falls eines vorhanden ist
				TrackingImage img = trackingFactory.getManualBallTracking().getTrackingImage(currentIndex);
				if (img != null && manualCircle != null) {
					img.getPosition().setX(img.getPosition().getX() - 1);
					manualCircle.setCenterX(manualCircle.getCenterX() - 1);
				}
			} else {
				// Initialisierung
				initBallPosition.setX(initBallPosition.getX() - 1);
				trackingFactory.initTracking(currentIndex, currentFile, initBallPosition);
				manualCircle.setCenterX(initBallPosition.getX() + imgView.getLayoutX());
				manualCircle.setCenterY(initBallPosition.getY() + imgView.getLayoutY());
			}
		});

		btnPositionRight = new Button(">");
		btnPositionRight.setDisable(true);
		btnPositionRight.setOnAction(event -> {
			if (btnRadiusInc.isDisable()) {
				// verändere das manuelle Tracking, falls eines vorhanden ist
				TrackingImage img = trackingFactory.getManualBallTracking().getTrackingImage(currentIndex);
				if (img != null && manualCircle != null) {
					img.getPosition().setX(img.getPosition().getX() + 1);
					manualCircle.setCenterX(manualCircle.getCenterX() + 1);
				}
			} else {
				// Initialisierung
				initBallPosition.setX(initBallPosition.getX() + 1);
				trackingFactory.initTracking(currentIndex, currentFile, initBallPosition);
				manualCircle.setCenterX(initBallPosition.getX() + imgView.getLayoutX());
				manualCircle.setCenterY(initBallPosition.getY() + imgView.getLayoutY());
			}
		});

		btnPositionBottom = new Button("v");
		btnPositionBottom.setDisable(true);
		btnPositionBottom.setOnAction(event -> {
			if (btnRadiusInc.isDisable()) {
				// verändere das manuelle Tracking, falls eines vorhanden ist
				TrackingImage img = trackingFactory.getManualBallTracking().getTrackingImage(currentIndex);
				if (img != null && manualCircle != null) {
					img.getPosition().setY(img.getPosition().getY() + 1);
					manualCircle.setCenterY(manualCircle.getCenterY() + 1);
				}
			} else {
				// Initialisierung
				initBallPosition.setY(initBallPosition.getY() + 1);
				trackingFactory.initTracking(currentIndex, currentFile, initBallPosition);
				manualCircle.setCenterX(initBallPosition.getX() + imgView.getLayoutX());
				manualCircle.setCenterY(initBallPosition.getY() + imgView.getLayoutY());
			}
		});

		gridPane.add(positionLabel, 0, 5, 3, 1);

		gridPane.add(btnPositionTop, 1, 6);
		gridPane.add(btnPositionLeft, 0, 7);
		gridPane.add(btnPositionRight, 2, 7);
		gridPane.add(btnPositionBottom, 1, 8);

		borderPaneMain.setCenter(borderPaneImage);
		borderPaneMain.setRight(gridPane);

		MenuBar menuBar = createMenuBar(primaryStage);

		vboxMain.getChildren().add(menuBar);
		vboxMain.getChildren().add(borderPaneMain);

		Scene scene = new Scene(vboxMain);
		scene.getStylesheets().add("css/style.css");

		scene.getAccelerators().put(settings.getKeyCombinationBtnPrev(), () -> btnPrev.fire());
		scene.getAccelerators().put(settings.getKeyCombinationBtnNext(), () -> btnNext.fire());
		scene.getAccelerators().put(settings.getKeyCombinationBtnPositionTop(), () -> btnPositionTop.fire());
		scene.getAccelerators().put(settings.getKeyCombinationBtnPositionRight(), () -> btnPositionRight.fire());
		scene.getAccelerators().put(settings.getKeyCombinationBtnPositionBottom(), () -> btnPositionBottom.fire());
		scene.getAccelerators().put(settings.getKeyCombinationBtnPositionLeft(), () -> btnPositionLeft.fire());

		primaryStage.setScene(scene);

		primaryStage.setWidth(settings.getImageWidth() + 350);
		primaryStage.setHeight(settings.getImageHeight() + 152);
		primaryStage.setTitle("Kicker-Balltracking");

		try {
			Image image = new Image(getClass().getClassLoader().getResourceAsStream(settings.getWindowicon()));
			primaryStage.getIcons().add(image);
		} catch (Exception e) {
			logger.error("failed to load window icon", e);
		}

		primaryStage.show();
		primaryStage.setResizable(false);
	}

	private MenuBar createMenuBar(final Stage primaryStage) {

		MenuBar menuBar = new MenuBar();

		Menu menuFile = new Menu("File");
		Menu menuEdit = new Menu("Edit");
		Menu menuTrack = new Menu("Track");
		Menu menuHelp = new Menu("Help");

		MenuItem chooseFile = new MenuItem("Open Image");
		chooseFile.setOnAction(event -> {

			String dir = directory == null ? "" : directory.getAbsolutePath();
			File preDir = new File(dir);

			FileChooser chooser = new FileChooser();

			if (preDir.exists() && preDir.isDirectory()) {
				chooser.setInitialDirectory(preDir);
			}

			chooser.setTitle("Choose Image");
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
				refreshImageView(true);
			}

		});

		MenuItem chooseFolder = new MenuItem("Open Folder");
		chooseFolder.setOnAction(event -> {

			String dir = directory == null ? "" : directory.getAbsolutePath();
			File preDir = new File(dir);

			DirectoryChooser chooser = new DirectoryChooser();

			if (preDir.exists() && preDir.isDirectory()) {
				chooser.setInitialDirectory(preDir);
			}

			chooser.setTitle("Choose Folder");
			File selectedDir = chooser.showDialog(primaryStage);

			if (selectedDir == null) {
				logger.warn("no directory chosen");
			} else {
				logger.info("folder " + directory + " chosen");
				directory = selectedDir;
				refreshDirectory();
				createTracking(directory.getAbsolutePath());
			}

		});

		final MenuItem exportToFile = new MenuItem("Export");
		exportToFile.setDisable(true);
		exportToFile.setOnAction(event -> {

			if (trackingFactory != null
					&& (trackingFactory.getAutomaticBallTracking().getAllTrackedImages().size() > 0
					|| trackingFactory.getManualBallTracking().getAllTrackedImages().size() > 0)) {

				String dir = directory == null ? "" : directory.getAbsolutePath();
				File preDir = new File(dir);

				FileChooser chooser = new FileChooser();

				if (preDir.exists() && preDir.isDirectory()) {
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
					trackingFactory.exportToXML(selectedFile);
				}

			}

		});

		showPath = new CheckMenuItem("Show Path");
		showPath.setSelected(false);
		showPath.setDisable(true);
		showPath.setOnAction(event -> {
			if (trackingFactory != null && showPath.isSelected()) {
				Collection<TrackingImage> images = trackingFactory.getAutomaticBallTracking().getAllTrackedImages();
				Position from = null;
				for (TrackingImage image : images) {
					Position to = image.getPosition();
					if (Position.POSITION_NOT_FOUND.equals(to)) {
						continue;
					}
					addMarker(image, settings.getAutoColor(), null);
					if (from != null) {
						addLine(from, to, Color.RED);
					}
					from = to;
				}

				images = trackingFactory.getManualBallTracking().getAllTrackedImages();
				for (TrackingImage image : images) {
					addMarker(image, settings.getManualColor(), null);
				}

				logger.info("show path true");
			} else if (!showPath.isSelected()) {
				removeMarkers();
				logger.info("show path false");
			} else {
				showPath.setSelected(false);
			}
		});

		showStatistics = new MenuItem("Show Statistics");
		showStatistics.setDisable(true);
		showStatistics.setOnAction(event -> {
			if (trackingFactory == null || trackingFactory.getAutomaticBallTracking() == null || trackingFactory
					.getManualBallTracking() == null || !initializeTracking.isDisable()) {
				return;
			}

			new StatisticsDialog(trackingFactory, files.length - 1).show();
		});

		menuEdit.getItems().add(showPath);
		menuEdit.getItems().add(showStatistics);

		final MenuItem trackNext = new MenuItem("Track Next");
		trackNext.setDisable(true);
		trackNext.setAccelerator(settings.getKeyCombinationTrackNext());
		trackNext.setOnAction(event -> {
			if (trackingFactory != null) {
				int nextFactoryIndex = trackingFactory.getCurrentIndex() + 1;
				if (nextFactoryIndex > 0 && nextFactoryIndex < files.length) {
					currentIndex = nextFactoryIndex;
					trackingFactory.trackAuto(currentIndex, files[currentIndex]);
				}
				refreshImageView(!showPath.isSelected());
			}
		});

		final MenuItem trackAll = new MenuItem("Track All");
		trackAll.setDisable(true);
		trackAll.setAccelerator(settings.getKeyCombinationTrackAll());
		trackAll.setOnAction(event -> {
			if (trackingFactory != null) {
				trackAll();
			}
		});

		final MenuItem trackManual = new MenuItem("Track Manual");
		trackManual.setDisable(true);
		trackManual.setAccelerator(settings.getKeyCombinationTrackManual());
		trackManual.setOnAction(event -> {
			if (currentIndex != trackingFactory.getInitialIndex()) {
				removeManualCircle();
				imgView.setCursor(Cursor.CROSSHAIR);
				if (filterShape != null) {
					filterShape.setCursor(Cursor.CROSSHAIR);
				}
				imgAnchor.setOnMouseClicked(event1 -> {

					double initX = event1.getX();
					double initY = event1.getY();

					int posX = (int) (initX - imgView.getLayoutX());
					int posY = (int) (initY - imgView.getLayoutY());

					Position pos;
					if (posX < settings.getLeftBound() || posX > settings.getRightBound() ||
							posY < settings.getTopBound() || posY > settings.getBottomBound()) {
						pos = Position.POSITION_NOT_FOUND;
					} else {
						pos = new Position(posX, posY);
					}

					int radius = trackingFactory.getBallShape().getRadius();

					Circle circle = new Circle(initX, initY, radius);
					circle.setFill(null);
					circle.setStroke(settings.getManualColor());
					circle.setStrokeWidth(1);

					trackingFactory.trackManual(currentIndex, currentFile, pos);

					TrackingImage img = trackingFactory.getManualBallTracking().getTrackingImage(currentIndex);
					if (img != null) {
						manualCircle = addMarker(img, settings.getManualColor(), null);
					} else {
						manualCircle = circle;
						imgAnchor.getChildren().add(manualCircle);
					}

					logger.info("x: " + posX + "  /  y: " + posY);

					imgAnchor.setOnMouseClicked(null);
					imgView.setCursor(Cursor.DEFAULT);
					if (filterShape != null) {
						filterShape.setCursor(Cursor.DEFAULT);
					}
				});
			}

			logger.info("Track Manual. File: " + currentFile.getName());
		});

		final MenuItem fixBallShape = new MenuItem("Finish Init");
		fixBallShape.setDisable(true);
		fixBallShape.setOnAction(event -> {
			initializeTracking.setDisable(true);
			showStatistics.setDisable(false);
			btnColor.setDisable(true);
			btnRadiusDec.setDisable(true);
			btnRadiusInc.setDisable(true);
			fixBallShape.setDisable(true);
			trackNext.setDisable(false);
			trackAll.setDisable(false);
			trackManual.setDisable(false);
			exportToFile.setDisable(false);
			removeManualCircle();
			setMarker(false);
			imgView.setCursor(Cursor.DEFAULT);
			if (filterShape != null) {
				filterShape.setCursor(Cursor.DEFAULT);
			}
			imgAnchor.setOnMouseClicked(null);
			logger.info("Initializing BallTracking finished");
		});

		initializeTracking = new MenuItem("Init Tracking");
		initializeTracking.setOnAction(event -> {
			removeManualCircle();
			imgView.setCursor(Cursor.CROSSHAIR);
			if (filterShape != null) {
				filterShape.setCursor(Cursor.CROSSHAIR);
			}
			imgAnchor.setOnMouseClicked(event12 -> {

				double initX = event12.getX();
				double initY = event12.getY();

				int posX = (int) (initX - imgView.getLayoutX());
				int posY = (int) (initY - imgView.getLayoutY());

				boolean xFail = posX < 0 || posX >= imgView.getImage().getWidth();
				boolean yFail = posY < 0 || posY >= imgView.getImage().getHeight();
				if (xFail || yFail) {
					String errorMsg = "pos" + (xFail ? "X (" + posX : "Y (" + posY) + ") out of range";
					logger.error(errorMsg);
					return;
				}

				Position position = new Position(posX, posY);

				int radius = ballRadius;

				Circle circle = new Circle(initX, initY, radius);
				circle.setFill(null);
				circle.setStroke(settings.getAutoColor());
				circle.setStrokeWidth(1);

				PixelReader pixelReader = imgView.getImage().getPixelReader();
				Color color = pixelReader.getColor(posX, posY);

				BallShape ballShape = new BallShape(radius, color);

				if (trackingFactory != null) {
					trackingFactory.setBallShape(ballShape);
					colorIndicator.setFill(color);
					trackingFactory.initTracking(currentIndex, currentFile, position);
					initBallPosition = position;
					showPath.setDisable(false);
					btnColor.setDisable(false);
					btnRadiusDec.setDisable(false);
					btnRadiusInc.setDisable(false);
					btnPositionTop.setDisable(false);
					btnPositionLeft.setDisable(false);
					btnPositionRight.setDisable(false);
					btnPositionBottom.setDisable(false);
					fixBallShape.setDisable(false);
					logger.info("Initialized BallTracking. Image: " + currentFile.getName() + ". Position: "
							+ position.toString() + ". BallShape: " + ballShape.toString());
				} else {
					logger.warn("TrackingFactory is null! BallShape could not be assigned.");
				}

				manualCircle = circle;
				imgAnchor.getChildren().add(manualCircle);

				imgAnchor.setOnMouseClicked(null);

				imgView.setCursor(Cursor.DEFAULT);
				if (filterShape != null) {
					filterShape.setCursor(Cursor.DEFAULT);
				}

			});
		});

		MenuItem importFromFile = new MenuItem("Import");
		importFromFile.setOnAction(event -> {

			String dir = directory == null ? "" : directory.getAbsolutePath();
			File preDir = new File(dir);

			FileChooser chooser = new FileChooser();

			if (preDir.exists() && preDir.isDirectory()) {
				chooser.setInitialDirectory(preDir);
			}

			chooser.setTitle("Open File");
			ExtensionFilter xmlFilter = new ExtensionFilter("xml", "*.xml");
			chooser.getExtensionFilters().addAll(xmlFilter);
			File selectedFile = chooser.showOpenDialog(primaryStage);

			if (selectedFile == null) {
				logger.warn("no file chosen for import");
			} else {
				trackingFactory = TrackingFactory.importFromXML(selectedFile, AutomaticBallTracking.class);

				logger.info("imported from " + selectedFile.getAbsolutePath());

				exportToFile.setDisable(false);
				initializeTracking.setDisable(true);
				showStatistics.setDisable(false);
				fixBallShape.setDisable(true);
				trackManual.setDisable(false);
				trackNext.setDisable(false);
				trackAll.setDisable(false);
				btnColor.setDisable(true);
				btnRadiusDec.setDisable(true);
				btnRadiusInc.setDisable(true);
				btnPositionTop.setDisable(false);
				btnPositionRight.setDisable(false);
				btnPositionBottom.setDisable(false);
				btnPositionLeft.setDisable(false);
				showPath.setDisable(false);

				directory = new File(trackingFactory.getDirectory());
				refreshDirectory();
				currentIndex = trackingFactory.getInitialIndex();
				refreshImageView(true);
			}

		});

		menuFile.getItems().addAll(chooseFile, chooseFolder, new SeparatorMenuItem(), importFromFile, exportToFile);

		if (trackingFactory == null) {
			initializeTracking.setDisable(true);
		}

		menuTrack.getItems().addAll(initializeTracking, fixBallShape, new SeparatorMenuItem(), trackManual,
				new SeparatorMenuItem(), trackNext, trackAll);

		MenuItem helpItem = new MenuItem("Open doku");
		helpItem.setOnAction(event -> {
			File f = new File(settings.getDokuFile());
			if (f.exists()) {
				try {
					Desktop.getDesktop().open(f);
				} catch (IOException e) {
					logger.error("could not open file '" + f.getAbsolutePath() + "'", e);
				}
			} else {
				logger.error("the given file '" + f.getAbsolutePath() + "' doesn't exist!");
			}
		});

		menuHelp.getItems().add(helpItem);

		menuBar.getMenus().addAll(menuFile, menuEdit, menuTrack, menuHelp);

		return menuBar;
	}

	private void trackAll() {
		currentIndex = trackingFactory.getInitialIndex();
		trackingFactory.setCurrentIndex(currentIndex);

		final ProgressDialog dialog = new ProgressDialog(currentIndex + 1, files.length - 1);

		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {

				int maxIndex = files.length - trackingFactory.getInitialIndex();

				for (; currentIndex < files.length - 1; ) {
					if (isCancelled()) {
						logger.debug("cancelled tracking all");
						break;
					}
					currentIndex++;
					logger.debug("tracking index " + currentIndex);
					trackingFactory.trackAuto(currentIndex, files[currentIndex]);
					updateProgress(currentIndex - trackingFactory.getInitialIndex(), maxIndex);
				}

				logger.debug("finished tracking all");
				dialog.close();

				refreshImageView(true);

				return null;
			}
		};

		dialog.bindProgressProperty(task);

		Thread tread = new Thread(task);
		tread.setDaemon(true);
		logger.debug("start tracking all images from index " + currentIndex);
		tread.start();
	}

	private Circle addMarker(TrackingImage image, Color colorBorder, Color colorFill) {

		Position pos = image.getPosition();
		if (pos.isNotFound()) {
			return null;
		}

		int initX = pos.getX();
		int initY = pos.getY();

		Circle circle = new Circle(initX + imgView.getLayoutX(), initY + imgView.getLayoutY(),
				trackingFactory.getBallShape().getRadius());
		circle.setFill(colorFill);
		circle.setStroke(colorBorder);
		circle.setStrokeWidth(2);
		circle.setSmooth(true);

		imgAnchor.getChildren().add(circle);
		pathNodes.add(circle);

		return circle;
	}

	private void addLine(Position from, Position to, Color color) {
		if (Position.POSITION_NOT_FOUND.equals(from) || Position.POSITION_NOT_FOUND.equals(to)) {
			return;
		}

		Line line = new Line(from.getX() + imgView.getLayoutX(), from.getY() + imgView.getLayoutY(),
				to.getX() + imgView.getLayoutX(), to.getY() + imgView.getLayoutY());
		line.setFill(null);
		line.setStroke(color);
		line.setStrokeWidth(1);
		line.setSmooth(true);

		imgAnchor.getChildren().add(line);
		pathNodes.add(line);

	}

	private void removeMarkers() {

		for (Node node : pathNodes) {
			imgAnchor.getChildren().remove(node);
		}
		pathNodes = new LinkedList<>();
		showPath.setSelected(false);
		setMarker(false);

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

		InputStream fIn = null;

		try {

			currentIndex = 0;

			String dirString = directory == null ? "" : directory.getAbsolutePath();
			File dirFile = new File(dirString);

			files = dirFile.listFiles((dir, name) -> {
				String lcName = name.toLowerCase();
				return lcName.endsWith(".png") || lcName.endsWith(".jpg");
			});

			if (files == null) {
				files = new File[0];
			}

			Arrays.sort(files);

			fIn = new FileInputStream(files[currentIndex]);
			currentFile = files[currentIndex];

		} catch (Exception e) {
			try {
				directory = null;
				currentFile = new File(
						getClass().getClassLoader().getResource(settings.getImagePlaceholder()).getFile());
				fIn = new FileInputStream(currentFile);
			} catch (FileNotFoundException e1) {
				logger.error("failed to load image-placeholder", e1);
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

			Image image = new Image(fIn, settings.getImageWidth(), settings.getImageHeight(), true, true);

			imgView.setImage(image);

			if (filterShape != null) {
				imgAnchor.getChildren().remove(filterShape);
				imgAnchor.getChildren().add(filterShape);
			}

			setMarker(false);

			try {
				fIn.close();
			} catch (IOException e) {
				logger.fatal("can not close FileInputStream", e);
			}
		}

	}

	private void refreshImageView(boolean removeMarkers) {

		FileInputStream fIn = null;

		try {

			File oldFile = currentFile;
			currentFile = files[currentIndex];
			fIn = new FileInputStream(currentFile);

			if (!oldFile.equals(currentFile)) {
				logger.info("changed image to " + directory + File.separator + files[currentIndex].getName());
			}

		} catch (Exception e) {
			try {
				directory = null;
				currentFile = new File(
						getClass().getClassLoader().getResource(settings.getImagePlaceholder()).getFile());
				fIn = new FileInputStream(currentFile);
			} catch (FileNotFoundException e1) {
				logger.error("failed to load image-placeholder", e1);
				currentFile = null;
			}
		} finally {

			String labelText = directory == null ? "-" : directory.getAbsolutePath();
			lbDirectory.setText(labelText);
			labelText = currentFile == null || settings.getImagePlaceholder().endsWith(currentFile.getName()) ? "-"
					: currentFile.getName();
			lbFilename.setText(labelText);

			if (removeMarkers) {
				removeMarkers();
			}

			removeManualCircle();

			Image image = new Image(fIn, settings.getImageWidth(), settings.getImageHeight(), true, true);
			imgView.setImage(image);

			if (filterShape != null) {
				imgAnchor.getChildren().remove(filterShape);
				imgAnchor.getChildren().add(filterShape);
			}

			checkNavBtns();

			setMarker(!removeMarkers);

			if (fIn != null) {
				try {
					fIn.close();
				} catch (IOException e) {
					logger.fatal("can not close FileInputStream", e);
				}
			}
		}
	}

	private void setMarker(boolean addLine) {
		if (trackingFactory != null) {
			TrackingImage atrImage = trackingFactory.getAutomaticBallTracking().getTrackingImage(currentIndex);
			if (atrImage != null) {
				addMarker(atrImage, settings.getAutoColor(), null);
				if (addLine) {
					TrackingImage patrImage = trackingFactory.getAutomaticBallTracking()
							.getTrackingImage(currentIndex - 1);
					if (patrImage != null) {
						addLine(patrImage.getPosition(), atrImage.getPosition(), Color.RED);
					}
				}
			}
			TrackingImage mtrImage = trackingFactory.getManualBallTracking().getTrackingImage(currentIndex);
			if (mtrImage != null) {
				manualCircle = addMarker(mtrImage, settings.getManualColor(), null);
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
		trackingFactory = new TrackingFactory(directory, null, 0, 0);
		initializeTracking.setDisable(false);
	}

}
