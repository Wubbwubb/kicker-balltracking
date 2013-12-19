package de.kicker.tracking.model.balltracking;

import java.io.File;
import java.util.Map;
import java.util.Random;

import de.kicker.tracking.model.Ball;
import de.kicker.tracking.model.Position;
import de.kicker.tracking.model.TrackingImage;

public class AutomaticBallTracking extends AbstractBallTracking {

	public AutomaticBallTracking(String directory) {
		super(directory);
	}

	public AutomaticBallTracking(String directory, Map<File, TrackingImage> trackedImages) {
		super(directory, trackedImages);
	}

	@Override
	public void trackFile(File file) {
		Random r = new Random();
		int x = r.nextInt(492) + 68;
		int y = r.nextInt(283) + 77;
		Position pos = new Position(x, y);
		Ball ball = new Ball(pos);
		TrackingImage image = new TrackingImage(file, ball);
		trackedImages.put(file, image);
	}

}
