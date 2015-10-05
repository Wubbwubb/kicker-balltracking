package de.kicker.tracking.test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageTest {

	public static void main(String[] args) throws IOException {

		File orgFile = new File("images\\TestImage_org.png");
		File orgFile2 = new File("images\\TestImage_org2.png");
		File newFile = new File("images\\TestImage_gray3.png");

		BufferedImage orgImage = ImageIO.read(orgFile);

		BufferedImage grayImage = new BufferedImage(orgImage.getWidth(), orgImage.getHeight(),
				BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = grayImage.getGraphics();
		g.drawImage(orgImage, 0, 0, null);
		g.dispose();

		Color[][] colors = new Color[grayImage.getWidth()][grayImage.getHeight()];
		for (int i = 0; i < colors.length; i++) {
			for (int j = 0; j < colors[0].length; j++) {
				colors[i][j] = getColor(grayImage, new Point(i, j));
			}
		}

		BufferedImage orgImage2 = ImageIO.read(orgFile2);

		BufferedImage grayImage2 = new BufferedImage(orgImage2.getWidth(), orgImage2.getHeight(),
				BufferedImage.TYPE_BYTE_GRAY);
		Graphics g2 = grayImage2.getGraphics();
		g2.drawImage(orgImage2, 0, 0, null);
		g2.dispose();

		for (int i = 0; i < colors.length; i++) {
			for (int j = 0; j < colors[0].length; j++) {
				Color temp1 = colors[i][j];
				Color temp2 = getColor(grayImage2, new Point(i, j));
				grayImage2.setRGB(
						i,
						j,
						new Color(Math.abs(temp1.getRed() - temp2.getRed()), Math.abs(temp1.getGreen()
								- temp2.getGreen()), Math.abs(temp1.getBlue() - temp2.getBlue())).getRGB());
			}
		}

		FileOutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream(newFile);
			ImageIO.write(grayImage2, "png", fileOut);
			fileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static Color getColor(BufferedImage image, Point point) {
		int rgb = image.getRGB(point.x, point.y);
		Color c = new Color(rgb);
		return c;
	}

}
