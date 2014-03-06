package de.kicker.tracking.model.balltracking;

import java.util.Map;

import de.kicker.tracking.model.TrackingImage;
import de.kicker.tracking.model.xml.BallTrackingColor;
import de.kicker.tracking.model.xml.BallTrackingType;

@BallTrackingType(value = "manual")
@BallTrackingColor(rgb = -16776961)
public class ManualBallTracking extends AbstractBallTracking {

	public ManualBallTracking() {
		super();
	}

	public ManualBallTracking(Map<Integer, TrackingImage> trackedImages) {
		super(trackedImages);
	}

}
