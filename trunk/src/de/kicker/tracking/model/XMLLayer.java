package de.kicker.tracking.model;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;

import org.jdom2.Attribute;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XMLLayer {

	public static void main(String[] args) {
		String dir = "E:\\Praktikum Master\\Bilder";
		File dirFile = new File(dir);
		File[] files = dirFile.listFiles();
		AutomaticBallTracking tr = new AutomaticBallTracking(files, dir, 0);
		while (!tr.endOfFiles()) {
			tr.trackNext();
		}
		export2XML(tr, "xml/test.xml");
	}

	public static void export2XML(BallTracking ballTracking, String file) {

		try {

			Element root = new Element("balltracking");
			Document doc = new Document(root);
			DocType docType = new DocType("balltracking", "xml/balltracking.dtd");
			doc.setDocType(docType);
			Attribute dirAtt = new Attribute("directory",
					ballTracking.getDirectory());
			root.setAttribute(dirAtt);

			Collection<TrackingImage> images = ballTracking
					.getAllTrackedImages();
			for (TrackingImage image : images) {
				Element imgEl = new Element("image");
				Attribute fileAtt = new Attribute("file", image.getFile()
						.getName());
				imgEl.setAttribute(fileAtt);
				Element posEl = new Element("position");
				Attribute xAtt = new Attribute("x", image.getBall()
						.getPosition().getX()
						+ "");
				Attribute yAtt = new Attribute("y", image.getBall()
						.getPosition().getY()
						+ "");
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

	public static AutomaticBallTracking readAutomaticBallTracking(String file) {
		File[] files = null;
		int index = 0;
		AutomaticBallTracking ballTracking = new AutomaticBallTracking(files,
				"", index);
		return ballTracking;

	}

}
