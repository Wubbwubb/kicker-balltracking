package de.kicker.tracking.model.balltracking;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.kicker.tracking.model.TrackingImage;

public abstract class AbstractBallTracking {

	protected String directory;

	protected Map<File, TrackingImage> trackedImages;

	protected AbstractBallTracking(String directory) {
		this(directory, new HashMap<File, TrackingImage>());
	}

	protected AbstractBallTracking(String directory, Map<File, TrackingImage> trackedImages) {
		this.directory = directory;
		this.trackedImages = trackedImages;
	}

	public Collection<TrackingImage> getAllTrackedImages() {
		Collection<TrackingImage> images = new LinkedList<>();
		for (TrackingImage image : trackedImages.values()) {
			images.add(image);
		}
		return images;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public TrackingImage getTrackingImage(File file) {
		return trackedImages.get(file);
	}

	public abstract void trackFile(File file);

}
