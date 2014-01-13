package de.kicker.tracking.model.balltracking;

import java.io.File;

import org.apache.log4j.Logger;

import de.kicker.tracking.model.Ball;
import de.kicker.tracking.model.BallShape;
import de.kicker.tracking.model.Position;
import de.kicker.tracking.model.xml.XMLLayer;

public class TrackingFactory {

	private static final Logger logger = Logger.getLogger(TrackingFactory.class);

	private String directory;
	private BallShape ballShape;
	private int initialIndex;
	private int currentIndex;

	public AutomaticBallTracking autoBallTracking;
	public ManualBallTracking manualBallTracking;

	public TrackingFactory(String directory, BallShape ballShape, int initialIndex, int currentIndex) {
		autoBallTracking = new AutomaticBallTracking();
		manualBallTracking = new ManualBallTracking();
		setDirectory(directory);
		setBallShape(null);
		setInitialIndex(initialIndex);
		setCurrentIndex(currentIndex);
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

	public void initTracking(int index, File file, Position position) {
		if (getBallShape() != null) {
			Ball ball = new Ball(position, getBallShape());
			autoBallTracking.assignBallToFile(index, file, ball);
			manualBallTracking.assignBallToFile(index, file, ball);
			setInitialIndex(index);
			setCurrentIndex(index);
		} else {
			logger.warn("BallShape element of TrackingFactory is null!");
		}
	}

	public void trackAuto(int index, File file) {
		if (getBallShape() != null) {
			if (index == getCurrentIndex() + 1) {
				autoBallTracking.trackFile(index, file, getBallShape());
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
		if (getBallShape() != null) {
			manualBallTracking.assignBallToFile(index, file, new Ball(position, getBallShape()));
		} else {
			logger.warn("BallShape element of TrackingFactory is null!");
		}
	}

	public int trackAll(File[] files) {
		int index = getInitialIndex();
		setCurrentIndex(index);
		for (; index < files.length - 1;) {
			index++;
			trackAuto(index, files[index]);
		}
		return index;
	}

	public void exportToXML(File file) {
		if (getBallShape() != null) {
			XMLLayer.exportToXML(this, file.getAbsolutePath());
		} else {
			logger.warn("BallShape element of TrackingFactory is null!");
		}
	}

	public static TrackingFactory importFromXML(File file) {
		TrackingFactory factory = XMLLayer.readBallTracking(file);
		return factory;
	}

}
