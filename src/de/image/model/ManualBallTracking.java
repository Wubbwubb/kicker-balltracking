package de.image.model;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ManualBallTracking implements BallTracking {

	private File[] files;
	private String directory;
	private int currentIndex;

	private Map<File, TrackingImage> trackedImages;

	public ManualBallTracking(File[] files, String directory, int index) {
		this.files = files;
		currentIndex = index;
		trackedImages = new HashMap<>(files.length);
		this.directory = directory;
	}

	@Override
	public String getDirectory() {
		return directory;
	}

	@Override
	public void trackNext() {
		
	}

	@Override
	public boolean endOfFiles() {
		return currentIndex >= files.length;
	}

	@Override
	public Collection<TrackingImage> getAllTrackedImages() {
		Collection<TrackingImage> images = new LinkedList<>();
		for (TrackingImage image : trackedImages.values()) {
			images.add(image);
		}
		return images;
	}

}
