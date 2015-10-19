package de.kicker.tracking.model.balltracking;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import de.kicker.tracking.model.Position;
import de.kicker.tracking.model.TrackingImage;

public abstract class AbstractBallTracking implements IBallTracking {

	protected Map<Integer, TrackingImage> trackedImages;

	protected AbstractBallTracking() {
		this(new HashMap<Integer, TrackingImage>());
	}

	protected AbstractBallTracking(Map<Integer, TrackingImage> trackedImages) {
		setTrackedImages(trackedImages);
	}

	@Override
	public Collection<TrackingImage> getAllTrackedImages() {
		Collection<TrackingImage> images = new LinkedList<>();
		for (TrackingImage image : trackedImages.values()) {
			images.add(image);
		}
		return images;
	}

	@Override
	public Set<Integer> getTrackedIndizes() {
		return trackedImages.keySet();
	}

	@Override
	public TrackingImage getTrackingImage(int index) {
		return trackedImages.get(index);
	}

	@Override
	public void setTrackedImages(Map<Integer, TrackingImage> trackedImages) {
		this.trackedImages = trackedImages;
	}

	@Override
	public void assignBallToFile(int index, File file, Position position) {
		TrackingImage image = new TrackingImage(file, position);
		trackedImages.put(index, image);
	}

}
