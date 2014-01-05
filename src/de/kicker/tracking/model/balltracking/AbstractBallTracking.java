package de.kicker.tracking.model.balltracking;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import de.kicker.tracking.model.Ball;
import de.kicker.tracking.model.TrackingImage;

public abstract class AbstractBallTracking {

	protected Map<Integer, TrackingImage> trackedImages;

	protected AbstractBallTracking() {
		this(new HashMap<Integer, TrackingImage>());
	}

	protected AbstractBallTracking(Map<Integer, TrackingImage> trackedImages) {
		this.trackedImages = trackedImages;
	}

	public Collection<TrackingImage> getAllTrackedImages() {
		Collection<TrackingImage> images = new LinkedList<>();
		for (TrackingImage image : trackedImages.values()) {
			images.add(image);
		}
		return images;
	}

	public Set<Integer> getTrackedIndizes() {
		return trackedImages.keySet();
	}

	public TrackingImage getTrackingImage(int index) {
		return trackedImages.get(index);
	}

	public void assignBallToFile(int index, File file, Ball ball) {
		TrackingImage image = new TrackingImage(file, ball);
		trackedImages.put(index, image);
	}

}
