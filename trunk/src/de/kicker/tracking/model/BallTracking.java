package de.kicker.tracking.model;

import java.util.Collection;

public interface BallTracking {

	public String getDirectory();

	public void trackNext();

	public boolean endOfFiles();
	
	public boolean isTracked();

	public Collection<TrackingImage> getAllTrackedImages();

}
