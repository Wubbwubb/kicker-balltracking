package de.kicker.tracking.view;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class FXUtil {

	public static HBox getHBox(int width, int height, int spacing) {
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

	public static VBox getVBox(int width, int height, int spacing) {
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

}
