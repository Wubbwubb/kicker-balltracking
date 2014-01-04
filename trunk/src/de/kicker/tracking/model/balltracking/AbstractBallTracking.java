package de.kicker.tracking.model.balltracking;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.kicker.tracking.model.Ball;
import de.kicker.tracking.model.TrackingImage;

public abstract class AbstractBallTracking {

	protected Map<File, TrackingImage> trackedImages;

	protected AbstractBallTracking() {
		this(new HashMap<File, TrackingImage>());
	}

	protected AbstractBallTracking(Map<File, TrackingImage> trackedImages) {
		this.trackedImages = trackedImages;
	}

	public Collection<TrackingImage> getAllTrackedImages() {
		Collection<TrackingImage> images = new LinkedList<>();
		for (TrackingImage image : trackedImages.values()) {
			images.add(image);
		}
		return images;
	}

	public TrackingImage getTrackingImage(File file) {
		return trackedImages.get(file);
	}

	public void assignBallToFile(File file, Ball ball) {
		TrackingImage image = new TrackingImage(file, ball);
		trackedImages.put(file, image);
	}

}
