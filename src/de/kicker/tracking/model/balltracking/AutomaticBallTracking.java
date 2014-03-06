package de.kicker.tracking.model.balltracking;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import de.kicker.tracking.model.BallShape;
import de.kicker.tracking.model.Position;
import de.kicker.tracking.model.TrackingImage;
import de.kicker.tracking.model.settings.Settings;
import de.kicker.tracking.model.xml.BallTrackingColor;
import de.kicker.tracking.model.xml.BallTrackingType;
import de.kicker.tracking.util.AWTUtil;

@BallTrackingType(value = "auto")
@BallTrackingColor(rgb = -65536)
public class AutomaticBallTracking extends AbstractBallTracking implements IAutomaticBallTracking {

	private static final Logger logger = Logger.getLogger(AutomaticBallTracking.class);
	private static final Settings settings = Settings.getInstance();

	private static final int X_MIN = 88;
	private static final int X_MAX = 560;
	private static final int Y_MIN = 106;
	private static final int Y_MAX = 378;

	private int searchFails = 0;

	public AutomaticBallTracking() {
		super();
	}

	public AutomaticBallTracking(Map<Integer, TrackingImage> trackedImages) {
		super(trackedImages);
	}

	@Override
	public void trackFile(int index, File file, BallShape ballShape) {
		Position position = calculatePosition(index, file, ballShape);
		logger.debug("calculated position: " + position.toString());
		assignBallToFile(index, file, position);
	}

	private Position calculatePosition(int index, File file, BallShape ballShape) {
		Random r = new Random();
		int x = r.nextInt(X_MAX - X_MIN) + X_MIN;
		int y = r.nextInt(Y_MAX - Y_MIN) + Y_MIN;
		Position position = new Position(x, y);

		try {

			TrackingImage preTrackingImage = getTrackingImage(index - 1);
			Position prePosition = getValidePrePosition(index);

			BufferedImage preImage = AWTUtil.getImageFromFile(preTrackingImage.getFile());
			BufferedImage actImage = AWTUtil.getImageFromFile(file);

			BufferedImage binaryImage = AWTUtil.getBinaryImage(actImage, ballShape.getAWTColor(),
					settings.getMaxColorDistance());

			if (settings.createDebugImages()) {

				BufferedImage diffImage = AWTUtil.getDifferenceImage(preImage, actImage);
				AWTUtil.writeImageToFile(diffImage, AWTUtil.getOutputFile(index, "diff"));

				AWTUtil.writeImageToFile(binaryImage, AWTUtil.getOutputFile(index, "negative"));

				BufferedImage diff1 = AWTUtil.getDifferenceImage(preImage, ballShape.getAWTColor());
				BufferedImage diff2 = AWTUtil.getDifferenceImage(actImage, ballShape.getAWTColor());

				BufferedImage diffDiff = AWTUtil.getDifferenceImage(diff1, diff2);
				AWTUtil.writeImageToFile(diffDiff, AWTUtil.getOutputFile(index, "diffDiff"));
			}

			boolean[][] bools = AWTUtil.getWhiteBooleans(binaryImage);

			if (searchFails >= settings.getSearchFailThreshold() || prePosition.isNotFound()) {
				logger.debug("reset prePosition");
				prePosition = resetPrePosition(bools, prePosition);
				if (prePosition.isNotFound()) {
					logger.debug("reset failed!");
					searchFails++;
					logger.debug("fail " + searchFails);
					return Position.POSITION_NOT_FOUND;
				}
			}

			if (Color.WHITE.equals(AWTUtil.getColor(binaryImage, prePosition))) {

				logger.debug("detect position around prePosition");
				searchFails = 0;
				return getPositionAroundPrePosition(bools, prePosition);

			} else {

				logger.debug("prePosition is not marked");
				Position nextBallPosition = findNextBallPosition(bools, prePosition);
				if (!nextBallPosition.isNotFound()) {
					logger.debug("next position: " + nextBallPosition.toString());
					searchFails = 0;
					return getPositionAroundPrePosition(bools, nextBallPosition);
				}

			}

			searchFails++;
			logger.debug("fail " + searchFails);
			return Position.POSITION_NOT_FOUND;

		} catch (Exception e) {
			logger.error("error in calculatePosition at index: " + index, e);
		}

		return position;
	}

	private Position getValidePrePosition(int index) {
		Position prePosition = Position.POSITION_NOT_FOUND;
		for (index--; index >= 0; index--) {
			TrackingImage trackedImage = getTrackingImage(index);
			if (trackedImage == null) {
				break;
			}
			if (!trackedImage.getPosition().isNotFound()) {
				prePosition = trackedImage.getPosition();
				break;
			}
		}
		return prePosition;
	}

	private Position resetPrePosition(boolean[][] negImage, Position prePosition) {

		int initialX = prePosition.isNotFound() ? X_MAX - X_MIN : prePosition.getX();
		int initialY = prePosition.isNotFound() ? Y_MAX - Y_MIN : prePosition.getY();

		int k = 1;
		while (true) {
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
						if (isBallIndicator(negImage, p)) {
							return p;
						}
					}
				}
			}
			if (startX == X_MIN - initialX && maxX == X_MAX - initialX && startY == Y_MIN - initialY
					&& maxY == Y_MAX - initialY) {
				break;
			}
			k++;
		}

		return Position.POSITION_NOT_FOUND;
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
		while (k <= settings.getRadiusSearchSmall()) {
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

		return Position.POSITION_NOT_FOUND;
	}

	private Position getPositionAroundPrePosition(boolean[][] negImage, Position prePosition) {

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
