package de.kicker.tracking.test;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import de.kicker.tracking.util.AWTUtil;

public class Testify {

	public static void main(String[] args) {

		File file1 = new File("E:/Praktikum Master/Bilder_orange/PWV-0041_orange00560.png");
		File file2 = new File("E:/Praktikum Master/Bilder_orange/PWV-0041_orange00561.png");
		File outFile1 = new File("test/diff.png");
		File outFile2 = new File("test/diff1.png");
		File outFile3 = new File("test/diff2.png");
		File outFile4 = new File("test/negDiff.png");

		Color color = new Color(253, 67, 0);

		try {

			BufferedImage image1 = AWTUtil.getImageFromFile(file1);
			BufferedImage image2 = AWTUtil.getImageFromFile(file2);

			BufferedImage diff1 = AWTUtil.getDifferenceImage(image1, color);
			BufferedImage diff2 = AWTUtil.getDifferenceImage(image2, color);

			BufferedImage diff = AWTUtil.getDifferenceImage(diff1, diff2);

			BufferedImage negDiff = AWTUtil.getBinaryImage(diff, color, 150, 88, 106, 560, 378);

			AWTUtil.writeImageToFile(diff, outFile1);
			AWTUtil.writeImageToFile(diff1, outFile2);
			AWTUtil.writeImageToFile(diff2, outFile3);
			AWTUtil.writeImageToFile(negDiff, outFile4);

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(Color.BLUE.getRGB());

	}

}
