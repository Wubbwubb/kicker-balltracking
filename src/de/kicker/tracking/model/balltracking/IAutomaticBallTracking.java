package de.kicker.tracking.model.balltracking;

import java.io.File;

import de.kicker.tracking.model.BallShape;

public interface IAutomaticBallTracking extends IBallTracking {

	/**
	 * 
	 * @param index
	 * @param file
	 * @param ballShape
	 */
	public void trackFile(int index, File file, BallShape ballShape);

}
