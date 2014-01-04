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
	protected BallShape ballShape;

	public AutomaticBallTracking autoBallTracking;
	public ManualBallTracking manualBallTracking;

	public TrackingFactory(String directory, BallShape ballShape) {
		autoBallTracking = new AutomaticBallTracking();
		manualBallTracking = new ManualBallTracking();
		setDirectory(directory);
		setBallShape(null);
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

	public void initTracking(File file, Position position) {
		if (getBallShape() != null) {
			Ball ball = new Ball(position, getBallShape());
			autoBallTracking.assignBallToFile(file, ball);
			manualBallTracking.assignBallToFile(file, ball);
		} else {
			logger.warn("BallShape element of TrackingFactory is null!");
		}
	}

	public void trackAuto(File file) {
		if (getBallShape() != null) {
			autoBallTracking.trackFile(file, getBallShape());
		} else {
			logger.warn("BallShape element of TrackingFactory is null!");
		}
	}

	public void trackManual(File file, Position position) {
		if (getBallShape() != null) {
			manualBallTracking.assignBallToFile(file, new Ball(position, getBallShape()));
		} else {
			logger.warn("BallShape element of TrackingFactory is null!");
		}
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
