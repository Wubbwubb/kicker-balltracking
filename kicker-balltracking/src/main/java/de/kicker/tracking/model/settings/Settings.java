package de.kicker.tracking.model.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;

public final class Settings {

	private static Settings singleton;

	private Properties prop;

	private boolean debugMode;

	// ##### window settings #####
	private static final String windowIcon = "images/icon.png";

	// ##### image settings #####
	private String imageDir;
	private static final String imagePlaceholder = "images/img_placeholder.png";
	private int imageWidth;
	private int imageHeight;

	private int ballRadius;

	private Color manualColor;
	private Color autoColor;

	private KeyCombination keyCombinationBtnPrev;
	private KeyCombination keyCombinationBtnNext;
	private KeyCombination keyCombinationTrackNext;
	private KeyCombination keyCombinationTrackAll;
	private KeyCombination keyCombinationTrackManual;

	private int searchFailThreshold;
	private int maxColorDistance;
	private int radiusSearchSmall;

	private int topBound;
	private int rightBound;
	private int bottomBound;
	private int leftBound;

	private String dokuFile;

	private String debugDirectory;
	private boolean createDebugImages;

	public static Settings getInstance() {
		if (singleton == null) {
			singleton = new Settings();
		}
		return singleton;
	}

	private Settings() {
		InputStream fIn = null;
		try {
			fIn = new FileInputStream("properties" + File.separator + "settings.properties");
			prop = new Properties();
			prop.load(fIn);

			setDebugMode(getBooleanProperty("debug_mode", false));

			setImageDir(getProperty("image_dir",
					"E:" + File.separator + "Praktikum Master" + File.separator + "Bilder_orange"));
			setImageWidth(getIntProperty("image_width", 640));
			setImageHeight(getIntProperty("image_height", 480));

			setBallRadius(getIntProperty("ball_radius", 6));

			setKeyCombinationBtnPrev(KeyCombination.keyCombination("F3"));
			setKeyCombinationBtnNext(KeyCombination.keyCombination("F4"));
			setKeyCombinationTrackNext(KeyCombination.keyCombination("F6"));
			setKeyCombinationTrackAll(KeyCombination.keyCombination("F8"));
			setKeyCombinationTrackManual(KeyCombination.keyCombination("F5"));

			setSearchFailThreshold(getIntProperty("search_fail_threshold", 5));
			setMaxColorDistance(getIntProperty("max_color_distance", 40));
			setRadiusSearchSmall(getIntProperty("radius_search_small", 25));

			setTopBound(getIntProperty("top_bound", 106));
			setRightBound(getIntProperty("right_bound", 560));
			setBottomBound(getIntProperty("bottom_bound", 378));
			setLeftBound(getIntProperty("left_bound", 88));

			setDokuFile(getProperty("doku_file", "doku/Masterpraktikum Kicker-Balltracking.pdf"));

			setDebugDirectory(getProperty("debug_dir", "E:" + File.separator + "Praktikum Master"));
			setCreateDebugImages(getBooleanProperty("create_debug_images", false));

			setAutoColor(getProperty("auto_color", "#FF0000"));
			setManualColor(getProperty("manual_color", "#0000FF"));

		} catch (Exception e) {
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
		String value;
		try {
			value = prop.getProperty(key);
		} catch (Exception e) {
			return defaultValue;
		}
		return value;
	}

	private int getIntProperty(String key, int defaultValue) {
		int value;
		try {
			value = Integer.parseInt(prop.getProperty(key));
		} catch (Exception e) {
			return defaultValue;
		}
		return value;
	}

	private boolean getBooleanProperty(String key, boolean defaultValue) {
		boolean value;
		try {
			value = Boolean.parseBoolean(prop.getProperty(key));
		} catch (Exception e) {
			return defaultValue;
		}
		return value;
	}

	public String getWindowicon() {
		return windowIcon;
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

	public int getTopBound() {
		return topBound;
	}

	private void setTopBound(int topBound) {
		this.topBound = topBound;
	}

	public int getRightBound() {
		return rightBound;
	}

	private void setRightBound(int rightBound) {
		this.rightBound = rightBound;
	}

	public int getBottomBound() {
		return bottomBound;
	}

	private void setBottomBound(int bottomBound) {
		this.bottomBound = bottomBound;
	}

	public int getLeftBound() {
		return leftBound;
	}

	private void setLeftBound(int leftBound) {
		this.leftBound = leftBound;
	}

	public String getDebugDirectory() {
		return debugDirectory;
	}

	private void setDebugDirectory(String debugDirectory) {
		this.debugDirectory = debugDirectory;
	}

	public String getDokuFile() {
		return dokuFile;
	}

	private void setDokuFile(String dokuFile) {
		this.dokuFile = dokuFile;
	}

	public boolean createDebugImages() {
		return createDebugImages;
	}

	private void setCreateDebugImages(boolean createDebugImages) {
		this.createDebugImages = createDebugImages;
	}

	public Color getManualColor() {
		return manualColor;
	}

	private void setManualColor(String manualColor) {
		this.manualColor = Color.web(manualColor);
	}

	public Color getAutoColor() {
		return autoColor;
	}

	private void setAutoColor(String autoColor) {
		this.autoColor = Color.web(autoColor);
	}
}
