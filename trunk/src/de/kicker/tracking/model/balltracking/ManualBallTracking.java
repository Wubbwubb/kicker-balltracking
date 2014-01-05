package de.kicker.tracking.model.balltracking;

import java.util.Map;

import de.kicker.tracking.model.TrackingImage;
import de.kicker.tracking.model.xml.XMLType;

@XMLType(value = "manual")
public class ManualBallTracking extends AbstractBallTracking {

	public ManualBallTracking() {
		super();
	}

	public ManualBallTracking(Map<Integer, TrackingImage> trackedImages) {
		super(trackedImages);
	}

}
