package de.kicker.tracking.model.balltracking;

import java.io.File;
import java.util.Map;
import java.util.Random;

import de.kicker.tracking.model.Ball;
import de.kicker.tracking.model.BallShape;
import de.kicker.tracking.model.Position;
import de.kicker.tracking.model.TrackingImage;
import de.kicker.tracking.model.xml.XMLType;

@XMLType(value = "auto")
public class AutomaticBallTracking extends AbstractBallTracking {

	public AutomaticBallTracking() {
		super();
	}

	public AutomaticBallTracking(Map<Integer, TrackingImage> trackedImages) {
		super(trackedImages);
	}

	public void trackFile(int index, File file, BallShape ballShape) {
		Random r = new Random();
		int x = r.nextInt(492) + 68;
		int y = r.nextInt(283) + 77;
		Position pos = new Position(x, y);
		Ball ball = new Ball(pos, ballShape);
		assignBallToFile(index, file, ball);
	}

}
