package de.kicker.tracking.model.balltracking;

import java.io.File;

import org.apache.log4j.Logger;

import de.kicker.tracking.model.BallShape;
import de.kicker.tracking.model.Position;
import de.kicker.tracking.model.xml.XMLLayer;

public class TrackingFactory {

	private static final Logger logger = Logger.getLogger(TrackingFactory.class);

	private String directory;
	private BallShape ballShape;
	private int initialIndex;
	private int currentIndex;

	private IAutomaticBallTracking autoBallTracking;
	private ManualBallTracking manualBallTracking;

	public TrackingFactory(String directory, BallShape ballShape, int initialIndex, int currentIndex) {
		setAutomaticBallTracking(new AutomaticBallTracking());
		setManualBallTracking(new ManualBallTracking());
		setDirectory(directory);
		setBallShape(ballShape);
		setInitialIndex(initialIndex);
		setCurrentIndex(currentIndex);
	}

	public IAutomaticBallTracking getAutomaticBallTracking() {
		return autoBallTracking;
	}

	public void setAutomaticBallTracking(IAutomaticBallTracking ballTracking) {
		this.autoBallTracking = ballTracking;
	}

	public ManualBallTracking getManualBallTracking() {
		return manualBallTracking;
	}

	public void setManualBallTracking(ManualBallTracking ballTracking) {
		this.manualBallTracking = ballTracking;
	}

	public String getDirectory() {
		return this.directory;
	}

	public void setDirectory(String dir) {
		this.directory = dir;
	}

	public BallShape getBallShape() {
		return this.ballShape;
	}

	public void setBallShape(BallShape ballShape) {
		this.ballShape = ballShape;
	}

	public int getInitialIndex() {
		return initialIndex;
	}

	public void setInitialIndex(int initialIndex) {
		this.initialIndex = initialIndex;
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(int index) {
		this.currentIndex = index;
	}

	/**
	 * @param index
	 * @param file
	 * @param position
	 */
	public void initTracking(int index, File file, Position position) {
		autoBallTracking.assignBallToFile(index, file, position);
		manualBallTracking.assignBallToFile(index, file, position);
		setInitialIndex(index);
		setCurrentIndex(index);
	}

	public void trackAuto(int index, File file) {
		if (getBallShape() != null) {
			if (index == getCurrentIndex() + 1) {
				autoBallTracking.trackFile(index, file, getBallShape());
//				TrackingImage img = manualBallTracking.getTrackingImage(currentIndex);
//				if (img == null || img.getPosition() == null) {
//					trackManual(index, file, Position.POSITION_NOT_FOUND);
//				}
				setCurrentIndex(index);
			} else {
				logger.error("current index is " + currentIndex + ". you have to track " + (currentIndex + 1)
						+ " instead of " + index + "!");
			}
		} else {
			logger.warn("BallShape element of TrackingFactory is null!");
		}
	}

	public void trackManual(int index, File file, Position position) {
		manualBallTracking.assignBallToFile(index, file, position);
	}

	public void exportToXML(File file) {
		if (getBallShape() != null) {
			XMLLayer.exportToXML(this, file.getAbsolutePath());
		} else {
			logger.warn("BallShape element of TrackingFactory is null!");
		}
	}

	public static TrackingFactory importFromXML(File file, Class<? extends IAutomaticBallTracking> automaticClass) {
		return XMLLayer.readBallTracking(file, automaticClass);
	}

}
