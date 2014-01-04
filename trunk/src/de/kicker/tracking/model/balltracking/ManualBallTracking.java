package de.kicker.tracking.model.balltracking;

import java.io.File;
import java.util.Map;

import de.kicker.tracking.model.TrackingImage;
import de.kicker.tracking.model.xml.XMLType;

@XMLType(type = "manual")
public class ManualBallTracking extends AbstractBallTracking {

	public ManualBallTracking() {
		super();
	}

	public ManualBallTracking(Map<File, TrackingImage> trackedImages) {
		super(trackedImages);
	}

}
