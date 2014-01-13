package de.kicker.tracking.model.balltracking;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import de.kicker.tracking.model.Ball;
import de.kicker.tracking.model.BallShape;
import de.kicker.tracking.model.Position;
import de.kicker.tracking.model.TrackingImage;
import de.kicker.tracking.model.xml.XMLType;

@XMLType(value = "auto")
public class AutomaticBallTracking extends AbstractBallTracking {

	private static final Logger logger = Logger.getLogger(AutomaticBallTracking.class);

	public AutomaticBallTracking() {
		super();
	}

	public AutomaticBallTracking(Map<Integer, TrackingImage> trackedImages) {
		super(trackedImages);
	}

	public void trackFile(int index, File file, BallShape ballShape) {
		Position position = calculatePosition(index, file, ballShape);
		Ball ball = new Ball(position, ballShape);
		assignBallToFile(index, file, ball);
	}

	private Position calculatePosition(int index, File file, BallShape ballShape) {
		Random r = new Random();
		int x = r.nextInt(492) + 68;
		int y = r.nextInt(283) + 77;
		Position position = new Position(x, y);

		File outFile = getOutputFile(index);
		TrackingImage preTrackingImage = getTrackingImage(index - 1);
		Position prePosition = preTrackingImage.getBall().getPosition();

		try {

			BufferedImage preImage = getImageFromFile(preTrackingImage.getFile());
			BufferedImage actImage = getImageFromFile(file);
			BufferedImage diffImage = getDifferenceImage(preImage, actImage);

			writeImageToFile(diffImage, outFile);

		} catch (Exception e) {
			logger.error("error in calculatePosition at index: " + index, e);
		}

		return position;
	}

	private File getOutputFile(int index) {

		String indexString = "";
		for (int length = String.valueOf(index).length(); length < 4; length++) {
			indexString += 0;
		}
		indexString += index;
		return new File("E:/Praktikum Master/diffTest/diffImage_" + indexString + ".png");
	}

	private BufferedImage getDifferenceImage(BufferedImage preImage, BufferedImage actImage) {

		BufferedImage diffImage = new BufferedImage(preImage.getWidth(), preImage.getHeight(),
				BufferedImage.TYPE_BYTE_GRAY);

		for (int i = 0; i < diffImage.getWidth(); i++) {
			for (int j = 0; j < diffImage.getHeight(); j++) {
				Point p = new Point(i, j);
				Color temp1 = getColor(preImage, p);
				Color temp2 = getColor(actImage, p);
				Color diffColor = new Color(Math.abs(temp1.getRed() - temp2.getRed()), Math.abs(temp1.getGreen()
						- temp2.getGreen()), Math.abs(temp1.getBlue() - temp2.getBlue()));
				diffImage.setRGB(i, j, diffColor.getRGB());
			}
		}

		return diffImage;
	}

	private BufferedImage getImageFromFile(File file) throws IOException {
		return ImageIO.read(file);
	}

	private void writeImageToFile(BufferedImage image, File file) throws IOException {
		ImageIO.write(image, "png", file);
	}

	private Color getColor(BufferedImage image, Point point) {
		return new Color(image.getRGB(point.x, point.y));
	}

}
