package de.kicker.tracking.model.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javafx.scene.input.KeyCombination;

public final class Settings {

	private static Settings singleton;

	Properties prop;

	private boolean debugMode;

	// ##### installation directory #####
	private String installDir;

	// ##### window settings #####
	private String windowTitle;
	private int windowWidth;
	private int windowHeight;
	private String windowIcon;

	// ##### image settings #####
	private String imageDir;
	private String imagePlaceholder;
	private int imageWidth;
	private int imageHeight;

	private String dirChooserTitle;
	private String fileChooserTitle;

	private int ballRadius;

	private KeyCombination keyCombinationBtnPrev;
	private KeyCombination keyCombinationBtnNext;
	private KeyCombination keyCombinationTrackNext;
	private KeyCombination keyCombinationTrackAll;
	private KeyCombination keyCombinationTrackManual;

	private int searchFailThreshold;
	private int maxColorDistance;
	private int radiusSearchSmall;

	private String debugDirectory;
	private boolean createDebugImages;

	private Settings() {
		InputStream fIn = null;
		try {
			fIn = new FileInputStream("properties" + File.separator + "settings.properties");
			prop = new Properties();
			prop.load(fIn);

			setDebugMode(getBooleanProperty("debug_mode", false));

			setInstallDir(getProperty("install_dir", ""));

			setWindowTitle(getProperty("window_title", "Kicker BallTracking"));
			setWindowWidth(getIntProperty("window_width", 1200));
			setWindowHeight(getIntProperty("window_height", 900));
			setWindowIcon(getProperty("window_icon", "images/icon.png"));

			setImageDir(getProperty("image_dir", "E:" + File.separator + "Praktikum Master" + File.separator
					+ "Bilder_orange"));
			setImagePlaceholder(getProperty("image_placeholder", "images/img_placeholder"));
			setImageWidth(getIntProperty("image_width", 640));
			setImageHeight(getIntProperty("image_height", 480));

			setDirChooserTitle(getProperty("folderchooser_title", "Choose Folder"));
			setFileChooserTitle(getProperty("filechooser_title", "Choose Image"));

			setBallRadius(getIntProperty("ball_radius", 6));

			setKeyCombinationBtnPrev(KeyCombination.keyCombination("F3"));
			setKeyCombinationBtnNext(KeyCombination.keyCombination("F4"));
			setKeyCombinationTrackNext(KeyCombination.keyCombination("F6"));
			setKeyCombinationTrackAll(KeyCombination.keyCombination("F8"));
			setKeyCombinationTrackManual(KeyCombination.keyCombination("F5"));

			setSearchFailThreshold(getIntProperty("search_fail_threshold", 5));
			setMaxColorDistance(getIntProperty("max_color_distance", 40));
			setRadiusSearchSmall(getIntProperty("radius_search_small", 25));

			setDebugDirectory(getProperty("debug_dir", "E:" + File.separator + "Praktikum Master"));
			setCreateDebugImages(getBooleanProperty("create_debug_images", false));

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

	private boolean getBooleanProperty(String key, boolean defaultValue) {
		boolean value = false;
		try {
			value = Boolean.parseBoolean(prop.getProperty(key));
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

	public String getInstallDir() {
		return installDir;
	}

	private void setInstallDir(String installDir) {
		this.installDir = installDir;
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

	public int getImageWidth() {
		return imageWidth;
	}

	private void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	private void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}

	public int getBallRadius() {
		return ballRadius;
	}

	private void setBallRadius(int ballRadius) {
		this.ballRadius = ballRadius;
	}

	public KeyCombination getKeyCombinationBtnPrev() {
		return keyCombinationBtnPrev;
	}

	private void setKeyCombinationBtnPrev(KeyCombination keyCombinationBtnPrev) {
		this.keyCombinationBtnPrev = keyCombinationBtnPrev;
	}

	public KeyCombination getKeyCombinationBtnNext() {
		return keyCombinationBtnNext;
	}

	private void setKeyCombinationBtnNext(KeyCombination keyCombinationBtnNext) {
		this.keyCombinationBtnNext = keyCombinationBtnNext;
	}

	public KeyCombination getKeyCombinationTrackNext() {
		return keyCombinationTrackNext;
	}

	private void setKeyCombinationTrackNext(KeyCombination keyCombinationTrackNext) {
		this.keyCombinationTrackNext = keyCombinationTrackNext;
	}

	public KeyCombination getKeyCombinationTrackAll() {
		return keyCombinationTrackAll;
	}

	private void setKeyCombinationTrackAll(KeyCombination keyCombinationTrackAll) {
		this.keyCombinationTrackAll = keyCombinationTrackAll;
	}

	public KeyCombination getKeyCombinationTrackManual() {
		return keyCombinationTrackManual;
	}

	private void setKeyCombinationTrackManual(KeyCombination keyCombinationTrackManual) {
		this.keyCombinationTrackManual = keyCombinationTrackManual;
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	private void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	public int getSearchFailThreshold() {
		return searchFailThreshold;
	}

	private void setSearchFailThreshold(int searchFailThreshold) {
		this.searchFailThreshold = searchFailThreshold;
	}

	public int getMaxColorDistance() {
		return maxColorDistance;
	}

	private void setMaxColorDistance(int maxColorDistance) {
		this.maxColorDistance = maxColorDistance;
	}

	public int getRadiusSearchSmall() {
		return radiusSearchSmall;
	}

	private void setRadiusSearchSmall(int radiusSearchSmall) {
		this.radiusSearchSmall = radiusSearchSmall;
	}

	public String getDebugDirectory() {
		return debugDirectory;
	}

	private void setDebugDirectory(String debugDirectory) {
		this.debugDirectory = debugDirectory;
	}

	public boolean createDebugImages() {
		return createDebugImages;
	}

	private void setCreateDebugImages(boolean createDebugImages) {
		this.createDebugImages = createDebugImages;
	}
}
