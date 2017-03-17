package de.kicker.tracking.model.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.scene.paint.Color;

import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.kicker.tracking.model.BallShape;
import de.kicker.tracking.model.Position;
import de.kicker.tracking.model.TrackingImage;
import de.kicker.tracking.model.balltracking.AutomaticBallTracking;
import de.kicker.tracking.model.balltracking.IAutomaticBallTracking;
import de.kicker.tracking.model.balltracking.ManualBallTracking;
import de.kicker.tracking.model.balltracking.TrackingFactory;

public final class XMLLayer {

	private static final Logger logger = Logger.getLogger(XMLLayer.class);

	private static final String ELEMENT_BALLTRACKING = "balltracking";
	private static final String ELEMENT_BALLSHAPE = "ballshape";
	private static final String ELEMENT_COLOR = "color";
	private static final String ELEMENT_IMAGE = "image";
	private static final String ELEMENT_POSITION = "position";

	private static final String ATTRIBUTE_DIRECTORY = "directory";
	private static final String ATTRIBUTE_RADIUS = "radius";
	private static final String ATTRIBUTE_RED = "red";
	private static final String ATTRIBUTE_GREEN = "green";
	private static final String ATTRIBUTE_BLUE = "blue";
	private static final String ATTRIBUTE_OPACITY = "opacity";
	private static final String ATTRIBUTE_INDEX = "index";
	private static final String ATTRIBUTE_FILE = "file";
	private static final String ATTRIBUTE_TYPE = "type";
	private static final String ATTRIBUTE_X = "x";
	private static final String ATTRIBUTE_Y = "y";

	private XMLLayer() {
	}

