package de.kicker.tracking.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.apache.log4j.Logger;

import de.kicker.tracking.model.BallShape;
import de.kicker.tracking.model.Position;
import de.kicker.tracking.model.TrackingImage;
import de.kicker.tracking.model.balltracking.TrackingFactory;
import de.kicker.tracking.model.settings.Settings;
import de.kicker.tracking.util.FXUtil;

public class BallTrackingApplication extends Application {

	private final static Logger logger = Logger.getLogger(BallTrackingApplication.class);
	private final static Settings settings = Settings.getInstance();

	private BorderPane borderPaneMain;
	private GridPane gridPane;
	private Rectangle colorIndicator;
	private AnchorPane imgAnchor;
	private ImageView imgView;
	private Button btnPrev;
	private Button btnNext;
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

	private TrackingFactory trackingFactory;

	public static void main(String[] args) {
		String appenderToDelete = settings.isDebugMode() ? "name.file_appender" : "name.console_appender";
		Logger rootLogger = Logger.getRootLogger();
		ResourceBundle b = ResourceBundle.getBundle("log4j");
		rootLogger.removeAppender(b.getString(appenderToDelete));
		logger.info("Start Application");
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {

		pathNodes = new LinkedList<>();

		VBox vboxMain = FXUtil.getVBox(-1, -1, -1);
		borderPaneMain = new BorderPane();
		borderPaneMain.setId("borderPaneMain");

		BorderPane borderPaneImage = new BorderPane();
		borderPaneImage.setId("borderPaneImage");

		gridPane = new GridPane();
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

					}

				});
			}

		});

		gridPane.add(btnColor, 0, 0);

		HBox colorBox = FXUtil.getHBox(20, 20, 0);

		colorIndicator = new Rectangle(0, 0, 20, 20);
		colorIndicator.setArcWidth(10);
		colorIndicator.setArcHeight(10);
		colorIndicator.setFill(Color.web("#aeaeae"));
		colorIndicator.setStroke(Color.BLACK);
		colorIndicator.setStrokeWidth(1);
		colorIndicator.setSmooth(true);

		colorBox.getChildren().add(colorIndicator);

		gridPane.add(colorBox, 1, 0);

		borderPaneMain.setCenter(borderPaneImage);
		borderPaneMain.setRight(gridPane);

		MenuBar menuBar = createMenuBar(primaryStage);

		vboxMain.getChildren().add(menuBar);
		vboxMain.getChildren().add(borderPaneMain);

		Scene scene = new Scene(vboxMain, settings.getWindowWidth(), settings.getWindowHeight());
		scene.getStylesheets().add("css/style.css");

		scene.getAccelerators().put(settings.getKeyCombinationBtnPrev(), new Runnable() {
			@Override
			public void run() {
				btnPrev.fire();
			}
		});
		scene.getAccelerators().put(settings.getKeyCombinationBtnNext(), new Runnable() {
			@Override
			public void run() {
				btnNext.fire();
			}
		});

		primaryStage.setScene(scene);

		primaryStage.setWidth(Math.max(settings.getWindowWidth(), settings.getImageWidth() + 350));
		primaryStage.setHeight(Math.max(settings.getWindowHeight(), settings.getImageHeight() + 162));
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

		final MenuItem exportToFile = new MenuItem("Export");
		exportToFile.setDisable(true);
		exportToFile.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				if (trackingFactory != null
						&& (trackingFactory.autoBallTracking.getAllTrackedImages().size() > 0 || trackingFactory.manualBallTracking
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
						trackingFactory.exportToXML(selectedFile);
					}

				}

			}

		});

		showPath = new CheckMenuItem("Show Path");
		showPath.setSelected(false);
		showPath.setDisable(true);
		showPath.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (trackingFactory != null && showPath.isSelected()) {
					Collection<TrackingImage> images = trackingFactory.autoBallTracking.getAllTrackedImages();
					for (TrackingImage image : images) {
						addMarker(image, Color.RED, Color.WHITE);
					}

					images = trackingFactory.manualBallTracking.getAllTrackedImages();
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
		trackNext.setAccelerator(settings.getKeyCombinationTrackNext());
		trackNext.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (trackingFactory != null) {
					int nextFactoryIndex = trackingFactory.getCurrentIndex() + 1;
					if (nextFactoryIndex > 0 && nextFactoryIndex < files.length) {
						currentIndex = nextFactoryIndex;
						trackingFactory.trackAuto(currentIndex, files[currentIndex]);
					}
					refreshImageView();
				}
			}
		});

		final MenuItem trackAll = new MenuItem("Track All");
		trackAll.setDisable(true);
		trackAll.setAccelerator(settings.getKeyCombinationTrackAll());
		trackAll.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (trackingFactory != null) {
					currentIndex = trackingFactory.trackAll(files);
					refreshImageView();
				}
			}
		});

		final MenuItem trackManual = new MenuItem("Track Manual");
		trackManual.setDisable(true);
		trackManual.setAccelerator(settings.getKeyCombinationTrackManual());
		trackManual.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				removeManualCircle();
				Stage dialog = new Stage(StageStyle.UTILITY);
				dialog.initModality(Modality.APPLICATION_MODAL);
				Scene scene = new Scene(new Group(new ProgressBar()));
				dialog.setScene(scene);
				dialog.show();
				imgView.setCursor(Cursor.CROSSHAIR);
				imgAnchor.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {

						double initX = event.getX();
						double initY = event.getY();

						int posX = (int) (initX - imgView.getLayoutX());
						int posY = (int) (initY - imgView.getLayoutY());
						Position pos = new Position(posX, posY);

						int radius = settings.getBallRadius();

						Circle circle = new Circle(initX, initY, radius);
						circle.setFill(null);
						circle.setStroke(Color.BLUE);
						circle.setStrokeWidth(1);

						trackingFactory.trackManual(currentIndex, currentFile, pos);

						TrackingImage img = trackingFactory.manualBallTracking.getTrackingImage(currentIndex);
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
				exportToFile.setDisable(false);
				removeManualCircle();
				setMarker();
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

						boolean xFail = posX < 0 || posX >= imgView.getImage().getWidth();
						boolean yFail = posY < 0 || posY >= imgView.getImage().getHeight();
						if (xFail || yFail) {
							String errorMsg = "pos" + (xFail ? "X (" + posX : "Y (" + posY) + ") out of range";
							logger.error(errorMsg);
							return;
						}

						Position position = new Position(posX, posY);

						int radius = settings.getBallRadius();

						Circle circle = new Circle(initX, initY, radius);
						circle.setFill(null);
						circle.setStroke(Color.RED);
						circle.setStrokeWidth(1);

						PixelReader pixelReader = imgView.getImage().getPixelReader();
						Color color = pixelReader.getColor(posX, posY);

						BallShape ballShape = new BallShape(radius, color);

						if (trackingFactory != null) {
							trackingFactory.setBallShape(ballShape);
							colorIndicator.setFill(color);
							trackingFactory.initTracking(currentIndex, currentFile, position);
							showPath.setDisable(false);
							btnColor.setDisable(false);
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

					}

				});
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
					trackingFactory = TrackingFactory.importFromXML(selectedFile);

					logger.info("imported from " + selectedFile.getAbsolutePath());

					exportToFile.setDisable(false);
					initializeTracking.setDisable(true);
					fixBallShape.setDisable(true);
					trackManual.setDisable(false);
					trackNext.setDisable(false);
					trackAll.setDisable(false);
					btnColor.setDisable(true);
					showPath.setDisable(false);

					directory = new File(trackingFactory.getDirectory());
					refreshDirectory();
					currentIndex = trackingFactory.getInitialIndex();
					refreshImageView();
				}

			}

		});

		menuFile.getItems().addAll(chooseFile, chooseFolder, new SeparatorMenuItem(), importFromFile, exportToFile);

		if (trackingFactory == null) {
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
		circle.setSmooth(true);

		imgAnchor.getChildren().add(circle);
		pathNodes.add(circle);

	}

	private void removeMarkers() {

		for (Node node : pathNodes) {
			imgAnchor.getChildren().remove(node);
		}
		pathNodes = new LinkedList<>();
		showPath.setSelected(false);
		setMarker();

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

			files = dirFile.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					String lcName = name.toLowerCase();
					return lcName.endsWith(".png") || lcName.endsWith(".jpg");
				}
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

			Image image = new Image(fIn, settings.getImageWidth(), settings.getImageHeight(), true, true);

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

			File oldFile = currentFile;
			currentFile = files[currentIndex];
			fIn = new FileInputStream(currentFile);

			if (!oldFile.equals(currentFile)) {
				logger.info("changed image to " + directory + File.separator + files[currentIndex].getName());
			}

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

			Image image = new Image(fIn, settings.getImageWidth(), settings.getImageHeight(), true, true);
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
		if (trackingFactory != null) {
			TrackingImage atrImage = trackingFactory.autoBallTracking.getTrackingImage(currentIndex);
			if (atrImage != null) {
				addMarker(atrImage, Color.RED, Color.WHITE);
			}
			TrackingImage mtrImage = trackingFactory.manualBallTracking.getTrackingImage(currentIndex);
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
		trackingFactory = new TrackingFactory(directory, null, 0, 0);
		initializeTracking.setDisable(false);
	}

}
