package de.kicker.tracking.view;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.apache.log4j.Logger;

import de.kicker.tracking.util.FXUtil;

public class ProgressDialog extends Stage {

	private static final Logger logger = Logger.getLogger(ProgressDialog.class);

	private Label label;
	private ProgressBar progressBar;
	private ProgressIndicator progressIndicator;
	private Button btnCancel;
	private Task<Void> task;

	public ProgressDialog(int currentIndex, int maxIndex) {
		super(StageStyle.UTILITY);
		initModality(Modality.APPLICATION_MODAL);
		initDialog(currentIndex, maxIndex);
		logger.debug("show dialog");
		show();
	}

	private void initDialog(int index, int maxIndex) {

		VBox root = FXUtil.getVBox(-1, -1, 20);
		root.setMinWidth(275);
		root.setPadding(new Insets(20));

		label = new Label("tracking from index " + index + " to index " + maxIndex);
		HBox hbox = FXUtil.getHBox(-1, -1, 15);
		progressBar = new ProgressBar();
		progressIndicator = new ProgressIndicator();

		progressIndicator.setMinSize(30, 40);

		btnCancel = new Button("cancel");
		btnCancel.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (task != null) {
					task.cancel(false);
					close();
				}
			}
		});

		ObservableList<Node> hboxElements = hbox.getChildren();
		hboxElements.add(progressBar);
		hboxElements.add(progressIndicator);
		hboxElements.add(btnCancel);

		ObservableList<Node> rootElements = root.getChildren();
		rootElements.add(label);
		rootElements.add(hbox);

		setScene(new Scene(root));

		setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				if (task != null) {
					task.cancel(false);
				}
			}
		});

		setTitle("Track All - Progress");

	}

	public void bindProgressProperty(Task<Void> task) {
		this.task = task;
		progressBar.progressProperty().bind(task.progressProperty());
		progressIndicator.progressProperty().bind(task.progressProperty());
	}

}
