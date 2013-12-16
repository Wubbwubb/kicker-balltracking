package de.kicker.tracking.model.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Settings {

	private static Settings singleton;

	Properties prop;

	// ##### window settings #####
	private String windowTitle;
	private int windowWidth;
	private int windowHeight;
	private String windowIcon;

	// ##### image settings #####
	private String imageDir;
	private String imagePlaceholder;

	private String dirChooserTitle;
	private String fileChooserTitle;

	private Settings() {
		InputStream fIn = null;
		try {
			fIn = new FileInputStream("properties" + File.separator
					+ "settings.properties");
			prop = new Properties();
			prop.load(fIn);

			setWindowTitle(getProperty("window_title", "Kicker BallTracking"));
			setWindowWidth(getIntProperty("window_width", 1200));
			setWindowHeight(getIntProperty("window_height", 900));

			setImageDir(getProperty("image_dir", "E:\\Praktikum Master\\Bilder"));
			setImagePlaceholder(getProperty("image_placeholder",
					"images/img_placeholder"));
			setDirChooserTitle(getProperty("folderchooser_title",
					"Choose Folder"));
			setFileChooserTitle(getProperty("filechooser_title", "Choose Image"));
			setWindowIcon(getProperty("window_icon", "images/icon.png"));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fIn != null) {
					fIn.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String getProperty(String key, String defaultValue) {
		String value = "";
		try {
			value = prop.getProperty(key);
		} catch (Exception e) {
			return defaultValue;
		}
		return value;
	}

	private int getIntProperty(String key, int defaultValue) {
		int value = 0;
		try {
			value = Integer.parseInt(prop.getProperty(key));
		} catch (Exception e) {
			return defaultValue;
		}
		return value;
	}

	public static Settings getInstance() {
		if (singleton == null) {
			singleton = new Settings();
		}
		return singleton;
	}

	public String getWindowTitle() {
		return windowTitle;
	}

	private void setWindowTitle(String windowTitle) {
		this.windowTitle = windowTitle;
	}

	public int getWindowWidth() {
		return windowWidth;
	}

	private void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	private void setWindowHeight(int windowHeight) {
		this.windowHeight = windowHeight;
	}

	public String getImageDir() {
		return imageDir;
	}

	private void setImageDir(String imageDir) {
		imageDir = imageDir.replace("/", File.separator);
		this.imageDir = imageDir;
	}

	public String getImagePlaceholder() {
		return imagePlaceholder;
	}

	private void setImagePlaceholder(String imagePlaceholder) {
		imagePlaceholder = imagePlaceholder.replace("/", File.separator);
		this.imagePlaceholder = imagePlaceholder;
	}

	public String getDirChooserTitle() {
		return dirChooserTitle;
	}

	private void setDirChooserTitle(String chooserTitle) {
		this.dirChooserTitle = chooserTitle;
	}

	public String getWindowIcon() {
		return windowIcon;
	}

	private void setWindowIcon(String windowIcon) {
		this.windowIcon = windowIcon;
	}

	public String getFileChooserTitle() {
		return fileChooserTitle;
	}

	private void setFileChooserTitle(String fileChooserTitle) {
		this.fileChooserTitle = fileChooserTitle;
	}

}
