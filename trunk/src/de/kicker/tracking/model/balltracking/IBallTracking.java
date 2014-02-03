package de.kicker.tracking.model.balltracking;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import de.kicker.tracking.model.Position;
import de.kicker.tracking.model.TrackingImage;

public interface IBallTracking {

	/**
	 * Returns all TrackedImages of this BallTracking object.
	 * 
	 * @return all TrackedImages
	 */
	public Collection<TrackingImage> getAllTrackedImages();

	/**
	 * Returns a Set of all indizes of the TrackedImages.
	 * 
	 * @return
	 */
	public Set<Integer> getTrackedIndizes();

	/**
	 * Returns the TrackedImage with the given index. If the given index is not
	 * tracked the method returns null.
	 * 
	 * @param index
	 *            - index of the desired TrackedImage
	 * @return the TrackedImage at the given index
	 */
	public TrackingImage getTrackingImage(int index);

	/**
	 * 
	 * @param trackedImages
	 */
	public void setTrackedImages(Map<Integer, TrackingImage> trackedImages);

	/**
	 * 
	 * @param index
	 * @param file
	 * @param ball
	 */
	public void assignBallToFile(int index, File file, Position position);

}
