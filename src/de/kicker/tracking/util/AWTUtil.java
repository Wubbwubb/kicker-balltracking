package de.kicker.tracking.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.kicker.tracking.model.Position;
import de.kicker.tracking.model.settings.Settings;

public final class AWTUtil {

	private static final Settings settings = Settings.getInstance();

	private AWTUtil() {
	}

	public static File getOutputFile(int index, String bez) throws IOException {

		String indexString = "";
		for (int length = String.valueOf(index).length(); length < 4; length++) {
			indexString += 0;
		}
		indexString += index;
		File file = new File(settings.getDebugDirectory() + File.separator + bez + File.separator + bez + "_"
				+ indexString + ".png");
		file.mkdirs();
		return file;
	}

	public static void writeImageToFile(BufferedImage image, File file) throws IOException {
		ImageIO.write(image, "png", file);
	}

	public static BufferedImage getDifferenceImage(BufferedImage preImage, BufferedImage actImage) {

		BufferedImage diffImage = new BufferedImage(preImage.getWidth(), preImage.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		for (int i = 0; i < diffImage.getWidth(); i++) {
			for (int j = 0; j < diffImage.getHeight(); j++) {
				Position pos = new Position(i, j);
				Color temp1 = getColor(preImage, pos);
				Color temp2 = getColor(actImage, pos);
				Color diffColor = new Color(Math.abs(temp1.getRed() - temp2.getRed()), Math.abs(temp1.getGreen()
						- temp2.getGreen()), Math.abs(temp1.getBlue() - temp2.getBlue()));
				diffImage.setRGB(i, j, diffColor.getRGB());
			}
		}

		return diffImage;
	}

	public static BufferedImage getDifferenceImage(BufferedImage image, Color color) {

		BufferedImage diffImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

		for (int i = 0; i < diffImage.getWidth(); i++) {
			for (int j = 0; j < diffImage.getHeight(); j++) {
				Position pos = new Position(i, j);
				Color tempColor = getColor(image, pos);
				Color diffColor = new Color(Math.abs(tempColor.getRed() - color.getRed()), Math.abs(tempColor
						.getGreen() - color.getGreen()), Math.abs(tempColor.getBlue() - color.getBlue()));
				diffImage.setRGB(i, j, diffColor.getRGB());
			}
		}

		return diffImage;
	}

	public static BufferedImage getImageFromFile(File file) throws IOException {
		return ImageIO.read(file);
	}

	public static Color getColor(BufferedImage image, Position position) {
		return new Color(image.getRGB(position.getX(), position.getY()));
	}

	public static BufferedImage getBinaryImage(BufferedImage image, Color goalColor, double maxDistance) {

		BufferedImage binaryImage = new BufferedImage(image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_BYTE_GRAY);

		for (int i = 0; i < binaryImage.getWidth(); i++) {
			for (int j = 0; j < binaryImage.getHeight(); j++) {
				Position pos = new Position(i, j);
				Color orgColor = getColor(image, pos);
				Color newColor;
				if (colorMatches(orgColor, goalColor, maxDistance)) {
					newColor = Color.WHITE;
				} else {
					newColor = Color.BLACK;
				}
				binaryImage.setRGB(i, j, newColor.getRGB());
			}
		}

		return binaryImage;
	}

	public static boolean colorMatches(Color orgColor, Color goalColor, double maxDistance) {
		if (getColorDistance(orgColor, goalColor) <= maxDistance) {
			return true;
		}
		return false;
	}

	public static double getColorDistance(Color orgColor, Color goalColor) {
		double dRed = Math.abs(orgColor.getRed() - goalColor.getRed());
		double dGreen = Math.abs(orgColor.getGreen() - goalColor.getGreen());
		double dBlue = Math.abs(orgColor.getBlue() - goalColor.getBlue());
		return Math.sqrt(Math.pow(dRed, 2) + Math.pow(dGreen, 2) + Math.pow(dBlue, 2));
	}

	public static Color[][] getColorsOfImage(BufferedImage image) {
		Color[][] colors = new Color[image.getWidth()][image.getHeight()];
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				colors[i][j] = getColor(image, new Position(i, j));
			}
		}
		return colors;
	}

	public static boolean[][] getWhiteBooleans(BufferedImage image) {
		boolean[][] colors = new boolean[image.getWidth()][image.getHeight()];
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				colors[i][j] = Color.WHITE.equals(getColor(image, new Position(i, j)));
			}
		}
		return colors;
	}

}
