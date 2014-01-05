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
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.kicker.tracking.model.Ball;
import de.kicker.tracking.model.BallShape;
import de.kicker.tracking.model.Position;
import de.kicker.tracking.model.TrackingImage;
import de.kicker.tracking.model.balltracking.AutomaticBallTracking;
import de.kicker.tracking.model.balltracking.ManualBallTracking;
import de.kicker.tracking.model.balltracking.TrackingFactory;
import de.kicker.tracking.model.settings.Settings;

public class XMLLayer {

	private static Logger logger = Logger.getLogger(XMLLayer.class);
	private static Settings settings = Settings.getInstance();

	public static void main(String[] args) {
		String dir = "E:\\Praktikum Master\\Bilder";
		File dirFile = new File(dir);
		File[] files = dirFile.listFiles();
		TrackingFactory factory = new TrackingFactory(dir, null, 0, 0);
		int i = 0;
		for (File file : files) {
			factory.trackAuto(i++, file);
		}
		File f = new File("xml/test.xml");
		exportToXML(factory, f.getAbsolutePath());
		TrackingFactory t = readBallTracking(f);
		System.out.println(t.autoBallTracking.getAllTrackedImages().size());
	}

	public static void exportToXML(TrackingFactory trackingFactory, String file) {

		try {

			Element root = new Element("balltracking");
			Document doc = new Document(root);
			// TODO: DTD-validation uncomment
			@SuppressWarnings("unused")
			DocType docType = new DocType("balltracking", settings.getInstallDir() + "/xml/balltracking.dtd");
			// doc.setDocType(docType);
			Attribute dirAtt = new Attribute("directory", trackingFactory.getDirectory());
			root.setAttribute(dirAtt);

			Element shapeEl = new Element("ballshape");
			Attribute radiusAtt = new Attribute("radius", trackingFactory.getBallShape().getRadius() + "");

			shapeEl.setAttribute(radiusAtt);

			Element colorEl = new Element("color");
			Attribute rAtt = new Attribute("red", trackingFactory.getBallShape().getColor().getRed() + "");
			Attribute gAtt = new Attribute("green", trackingFactory.getBallShape().getColor().getGreen() + "");
			Attribute bAtt = new Attribute("blue", trackingFactory.getBallShape().getColor().getBlue() + "");
			Attribute oAtt = new Attribute("opacity", trackingFactory.getBallShape().getColor().getOpacity() + "");

			colorEl.setAttribute(rAtt);
			colorEl.setAttribute(gAtt);
			colorEl.setAttribute(bAtt);
			colorEl.setAttribute(oAtt);

			shapeEl.addContent(colorEl);
			root.addContent(shapeEl);

			String type = AutomaticBallTracking.class.getAnnotation(XMLType.class).value();
			Set<Integer> indizes = trackingFactory.autoBallTracking.getTrackedIndizes();
			for (int index : indizes) {
				TrackingImage image = trackingFactory.autoBallTracking.getTrackingImage(index);

				Element imgEl = new Element("image");

				Attribute indexAtt = new Attribute("index", index + "");
				Attribute fileAtt = new Attribute("file", image.getFile().getName());
				Attribute typeAtt = new Attribute("type", type);

				imgEl.setAttribute(indexAtt);
				imgEl.setAttribute(fileAtt);
				imgEl.setAttribute(typeAtt);

				Element ballEl = new Element("ball");
				Element posEl = new Element("position");

				Attribute xAtt = new Attribute("x", image.getBall().getPosition().getX() + "");
				Attribute yAtt = new Attribute("y", image.getBall().getPosition().getY() + "");

				posEl.setAttribute(xAtt);
				posEl.setAttribute(yAtt);

				ballEl.addContent(posEl);

				imgEl.addContent(ballEl);

				root.addContent(imgEl);
			}

			type = ManualBallTracking.class.getAnnotation(XMLType.class).value();
			indizes = trackingFactory.manualBallTracking.getTrackedIndizes();
			for (int index : indizes) {
				TrackingImage image = trackingFactory.manualBallTracking.getTrackingImage(index);

				Element imgEl = new Element("image");

				Attribute indexAtt = new Attribute("index", index + "");
				Attribute fileAtt = new Attribute("file", image.getFile().getName());
				Attribute typeAtt = new Attribute("type", type);

				imgEl.setAttribute(indexAtt);
				imgEl.setAttribute(fileAtt);
				imgEl.setAttribute(typeAtt);

				Element ballEl = new Element("ball");
				Element posEl = new Element("position");

				Attribute xAtt = new Attribute("x", image.getBall().getPosition().getX() + "");
				Attribute yAtt = new Attribute("y", image.getBall().getPosition().getY() + "");

				posEl.setAttribute(xAtt);
				posEl.setAttribute(yAtt);

				ballEl.addContent(posEl);

				imgEl.addContent(ballEl);

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
			e.printStackTrace();
		}

	}

	public static TrackingFactory readBallTracking(File xmlFile) {

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
			if (root == null || !"balltracking".equals(root.getName())) {
				throw new IOException(xmlFile + " has wrong structure!");
			}
			dir = root.getAttributeValue("directory");

			Element shapeEl = root.getChild("ballshape");
			int radius = Integer.parseInt(shapeEl.getAttributeValue("radius"));

			Element colorEl = shapeEl.getChild("color");
			double red = Double.parseDouble(colorEl.getAttributeValue("red"));
			double green = Double.parseDouble(colorEl.getAttributeValue("green"));
			double blue = Double.parseDouble(colorEl.getAttributeValue("blue"));
			double opacity = Double.parseDouble(colorEl.getAttributeValue("opacity"));
			BallShape ballShape = new BallShape(radius, new Color(red, green, blue, opacity));

			List<Element> images = root.getChildren("image");

			String autoType = AutomaticBallTracking.class.getAnnotation(XMLType.class).value();
			String manualType = ManualBallTracking.class.getAnnotation(XMLType.class).value();

			int initialIndex = Integer.MAX_VALUE;
			int currentIndex = Integer.MIN_VALUE;

			for (Element imgEl : images) {

				int index = Integer.parseInt(imgEl.getAttributeValue("index"));
				File file = new File(dir + File.separator + imgEl.getAttributeValue("file"));

				Element ballEl = imgEl.getChild("ball");
				Element posEl = ballEl.getChild("position");
				int x = Integer.parseInt(posEl.getAttributeValue("x"));
				int y = Integer.parseInt(posEl.getAttributeValue("y"));

				Ball ball = new Ball(new Position(x, y), ballShape);

				TrackingImage image = new TrackingImage(file, ball);
				String type = imgEl.getAttributeValue("type");

				if (autoType.equals(type)) {
					autoTrackedImages.put(index, image);
					currentIndex = Math.max(currentIndex, index);
				} else if (manualType.equals(type)) {
					manualTrackedImages.put(index, image);
				}

				initialIndex = Math.min(initialIndex, index);

			}

			factory = new TrackingFactory(dir, ballShape, initialIndex, currentIndex);
			factory.autoBallTracking = new AutomaticBallTracking(autoTrackedImages);
			factory.manualBallTracking = new ManualBallTracking(manualTrackedImages);

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		} catch (JDOMException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}

		return factory;
	}
}
