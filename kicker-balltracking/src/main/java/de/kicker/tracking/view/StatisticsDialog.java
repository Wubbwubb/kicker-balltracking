package de.kicker.tracking.view;

import de.kicker.tracking.model.Position;
import de.kicker.tracking.model.TrackingImage;
import de.kicker.tracking.model.balltracking.AbstractBallTracking;
import de.kicker.tracking.model.balltracking.IAutomaticBallTracking;
import de.kicker.tracking.model.balltracking.ManualBallTracking;
import de.kicker.tracking.model.balltracking.TrackingFactory;
import de.kicker.tracking.model.settings.Settings;
import de.kicker.tracking.util.FXUtil;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

class StatisticsDialog extends Stage {

	private static final Logger logger = Logger.getLogger(StatisticsDialog.class);
	private final static Settings settings = Settings.getInstance();

	private static final DecimalFormat FORMAT = new DecimalFormat("0.00");

	StatisticsDialog(TrackingFactory trackingFactory, int maxIndex) {
		super(StageStyle.DECORATED);
		initModality(Modality.APPLICATION_MODAL);
		initDialog(trackingFactory, maxIndex);
		logger.debug("show dialog");
		show();
	}

	private void initDialog(TrackingFactory trackingFactory, int maxIndex) {
//
//		Label label = new Label("tracking from index " + index + " to index " + maxIndex);
//		HBox hbox = FXUtil.getHBox(-1, -1, 15);
//		progressBar = new ProgressBar();
//		progressIndicator = new ProgressIndicator();
//
//		progressIndicator.setMinSize(30, 40);
//
//		Button btnCancel = new Button("cancel");
//		btnCancel.setOnAction(event -> {
//            if (task != null) {
//                task.cancel(false);
//                close();
//            }
//        });
//
//		ObservableList<Node> hboxElements = hbox.getChildren();
//		hboxElements.add(progressBar);
//		hboxElements.add(progressIndicator);
//		hboxElements.add(btnCancel);
//
//		ObservableList<Node> rootElements = root.getChildren();
//		rootElements.add(label);
//		rootElements.add(hbox);
//
//		setOnCloseRequest(event -> {
//            if (task != null) {
//                task.cancel(false);
//            }
//        });

		VBox root = FXUtil.getVBox(-1, -1, 0);
		root.setMinWidth(600);
		root.setMinHeight(600);
		root.setPadding(new Insets(0));

		setScene(new Scene(root));

		try {
			Image image = new Image(getClass().getClassLoader().getResourceAsStream(settings.getWindowicon()));
			getIcons().add(image);
		} catch (Exception e) {
			logger.error("failed to load window icon", e);
		}

		setTitle("Statistics");

		IAutomaticBallTracking automaticBallTracking = trackingFactory.getAutomaticBallTracking();
		ManualBallTracking manualBallTracking = trackingFactory.getManualBallTracking();

		if (automaticBallTracking == null || manualBallTracking == null) {
			return;
		}

		HBox absoluteContainer = FXUtil.getHBox(-1, -1, 0);
		absoluteContainer.setMinHeight(200);
		absoluteContainer.setPadding(new Insets(0));
		root.getChildren().add(absoluteContainer);

		VBox absoluteAutoContainer = FXUtil.getVBox(-1, -1, 0);
		absoluteAutoContainer.setMinWidth(300);
		absoluteAutoContainer.setPadding(new Insets(0));
		absoluteContainer.getChildren().add(absoluteAutoContainer);

		VBox absoluteManualContainer = FXUtil.getVBox(-1, -1, 0);
		absoluteManualContainer.setMinWidth(300);
		absoluteManualContainer.setPadding(new Insets(0));
		absoluteContainer.getChildren().add(absoluteManualContainer);

		HBox tmpHBox = FXUtil.getHBox(-1, -1, 5);
		tmpHBox.setPadding(new Insets(15));
		tmpHBox.setAlignment(Pos.CENTER);
		tmpHBox.getChildren().add(new Label("Auto Tracking"));
		absoluteAutoContainer.getChildren().add(tmpHBox);

		VBox tmpVBox = FXUtil.getVBox(-1, -1, 0);
		tmpVBox.setPadding(new Insets(0));
		absoluteAutoContainer.getChildren().add(tmpVBox);

		addAbsoluteStats(tmpVBox, (AbstractBallTracking) automaticBallTracking, maxIndex);

		tmpHBox = FXUtil.getHBox(-1, -1, 5);
		tmpHBox.setPadding(new Insets(15));
		tmpHBox.setAlignment(Pos.CENTER);
		tmpHBox.getChildren().add(new Label("Manual Tracking"));
		absoluteManualContainer.getChildren().add(tmpHBox);

		tmpVBox = FXUtil.getVBox(-1, -1, 0);
		tmpVBox.setPadding(new Insets(0));
		absoluteManualContainer.getChildren().add(tmpVBox);

		addAbsoluteStats(tmpVBox, manualBallTracking, maxIndex);

		VBox compareContainer = FXUtil.getVBox(-1, -1, 0);
		compareContainer.setMinHeight(400);
		compareContainer.setPadding(new Insets(0));
		root.getChildren().add(compareContainer);

		tmpHBox = FXUtil.getHBox(-1, -1, 5);
		tmpHBox.setPadding(new Insets(15));
		tmpHBox.setAlignment(Pos.CENTER);
		tmpHBox.getChildren().add(new Label("Compare"));
		compareContainer.getChildren().add(tmpHBox);

		tmpVBox = FXUtil.getVBox(-1, -1, 0);
		tmpVBox.setPadding(new Insets(0));
		compareContainer.getChildren().add(tmpVBox);

		logger.debug("hier kommt die Statistik");
		GridPane grid = new GridPane();
		grid.setHgap(15);
		grid.setVgap(8);
		grid.setAlignment(Pos.CENTER);
		ColumnConstraints cc = new ColumnConstraints();
		cc.setHalignment(HPos.RIGHT);
		grid.getColumnConstraints().add(cc);
		cc = new ColumnConstraints();
		cc.setHalignment(HPos.LEFT);
		grid.getColumnConstraints().add(cc);
		tmpVBox.getChildren().add(grid);

		Set<Integer> autoIndizes = automaticBallTracking.getTrackedIndizes();
		Set<Integer> manualIndizes = manualBallTracking.getTrackedIndizes();
		Collection<TrackingImage> autoImages = automaticBallTracking.getAllTrackedImages();
		Collection<TrackingImage> manualImages = manualBallTracking.getAllTrackedImages();

		int max = Math.max(autoIndizes.stream().max(Integer::compareTo).get(), manualIndizes.stream().max(Integer::compareTo).get());
		int falsePositives = 0;
		int falseNegatives = 0;
		int totalCompare = 0;
		for (int i = trackingFactory.getInitialIndex(); i < max; i++) {
			TrackingImage autoImg = automaticBallTracking.getTrackingImage(i);
			TrackingImage manualImg = manualBallTracking.getTrackingImage(i);
			if (autoImg != null && manualImg != null) {
				totalCompare++;
				if (!Objects.equals(autoImg.getPosition(), Position.POSITION_NOT_FOUND) && Objects.equals(manualImg.getPosition(), Position.POSITION_NOT_FOUND)) {
					falsePositives++;
				} else if (Objects.equals(autoImg.getPosition(), Position.POSITION_NOT_FOUND) && !Objects.equals(manualImg.getPosition(), Position.POSITION_NOT_FOUND)) {
					falseNegatives++;
				}
			}
		}

		grid.add(new Label("false positives:"), 0, 0);
		grid.add(new Label(String.valueOf(falsePositives) + " (" +
				FORMAT.format((falsePositives / (double) totalCompare) * 100) + "%)"), 1, 0);

		grid.add(new Label("false negatives:"), 0, 1);
		grid.add(new Label(String.valueOf(falseNegatives) + " (" + FORMAT.format((falseNegatives / (double) totalCompare) *
				100) + "%)"), 1, 1);
	}

