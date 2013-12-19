package de.kicker.tracking.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.kicker.tracking.model.balltracking.AutomaticBallTracking;
import de.kicker.tracking.model.balltracking.AbstractBallTracking;
import de.kicker.tracking.model.settings.Settings;

public class XMLLayer {

	private static Logger logger = Logger.getLogger(XMLLayer.class);
	private static Settings settings = Settings.getInstance();

	public static void main(String[] args) {
		String dir = "E:\\Praktikum Master\\Bilder";
		File dirFile = new File(dir);
		File[] files = dirFile.listFiles();
		AbstractBallTracking tr = new AutomaticBallTracking(dir);
		for (File file : files) {
			tr.trackFile(file);
		}
		export2XML(tr, "xml/test.xml");
	}

	public static void export2XML(AbstractBallTracking ballTracking, String file) {

		try {

			Element root = new Element("balltracking");
			Document doc = new Document(root);
			@SuppressWarnings("unused")
			DocType docType = new DocType("balltracking", settings.getInstallDir()
					+ "/xml/balltracking.dtd");
			// doc.setDocType(docType);
			Attribute dirAtt = new Attribute("directory", ballTracking.getDirectory());
			root.setAttribute(dirAtt);

			Collection<TrackingImage> images = ballTracking.getAllTrackedImages();
			for (TrackingImage image : images) {
				Element imgEl = new Element("image");
				Attribute fileAtt = new Attribute("file", image.getFile().getName());
				imgEl.setAttribute(fileAtt);
				Element posEl = new Element("position");
				Attribute xAtt = new Attribute("x", image.getBall().getPosition().getX() + "");
				Attribute yAtt = new Attribute("y", image.getBall().getPosition().getY() + "");
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
			e.printStackTrace();
		}

	}

	public static AbstractBallTracking readAutomaticBallTracking(File xmlFile) {

		SAXBuilder builder = new SAXBuilder();

		AutomaticBallTracking ballTracking = null;

		try {
			File[] files = null;
			String dir = null;
			Map<File, TrackingImage> trackedImages = new HashMap<>();

			Document doc = builder.build(xmlFile);
			Element root = doc.getRootElement();
			if (root == null || !"balltracking".equals(root.getName())) {
				throw new IOException(xmlFile + " has wrong structure!");
			}
			dir = root.getAttributeValue("directory");
			List<Element> images = root.getChildren("image");
			files = new File[images.size()];

			int i = 0;
			for (Element el : images) {

				File file = new File(dir + File.separator + el.getAttributeValue("file"));
				files[i++] = file;

				Element elPos = el.getChild("position");
				int x = Integer.parseInt(elPos.getAttributeValue("x"));
				int y = Integer.parseInt(elPos.getAttributeValue("y"));

				TrackingImage image = new TrackingImage(file, x, y);
				trackedImages.put(file, image);

			}

			ballTracking = new AutomaticBallTracking(dir, trackedImages);

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

		return ballTracking;
	}
}
