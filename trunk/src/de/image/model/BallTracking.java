package de.image.model;

import java.util.Collection;

public interface BallTracking {

	public String getDirectory();

	public void trackNext();

	public boolean endOfFiles();

	public Collection<TrackingImage> getAllTrackedImages();

}
