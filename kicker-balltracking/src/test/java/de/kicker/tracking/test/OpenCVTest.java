package de.kicker.tracking.test;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;

public class OpenCVTest {

	public static void main(String[] args) {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		CascadeClassifier faceDetector = new CascadeClassifier("test/haarcascade_frontalface_alt.xml");
		Mat image = Highgui.imread("D:/Bilder/Bilder Oktoberfest 2013/IMG_4849.JPG");

		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);

		System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

		for (Rect rect : faceDetections.toArray()) {
			Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(0, 255, 0));
		}

		String filename = "test/ouput.png";
		System.out.println(String.format("Writing %s", filename));
		Highgui.imwrite(filename, image);

	}

}
