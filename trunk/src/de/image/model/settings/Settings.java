package de.image.model.settings;

import java.util.ResourceBundle;

public final class Settings {

	public static String PATH_LOG;

	public static String FILE_LOG;
	public static boolean LOG_TO_FILE;

	public static int WINDOW_WIDTH;
	public static int WINDOW_HEIGHT;

	public static String WINDOW_TITLE;

	static {
		ResourceBundle resBundle = ResourceBundle.getBundle("settings");

		PATH_LOG = resBundle.getString("log_path");

		FILE_LOG = resBundle.getString("log_file");
		LOG_TO_FILE = Boolean.parseBoolean(resBundle.getString("log_to_file"));

		WINDOW_WIDTH = Integer.parseInt(resBundle.getString("window_width"));
		WINDOW_HEIGHT = Integer.parseInt(resBundle.getString("window_height"));

		WINDOW_TITLE = resBundle.getString("window_title");
	}

}
