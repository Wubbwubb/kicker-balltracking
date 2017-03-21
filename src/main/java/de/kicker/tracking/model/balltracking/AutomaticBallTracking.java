package de.kicker.tracking.model.balltracking;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

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
		this.xMin = settings.getLeftBound();
		if (this.xMin < 0) {
			this.xMin = 0;
		}
		this.xMax = settings.getRightBound();
		if (this.xMax < 0) {
			this.xMax = settings.getImageWidth();
		}
		this.yMin = settings.getTopBound();
		if (this.yMin < 0) {
			this.yMin = 0;
		}
		this.yMax = settings.getBottomBound();
		if (this.yMax < 0) {
			this.yMax = settings.getImageHeight();
		}
	}

	public AutomaticBallTracking(Map<Integer, TrackingImage> trackedImages) {
		super(trackedImages);
		this.xMin = settings.getLeftBound();
		if (this.xMin < 0) {
			this.xMin = 0;
		}
		this.xMax = settings.getRightBound();
		if (this.xMax < 0) {
			this.xMax = settings.getImageWidth();
		}
		this.yMin = settings.getTopBound();
		if (this.yMin < 0) {
			this.yMin = 0;
		}
		this.yMax = settings.getBottomBound();
		if (this.yMax < 0) {
			this.yMax = settings.getImageHeight();
		}
	}

	@Override
	public void trackFile(int index, File file, BallShape ballShape) {
		Position position = calculatePosition(index, file, ballShape);
		logger.debug("calculated position: " + position.toString());
		assignBallToFile(index, file, position);
	}

	private Position calculatePosition(int index, File file, BallShape ballShape) {
		try {
			TrackingImage preTrackingImage = getTrackingImage(index - 1);
			Position prePosition = getValidePrePosition(index);

			BufferedImage preImage = AWTUtil.getImageFromFile(preTrackingImage.getFile());
			BufferedImage actImage = AWTUtil.getImageFromFile(file);

			BufferedImage binaryImage = AWTUtil.getBinaryImage(actImage, ballShape.getAWTColor(), settings.getMaxColorDistance()
					, this.xMin, this.yMin, this.xMax, this.yMax);

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

		} catch (Exception e) {
			logger.error("error in calculatePosition at index: " + index, e);
		}

		searchFails++;
		logger.debug("fail " + searchFails);
		return Position.POSITION_NOT_FOUND;
	}

	private Position getValidePrePosition(int index) {
		Position prePosition = Position.POSITION_NOT_FOUND;
		for (index--; index >= 0; index--) {
			TrackingImage trackedImage = getTrackingImage(index);
			if (trackedImage == null) {
				break;
			}
			if (!trackedImage.getPosition().isNotFound()) {
				return trackedImage.getPosition();
			}
		}
		return prePosition;
	}

	private Position resetPrePosition(boolean[][] negImage, Position prePosition) {

		int initialX = prePosition.isNotFound() ? this.xMax - this.xMin : prePosition.getX();
		int initialY = prePosition.isNotFound() ? this.yMax - this.yMin : prePosition.getY();

		int k = 1;
		while (true) {
			int startX = Math.max(-k, this.xMin - initialX);
			int startY = Math.max(-k, this.yMin - initialY);
			int maxX = Math.min(k, this.xMax - initialX);
			int maxY = Math.min(k, this.yMax - initialY);
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
			if (startX == this.xMin - initialX && maxX == this.xMax - initialX && startY == this.yMin - initialY && maxY == this.yMax - initialY) {
				break;
			}
			k++;
		}

		return Position.POSITION_NOT_FOUND;
	}

	private boolean isBallIndicator(boolean[][] negImage, Position prePosition) {

		int initialX = prePosition.getX();
		int initialY = prePosition.getY();

		for (int x = Math.max(-1, this.xMin - initialX); x <= Math.min(1, this.xMax - initialX); x++) {
			for (int y = Math.max(-1, this.yMin - initialY); y <= Math.min(1, this.yMax - initialY); y++) {
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
			int maxX = Math.min(k, this.xMax - initialX);
			int maxY = Math.min(k, this.yMax - initialY);
			for (int x = Math.max(-k, this.xMin - initialX); x <= maxX; x++) {
				for (int y = Math.max(-k, this.yMin - initialY); y <= maxY; y++) {
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

		int tmpXMin = initialX;
		int tmpXMax = initialX;
		int tmpYMin = initialY;
		int tmpYMax = initialY;

		boolean update = true;
		int k = 1;
		while (update) {
			update = false;
			int startX = Math.max(-k, this.xMin - initialX);
			int startY = Math.max(-k, this.yMin - initialY);
			int maxX = Math.min(k, this.xMax - initialX);
			int maxY = Math.min(k, this.yMax - initialY);
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
						if (tmpXMin > p.getX()) {
							tmpXMin = p.getX();
							update = true;
						}
						if (tmpYMin > p.getY()) {
							tmpYMin = p.getY();
							update = true;
						}
						if (tmpXMax < p.getX()) {
							tmpXMax = p.getX();
							update = true;
						}
						if (tmpYMax < p.getY()) {
							tmpYMax = p.getY();
							update = true;
						}
					}
				}
			}
			k++;
		}

		return new Position((tmpXMax + tmpXMin) / 2, (tmpYMax + tmpYMin) / 2);
	}
}
