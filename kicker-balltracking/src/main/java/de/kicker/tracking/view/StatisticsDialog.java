package de.kicker.tracking.view;

import de.kicker.tracking.model.balltracking.IAutomaticBallTracking;
import de.kicker.tracking.model.balltracking.ManualBallTracking;
import de.kicker.tracking.model.balltracking.TrackingFactory;
import de.kicker.tracking.model.settings.Settings;
import de.kicker.tracking.util.FXUtil;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.log4j.Logger;

class StatisticsDialog extends Stage {

	private static final Logger logger = Logger.getLogger(StatisticsDialog.class);
	private final static Settings settings = Settings.getInstance();

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

		VBox root = FXUtil.getVBox(-1, -1, 20);
		root.setMinWidth(500);
		root.setMinHeight(600);
		root.setPadding(new Insets(20));

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

		logger.debug("hier kommt die Statistik");

		int initialIndex = trackingFactory.getInitialIndex();

		Label lbl = new Label("Automatic Tracking");
		TextField txt = new TextField("");
	}

}
