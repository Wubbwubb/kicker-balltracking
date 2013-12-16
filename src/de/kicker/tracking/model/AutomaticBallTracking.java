package de.kicker.tracking.model;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class AutomaticBallTracking implements BallTracking {

	private File[] files;
	private String directory;
	private int currentIndex;

	private Map<File, TrackingImage> trackedImages;

	public AutomaticBallTracking(File[] files, String directory, int index) {
		this.files = files;
		currentIndex = index;
		trackedImages = new HashMap<>(files.length);
		this.directory = directory;
	}

	@Override
	public void trackNext() {

		if (endOfFiles()) {
			return;
		}

		Random r = new Random();
		int x = r.nextInt(492) + 68;
		int y = r.nextInt(283) + 77;
		Position pos = new Position(x, y);
		Ball ball = new Ball(pos);
		TrackingImage image = new TrackingImage(files[currentIndex], ball);
		trackedImages.put(files[currentIndex], image);
		currentIndex++;
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
	public String getDirectory() {
		return directory;
	}

	@Override
	public boolean endOfFiles() {
		return currentIndex >= files.length;
	}

}
