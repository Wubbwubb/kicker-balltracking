package de.kicker.tracking.view;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProgressDialog extends Stage {

	public ProgressDialog() {
		super(StageStyle.UTILITY);
		initModality(Modality.APPLICATION_MODAL);
		initDialog();
	}

	private void initDialog() {
		
	}

}