	public static void exportToXML(TrackingFactory trackingFactory, String file) {

		try {

			Element root = new Element(ELEMENT_BALLTRACKING);
			Document doc = new Document(root);

			Attribute dirAtt = new Attribute(ATTRIBUTE_DIRECTORY, trackingFactory.getDirectory());
			root.setAttribute(dirAtt);

			Element shapeEl = new Element(ELEMENT_BALLSHAPE);
			Attribute radiusAtt = new Attribute(ATTRIBUTE_RADIUS, trackingFactory.getBallShape().getRadius() + "");

			shapeEl.setAttribute(radiusAtt);

			Element colorEl = new Element(ELEMENT_COLOR);
			Attribute rAtt = new Attribute(ATTRIBUTE_RED, trackingFactory.getBallShape().getFXColor().getRed() + "");
			Attribute gAtt = new Attribute(ATTRIBUTE_GREEN, trackingFactory.getBallShape().getFXColor().getGreen() + "");
			Attribute bAtt = new Attribute(ATTRIBUTE_BLUE, trackingFactory.getBallShape().getFXColor().getBlue() + "");
			Attribute oAtt = new Attribute(ATTRIBUTE_OPACITY, trackingFactory.getBallShape().getFXColor().getOpacity()
					+ "");

			colorEl.setAttribute(rAtt);
			colorEl.setAttribute(gAtt);
			colorEl.setAttribute(bAtt);
			colorEl.setAttribute(oAtt);

			shapeEl.addContent(colorEl);
			root.addContent(shapeEl);

			String type = AutomaticBallTracking.class.getAnnotation(BallTrackingType.class).value();
			Set<Integer> indizes = trackingFactory.getAutomaticBallTracking().getTrackedIndizes();
			for (int index : indizes) {
				TrackingImage image = trackingFactory.getAutomaticBallTracking().getTrackingImage(index);

				Element imgEl = new Element(ELEMENT_IMAGE);

				Attribute indexAtt = new Attribute(ATTRIBUTE_INDEX, index + "");
				Attribute fileAtt = new Attribute(ATTRIBUTE_FILE, image.getFile().getName());
				Attribute typeAtt = new Attribute(ATTRIBUTE_TYPE, type);

				imgEl.setAttribute(indexAtt);
				imgEl.setAttribute(fileAtt);
				imgEl.setAttribute(typeAtt);

				Element posEl = new Element(ELEMENT_POSITION);

				Attribute xAtt = new Attribute(ATTRIBUTE_X, image.getPosition().getX() + "");
				Attribute yAtt = new Attribute(ATTRIBUTE_Y, image.getPosition().getY() + "");

				posEl.setAttribute(xAtt);
				posEl.setAttribute(yAtt);

				imgEl.addContent(posEl);

				root.addContent(imgEl);
			}

			type = ManualBallTracking.class.getAnnotation(BallTrackingType.class).value();
			indizes = trackingFactory.getManualBallTracking().getTrackedIndizes();
			for (int index : indizes) {
				TrackingImage image = trackingFactory.getManualBallTracking().getTrackingImage(index);

				Element imgEl = new Element(ELEMENT_IMAGE);

				Attribute indexAtt = new Attribute(ATTRIBUTE_INDEX, index + "");
				Attribute fileAtt = new Attribute(ATTRIBUTE_FILE, image.getFile().getName());
				Attribute typeAtt = new Attribute(ATTRIBUTE_TYPE, type);

				imgEl.setAttribute(indexAtt);
				imgEl.setAttribute(fileAtt);
				imgEl.setAttribute(typeAtt);

				Element posEl = new Element(ELEMENT_POSITION);

				Attribute xAtt = new Attribute(ATTRIBUTE_X, image.getPosition().getX() + "");
				Attribute yAtt = new Attribute(ATTRIBUTE_Y, image.getPosition().getY() + "");

				posEl.setAttribute(xAtt);
				posEl.setAttribute(yAtt);

				imgEl.addContent(posEl);

				root.addContent(imgEl);
			}

			if (!file.endsWith(".xml")) {
				file += ".xml";
			}
			FileOutputStream fileOutput = new FileOutputStream(file);

			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			outputter.output(doc, fileOutput);
			fileOutput.close();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	public static <T extends IAutomaticBallTracking> TrackingFactory readBallTracking(File xmlFile,
			Class<T> automaticClass) {

		// TODO: choose the SAXBuilder with DTD-validation
		// SAXBuilder builder = new SAXBuilder(XMLReaders.DTDVALIDATING);
		SAXBuilder builder = new SAXBuilder();

		TrackingFactory factory = null;

		try {
			String dir = null;
			Map<Integer, TrackingImage> autoTrackedImages = new HashMap<>();
			Map<Integer, TrackingImage> manualTrackedImages = new HashMap<>();

			Document doc = builder.build(xmlFile);
			Element root = doc.getRootElement();
			if (root == null || !ELEMENT_BALLTRACKING.equals(root.getName())) {
				throw new IOException(xmlFile + " has wrong structure!");
			}
			dir = root.getAttributeValue(ATTRIBUTE_DIRECTORY);

			Element shapeEl = root.getChild(ELEMENT_BALLSHAPE);
			int radius = Integer.parseInt(shapeEl.getAttributeValue(ATTRIBUTE_RADIUS));

			Element colorEl = shapeEl.getChild(ELEMENT_COLOR);
			double red = Double.parseDouble(colorEl.getAttributeValue(ATTRIBUTE_RED));
			double green = Double.parseDouble(colorEl.getAttributeValue(ATTRIBUTE_GREEN));
			double blue = Double.parseDouble(colorEl.getAttributeValue(ATTRIBUTE_BLUE));
			double opacity = Double.parseDouble(colorEl.getAttributeValue(ATTRIBUTE_OPACITY));
			BallShape ballShape = new BallShape(radius, new Color(red, green, blue, opacity));

			List<Element> images = root.getChildren(ELEMENT_IMAGE);

			BallTrackingType autoType = automaticClass.getAnnotation(BallTrackingType.class);
			BallTrackingType manualType = ManualBallTracking.class.getAnnotation(BallTrackingType.class);

			if (autoType == null) {
				logger.error("no BallTrackingType defined for " + automaticClass.getName());
			}

			int initialIndex = Integer.MAX_VALUE;
			int currentIndex = Integer.MIN_VALUE;

			for (Element imgEl : images) {

				int index = Integer.parseInt(imgEl.getAttributeValue(ATTRIBUTE_INDEX));
				File file = new File(dir + File.separator + imgEl.getAttributeValue(ATTRIBUTE_FILE));

				Element posEl = imgEl.getChild(ELEMENT_POSITION);
				int x = Integer.parseInt(posEl.getAttributeValue(ATTRIBUTE_X));
				int y = Integer.parseInt(posEl.getAttributeValue(ATTRIBUTE_Y));

				Position position = new Position(x, y);

				TrackingImage image = new TrackingImage(file, position);
				String type = imgEl.getAttributeValue(ATTRIBUTE_TYPE);

				if (autoType.value().equals(type)) {
					autoTrackedImages.put(index, image);
					currentIndex = Math.max(currentIndex, index);
				} else if (manualType.value().equals(type)) {
					manualTrackedImages.put(index, image);
				}

				initialIndex = Math.min(initialIndex, index);

			}

			T autoTracking = automaticClass.newInstance();
			autoTracking.setTrackedImages(autoTrackedImages);

			factory = new TrackingFactory(dir, ballShape, initialIndex, currentIndex);
			factory.setAutomaticBallTracking(autoTracking);
			factory.setManualBallTracking(new ManualBallTracking(manualTrackedImages));

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return factory;
	}
}
