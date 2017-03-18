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
import javafx.scene.layout.*;
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
		setResizable(false);
		initDialog(trackingFactory, maxIndex);
		logger.debug("show dialog");
		show();
	}

	private void initDialog(TrackingFactory trackingFactory, int maxIndex) {

		VBox root = FXUtil.getVBox(-1, -1, 0);
		root.setMinWidth(1000);
		root.setMinHeight(400);
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

		Label tmpLbl;
		HBox tmpHBox;
		VBox tmpVBox;

		String headingStyle = "-fx-font-size: 1.5em;\n-fx-font-weight: bolder;";

		HBox absoluteContainer = FXUtil.getHBox(-1, -1, 0);
		absoluteContainer.setMinHeight(200);
		absoluteContainer.setPadding(new Insets(0));
		root.getChildren().add(absoluteContainer);

		VBox absoluteAutoContainer = FXUtil.getVBox(-1, -1, 0);
		absoluteAutoContainer.setMinWidth(500);
		absoluteAutoContainer.setPadding(new Insets(0));
		absoluteAutoContainer.setStyle("-fx-border-color: #000000;\n-fx-border-width: 0 1 1 0;");
		absoluteContainer.getChildren().add(absoluteAutoContainer);

		VBox absoluteManualContainer = FXUtil.getVBox(-1, -1, 0);
		absoluteManualContainer.setMinWidth(500);
		absoluteManualContainer.setPadding(new Insets(0));
		absoluteManualContainer.setStyle("-fx-border-color: #000000;\n-fx-border-width: 0 0 1 1;");
		absoluteContainer.getChildren().add(absoluteManualContainer);

		tmpHBox = FXUtil.getHBox(-1, -1, 5);
		tmpHBox.setPadding(new Insets(15));
		tmpHBox.setAlignment(Pos.CENTER);
		tmpLbl = new Label("Auto Tracking");
		tmpLbl.setStyle(headingStyle);
		tmpHBox.getChildren().add(tmpLbl);
		absoluteAutoContainer.getChildren().add(tmpHBox);

		tmpVBox = FXUtil.getVBox(-1, -1, 0);
		tmpVBox.setPadding(new Insets(0));
		absoluteAutoContainer.getChildren().add(tmpVBox);

		addAbsoluteStats(tmpVBox, (AbstractBallTracking) automaticBallTracking, maxIndex, true);

		tmpHBox = FXUtil.getHBox(-1, -1, 5);
		tmpHBox.setPadding(new Insets(15));
		tmpHBox.setAlignment(Pos.CENTER);
		tmpLbl = new Label("Manual Tracking");
		tmpLbl.setStyle(headingStyle);
		tmpHBox.getChildren().add(tmpLbl);
		absoluteManualContainer.getChildren().add(tmpHBox);

		tmpVBox = FXUtil.getVBox(-1, -1, 0);
		tmpVBox.setPadding(new Insets(0));
		absoluteManualContainer.getChildren().add(tmpVBox);

		addAbsoluteStats(tmpVBox, manualBallTracking, maxIndex, false);

		VBox compareContainer = FXUtil.getVBox(-1, -1, 0);
		compareContainer.setMinHeight(200);
		compareContainer.setPadding(new Insets(0));
		compareContainer.setStyle("-fx-border-color: #000000;\n-fx-border-width: 1 0 0 0;");
		root.getChildren().add(compareContainer);

		tmpHBox = FXUtil.getHBox(-1, -1, 5);
		tmpHBox.setPadding(new Insets(15));
		tmpHBox.setAlignment(Pos.CENTER);
		tmpLbl = new Label("Compare");
		tmpLbl.setStyle(headingStyle);
		tmpHBox.getChildren().add(tmpLbl);
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
		cc = new ColumnConstraints();
		cc.setHalignment(HPos.CENTER);
		cc.setMinWidth(20);
		grid.getColumnConstraints().add(cc);
		cc = new ColumnConstraints();
		cc.setHalignment(HPos.RIGHT);
		grid.getColumnConstraints().add(cc);
		cc = new ColumnConstraints();
		cc.setHalignment(HPos.LEFT);
		grid.getColumnConstraints().add(cc);
		tmpVBox.getChildren().add(grid);

		Set<Integer> autoIndizes = automaticBallTracking.getTrackedIndizes();
		Set<Integer> manualIndizes = manualBallTracking.getTrackedIndizes();

		int max = Math.max(autoIndizes.stream().max(Integer::compareTo).get(),
				manualIndizes.stream().max(Integer::compareTo).get());
		int falsePositives = 0;
		int falseNegatives = 0;
		int totalCompare = 0;
		int noOfEquals = 0;
		double maxDist = Double.MIN_VALUE;
		double minDist = Double.MAX_VALUE;
		double avgDist = 0.0;
		for (int i = trackingFactory.getInitialIndex(); i <= max; i++) {
			TrackingImage autoImg = automaticBallTracking.getTrackingImage(i);
			TrackingImage manualImg = manualBallTracking.getTrackingImage(i);
			if (autoImg != null && manualImg != null) {
				if (!Objects.equals(autoImg.getPosition(), Position.POSITION_NOT_FOUND) && Objects.equals(manualImg.getPosition
						(), Position.POSITION_NOT_FOUND)) {
					falsePositives++;
				} else if (Objects.equals(autoImg.getPosition(), Position.POSITION_NOT_FOUND) && !Objects.equals(manualImg
						.getPosition(), Position.POSITION_NOT_FOUND)) {
					falseNegatives++;
				}
				if (Objects.equals(autoImg.getPosition(), manualImg.getPosition())) {
					noOfEquals++;
				}
				double tmpDist = Position.getDistance(autoImg.getPosition(), manualImg.getPosition());
				maxDist = Math.max(maxDist, tmpDist);
				minDist = Math.min(minDist, tmpDist);
				avgDist = (avgDist * totalCompare + tmpDist) / (double) (totalCompare + 1);
				totalCompare++;
			}
		}

		grid.add(new Label("#comparable tracks:"), 0, 0);
		grid.add(new Label(String.valueOf(totalCompare)), 1, 0);

		grid.add(new Label("#same position:"), 0, 1);
		grid.add(new Label(String.valueOf(noOfEquals)), 1, 1);

		grid.add(new Label("#false positives:"), 0, 2);
		grid.add(new Label(String.valueOf(falsePositives) + " (" +
				FORMAT.format((falsePositives / (double) totalCompare) * 100) + "%)"), 1, 2);

		grid.add(new Label("#false negatives:"), 0, 3);
		grid.add(new Label(String.valueOf(falseNegatives) + " (" + FORMAT.format((falseNegatives / (double) totalCompare) *
				100) + "%)"), 1, 3);

		grid.add(new Label("minimum distance [px]:"), 3, 0);
		grid.add(new Label(FORMAT.format(minDist)), 4, 0);

		grid.add(new Label("maximum distance [px]:"), 3, 1);
		grid.add(new Label(FORMAT.format(maxDist)), 4, 1);

		grid.add(new Label("average distance [px]:"), 3, 2);
		grid.add(new Label(FORMAT.format(avgDist)), 4, 2);
	}

	private void addAbsoluteStats(VBox container, AbstractBallTracking bt, int maxIndex, boolean auto) {
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

		grid.add(new Label("#tracked images:"), 0, 0);
		grid.add(new Label(String.valueOf(allTrackedImages.size()) + " of " + noOfImages + " (" +
				FORMAT.format((allTrackedImages.size() / (double) noOfImages) * 100) + "%)"), 1, 0);

		grid.add(new Label("#position not found:"), 0, 1);
		grid.add(new Label(String.valueOf(posNotFound) + " (" + FORMAT.format((posNotFound / (double) allTrackedImages.size()) *
				100) + "%)"), 1, 1);

		if (!allTrackedImages.isEmpty()) {
			Set<Integer> indizes = bt.getTrackedIndizes();
			Integer min = indizes.stream().min(Integer::compareTo).get();
			Integer max = indizes.stream().max(Integer::compareTo).get();

			grid.add(new Label("first tracked index:"), 0, 2);
			grid.add(new Label(min.toString() + " ('" + bt.getTrackingImage(min).getFile().getName() + "')"), 1, 2);

			grid.add(new Label("last tracked index:"), 0, 3);
			grid.add(new Label(max.toString() + " ('" + bt.getTrackingImage(max).getFile().getName() + "')"), 1, 3);

			if (!auto) {
				grid.add(new Label("#not tracked:"), 0, 4);
				grid.add(new Label(String.valueOf(max - min + 1 - indizes.size())), 1, 4);
			}
		}
	}

}
