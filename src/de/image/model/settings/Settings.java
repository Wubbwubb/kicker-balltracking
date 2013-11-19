package de.image.model.settings;

import java.io.File;
import java.util.ResourceBundle;

public final class Settings {

	private static Settings singleton;

	private ResourceBundle resBundle;

	// ##### window settings #####
	private String windowTitle;
	private int windowWidth;
	private int windowHeight;
	private String windowIcon;

	// ##### image settings #####
	private String imageDir;
	private String imagePlaceholder;

	private String chooserTitle;

	private Settings() {
		resBundle = ResourceBundle.getBundle("settings");

		setWindowTitle(getProperty("window_title", "Kicker BallTracking"));
		setWindowWidth(getIntProperty("window_width", 1200));
		setWindowHeight(getIntProperty("window_height", 900));

		setImageDir(getProperty("image_dir", "E:\\Praktikum Master\\Bilder"));
		setImagePlaceholder(getProperty("image_placeholder",
				"images/img_placeholder"));
		setChooserTitle(getProperty("folderchooser_title", "Choose Folder"));
		setWindowIcon(getProperty("window_icon", "images/icon.png"));
	}

	private String getProperty(String key, String defaultValue) {
		String value = "";
		try {
			value = resBundle.getString(key);
		} catch (Exception e) {
			return defaultValue;
		}
		return value;
	}

	private int getIntProperty(String key, int defaultValue) {
		int value = 0;
		try {
			value = Integer.parseInt(resBundle.getString(key));
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

	public void setWindowTitle(String windowTitle) {
		this.windowTitle = windowTitle;
	}

	public int getWindowWidth() {
		return windowWidth;
	}

	public void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public void setWindowHeight(int windowHeight) {
		this.windowHeight = windowHeight;
	}

	public String getImageDir() {
		return imageDir;
	}

	public void setImageDir(String imageDir) {
		imageDir = imageDir.replace("/", File.separator);
		this.imageDir = imageDir;
	}

	public String getImagePlaceholder() {
		return imagePlaceholder;
	}

	public void setImagePlaceholder(String imagePlaceholder) {
		imagePlaceholder = imagePlaceholder.replace("/", File.separator);
		this.imagePlaceholder = imagePlaceholder;
	}

	public String getChooserTitle() {
		return chooserTitle;
	}

	public void setChooserTitle(String chooserTitle) {
		this.chooserTitle = chooserTitle;
	}

	public String getWindowIcon() {
		return windowIcon;
	}

	public void setWindowIcon(String windowIcon) {
		this.windowIcon = windowIcon;
	}

}
