package de.kicker.tracking.model;

import java.io.File;

public class TrackingImage {

	private File file;
	private Position position;

	public TrackingImage(File file, Position position) {
		setFile(file);
		setPosition(position);
	}

	public Position getPosition() {
		return this.position;
	}

	public void setPosition(Position ball) {
		this.position = ball;
	}

	public File getFile() {
		return this.file;
	}

	public void setFile(File file) {
		this.file = file;
	}

}
