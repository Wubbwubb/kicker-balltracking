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
import de.kicker.tracking.model.xml.BallTrackingType;
import de.kicker.tracking.util.AWTUtil;

@BallTrackingType(value = "auto")
public class AutomaticBallTracking extends AbstractBallTracking implements IAutomaticBallTracking {

	private static final Logger logger = Logger.getLogger(AutomaticBallTracking.class);
	private static final Settings settings = Settings.getInstance();

	private int searchFails = 0;

	private int xMin;
	private int xMax;
	private int yMin;
	private int yMax;

	public AutomaticBallTracking() {
		super();
		this.xMin = Math.max(0, settings.getLeftBound());
		this.xMax = Math.max(0, settings.getRightBound());
		this.yMin = Math.max(0, settings.getTopBound());
		this.yMax = Math.max(0, settings.getBottomBound());
	}

	public AutomaticBallTracking(Map<Integer, TrackingImage> trackedImages) {
		super(trackedImages);
		this.xMin = Math.max(0, settings.getLeftBound());
		this.xMax = Math.max(0, settings.getRightBound());
		this.yMin = Math.max(0, settings.getTopBound());
		this.yMax = Math.max(0, settings.getBottomBound());
	}

	@Override
	public void trackFile(int index, File file, BallShape ballShape) {
		Position position = calculatePosition(index, file, ballShape);
		logger.debug("calculated position: " + position.toString());
		assignBallToFile(index, file, position);
	}

	private Position calculatePosition(int index, File file, BallShape ballShape) {
		Random r = new Random();
		int x = r.nextInt(xMax - xMin) + xMin;
		int y = r.nextInt(yMax - yMin) + yMin;
		Position position = new Position(x, y);

		try {

			TrackingImage preTrackingImage = getTrackingImage(index - 1);
			Position prePosition = getValidePrePosition(index);

			BufferedImage preImage = AWTUtil.getImageFromFile(preTrackingImage.getFile());
			BufferedImage actImage = AWTUtil.getImageFromFile(file);

			BufferedImage binaryImage = AWTUtil.getBinaryImage(actImage, ballShape.getAWTColor(), settings.getMaxColorDistance()
					, xMin, yMin, xMax, yMax);

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

		int initialX = prePosition.isNotFound() ? xMax - xMin : prePosition.getX();
		int initialY = prePosition.isNotFound() ? yMax - yMin : prePosition.getY();

		int k = 1;
		while (true) {
			int startX = Math.max(-k, xMin - initialX);
			int startY = Math.max(-k, yMin - initialY);
			int maxX = Math.min(k, xMax - initialX);
			int maxY = Math.min(k, yMax - initialY);
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
			if (startX == xMin - initialX && maxX == xMax - initialX && startY == yMin - initialY && maxY == yMax - initialY) {
				break;
			}
			k++;
		}

		return Position.POSITION_NOT_FOUND;
	}

	private boolean isBallIndicator(boolean[][] negImage, Position prePosition) {

		int initialX = prePosition.getX();
		int initialY = prePosition.getY();

		for (int x = Math.max(-1, xMin - initialX); x <= Math.min(1, xMax - initialX); x++) {
			for (int y = Math.max(-1, yMin - initialY); y <= Math.min(1, yMax - initialY); y++) {
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
			int maxX = Math.min(k, xMax - initialX);
			int maxY = Math.min(k, yMax - initialY);
			for (int x = Math.max(-k, xMin - initialX); x <= maxX; x++) {
				for (int y = Math.max(-k, yMin - initialY); y <= maxY; y++) {
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
			int startX = Math.max(-k, xMin - initialX);
			int startY = Math.max(-k, yMin - initialY);
			int maxX = Math.min(k, xMax - initialX);
			int maxY = Math.min(k, yMax - initialY);
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
