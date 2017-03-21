package de.kicker.tracking.model.balltracking;

import java.util.Map;

import de.kicker.tracking.model.TrackingImage;
import de.kicker.tracking.model.xml.BallTrackingType;

@BallTrackingType(value = "manual")
public class ManualBallTracking extends AbstractBallTracking {

	ManualBallTracking() {
		super();
	}

	public ManualBallTracking(Map<Integer, TrackingImage> trackedImages) {
		super(trackedImages);
	}

}