	private void addAbsoluteStats(VBox container, AbstractBallTracking bt, int maxIndex) {
		GridPane grid = new GridPane();
		grid.setHgap(15);
		grid.setVgap(8);
		grid.setAlignment(Pos.CENTER);
		ColumnConstraints cc = new ColumnConstraints();
		cc.setHalignment(HPos.RIGHT);
		grid.getColumnConstraints().add(cc);
		cc = new ColumnConstraints();
		cc.setHalignment(HPos.LEFT);
		grid.getColumnConstraints().add(cc);
		container.getChildren().add(grid);

		int noOfImages = maxIndex + 1;
		Collection<TrackingImage> allTrackedImages = bt.getAllTrackedImages();
		long posNotFound = allTrackedImages.stream().filter(i -> Objects.equals(i.getPosition(), Position.POSITION_NOT_FOUND))
				.count();

		grid.add(new Label("tracked images:"), 0, 0);
		grid.add(new Label(String.valueOf(allTrackedImages.size()) + " of " + noOfImages + " (" +
				FORMAT.format((allTrackedImages.size() / (double) noOfImages) * 100) + "%)"), 1, 0);

		grid.add(new Label("position not found:"), 0, 1);
		grid.add(new Label(String.valueOf(posNotFound) + " (" + FORMAT.format((posNotFound / (double) allTrackedImages.size()) *
				100) + "%)"), 1, 1);
	}

}
