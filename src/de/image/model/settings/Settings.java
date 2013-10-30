package de.image.model.settings;

import java.util.ResourceBundle;

public final class Settings {

	private static Settings singleton;

	// ##### window settings #####
	private String windowTitle;
	private int windowWidth;
	private int windowHeight;

	private Settings() {
		ResourceBundle resBundle = ResourceBundle.getBundle("settings");

		setWindowTitle(resBundle.getString("window_title"));
		setWindowWidth(Integer.parseInt(resBundle.getString("window_width")));
		setWindowHeight(Integer.parseInt(resBundle.getString("window_height")));
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

}
