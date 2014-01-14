package de.kicker.tracking.model.balltracking;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import de.kicker.tracking.model.Ball;
import de.kicker.tracking.model.BallShape;
import de.kicker.tracking.model.Position;
import de.kicker.tracking.model.TrackingImage;
import de.kicker.tracking.model.xml.BallTrackingType;
import de.kicker.tracking.util.AWTUtil;

@BallTrackingType(value = "auto")
public class AutomaticBallTracking extends AbstractBallTracking {

	private static final Logger logger = Logger.getLogger(AutomaticBallTracking.class);

	private static final int X_MIN = 88;
	private static final int X_MAX = 560;
	private static final int Y_MIN = 106;
	private static final int Y_MAX = 378;

	private static final int FAIL_THRESHOLD = 10;

	private int searchFails;

	public AutomaticBallTracking() {
		super();
		searchFails = 0;
	}

	public AutomaticBallTracking(Map<Integer, TrackingImage> trackedImages) {
		super(trackedImages);
	}

	public void trackFile(int index, File file, BallShape ballShape) {
		Position position = calculatePosition(index, file, ballShape);
		logger.debug("calculated position: " + position.toString());
		Ball ball = new Ball(position, ballShape);
		assignBallToFile(index, file, ball);
	}

	private Position calculatePosition(int index, File file, BallShape ballShape) {
		Random r = new Random();
		int x = r.nextInt(X_MAX - X_MIN) + X_MIN;
		int y = r.nextInt(Y_MAX - Y_MIN) + Y_MIN;
		Position position = new Position(x, y);

		try {

			TrackingImage preTrackingImage = getTrackingImage(index - 1);
			Position prePosition = preTrackingImage.getBall().getPosition();

			// BufferedImage preImage =
			// AWTUtil.getImageFromFile(preTrackingImage.getFile());
			BufferedImage actImage = AWTUtil.getImageFromFile(file);
			// BufferedImage diffImage = AWTUtil.getDifferenceImage(preImage,
			// actImage);
			BufferedImage negImage = AWTUtil.getNegativeImage(actImage, ballShape.getAWTColor(), 40);

			// File debugFile = AWTUtil.getOutputFile(index, "negative");
			// AWTUtil.writeImageToFile(negImage, debugFile);

			boolean[][] bools = AWTUtil.getWhiteBooleans(negImage);

			if (searchFails > FAIL_THRESHOLD) {
				logger.debug("reset prePosition");
				prePosition = resetPrePosition(bools, prePosition);
				if (prePosition == null) {
					logger.debug("reset failed!");
					prePosition = preTrackingImage.getBall().getPosition();
				}
			}

			if (Color.WHITE.equals(AWTUtil.getColor(negImage, prePosition))) {

				logger.debug("detect position around prePosition");
				searchFails = 0;
				return getPositionAroundPrePosition(bools, prePosition);

			} else {

				logger.debug("prePosition is not marked");
				Position nextBallPosition = findNextBallPosition(bools, prePosition);
				logger.debug("next position: " + position.toString());
				if (nextBallPosition != null) {
					searchFails = 0;
					return getPositionAroundPrePosition(bools, nextBallPosition);
				}

			}

			searchFails++;
			logger.debug("fail " + searchFails);
			return prePosition;

		} catch (Exception e) {
			logger.error("error in calculatePosition at index: " + index, e);
		}

		return position;
	}

	private Position resetPrePosition(boolean[][] negImage, Position prePosition) {

		int initialX = prePosition.getX();
		int initialY = prePosition.getY();

		int k = 1;
		while (k <= 500) {
			int maxX = Math.min(k, X_MAX - initialX);
			int maxY = Math.min(k, Y_MAX - initialY);
			for (int x = Math.max(-k, X_MIN - initialX); x <= maxX; x++) {
				for (int y = Math.max(-k, Y_MIN - initialY); y <= maxY; y++) {
					if (Math.abs(x) != k && Math.abs(y) != k) {
						if (y != maxY) {
							y = maxY - 1;
						}
						continue;
					}

					Position p = new Position(initialX + x, initialY + y);
					if (negImage[p.getX()][p.getY()]) {
						if (isBallIndicator(negImage, p)) {
							return p;
						}
					}
				}
			}
			k++;
		}

		return null;
	}

	private boolean isBallIndicator(boolean[][] negImage, Position prePosition) {

		int initialX = prePosition.getX();
		int initialY = prePosition.getY();

		for (int x = Math.max(-1, X_MIN - initialX); x <= Math.min(1, X_MAX - initialX); x++) {
			for (int y = Math.max(-1, Y_MIN - initialY); y <= Math.min(1, Y_MAX - initialY); y++) {
				if (Math.abs(x) != 1 && Math.abs(y) != 1) {
					y = 0;
					continue;
				}

				Position p = new Position(initialX + x, initialY + y);
				if (negImage[p.getX()][p.getY()]) {
					return true;
				}
			}
		}

		return false;
	}

	private Position findNextBallPosition(boolean[][] negImage, Position prePosition) {

		int initialX = prePosition.getX();
		int initialY = prePosition.getY();

		int k = 1;
		while (k <= 25) {
			int maxX = Math.min(k, X_MAX - initialX);
			int maxY = Math.min(k, Y_MAX - initialY);
			for (int x = Math.max(-k, X_MIN - initialX); x <= maxX; x++) {
				for (int y = Math.max(-k, Y_MIN - initialY); y <= maxY; y++) {
					if (Math.abs(x) != k && Math.abs(y) != k) {
						if (y != maxY) {
							y = maxY - 1;
						}
						continue;
					}

					Position p = new Position(initialX + x, initialY + y);
					if (negImage[p.getX()][p.getY()]) {
						return p;
					}
				}
			}
			k++;
		}

		return null;
	}

	public Position getPositionAroundPrePosition(boolean[][] negImage, Position prePosition) {

		int initialX = prePosition.getX();
		int initialY = prePosition.getY();

		int xMin = initialX;
		int xMax = initialX;
		int yMin = initialY;
		int yMax = initialY;

		boolean update = true;
		int k = 1;
		while (update) {
			update = false;
			int startX = Math.max(-k, X_MIN - initialX);
			int startY = Math.max(-k, Y_MIN - initialY);
			int maxX = Math.min(k, X_MAX - initialX);
			int maxY = Math.min(k, Y_MAX - initialY);
			for (int x = startX; x <= maxX; x++) {
				for (int y = startY; y <= maxY; y++) {
					if (Math.abs(x) != k && Math.abs(y) != k) {
						if (y != maxY) {
							y = maxY - 1;
						}
						continue;
					}

					Position p = new Position(initialX + x, initialY + y);
					if (negImage[p.getX()][p.getY()]) {
						if (xMin > p.getX()) {
							xMin = p.getX();
							update = true;
						}
						if (yMin > p.getY()) {
							yMin = p.getY();
							update = true;
						}
						if (xMax < p.getX()) {
							xMax = p.getX();
							update = true;
						}
						if (yMax < p.getY()) {
							yMax = p.getY();
							update = true;
						}
					}
				}
			}
			k++;
		}

		return new Position((xMax + xMin) / 2, (yMax + yMin) / 2);
	}
}
