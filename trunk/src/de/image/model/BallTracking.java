package de.image.model;

import java.io.File;
import java.util.Map;

public class BallTracking {

	private File[] files;
	private int currentIndex;

	private Map<File, TrackingImage> trackedImages;
	private Map<File, TrackingImage> setImages;

	public BallTracking(File[] files, int index) {
		this.files = files;
		currentIndex = index;
	}

	public void trackIndex(int index) {

	}

}
