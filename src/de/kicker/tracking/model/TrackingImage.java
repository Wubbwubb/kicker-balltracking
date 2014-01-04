package de.kicker.tracking.model;

import java.io.File;

public class TrackingImage {

	private File file;
	private Ball ball;

	public TrackingImage(File file, Ball ball) {
		setFile(file);
		setBall(ball);
	}

	public Ball getBall() {
		return this.ball;
	}

	public void setBall(Ball ball) {
		this.ball = ball;
	}

	public File getFile() {
		return this.file;
	}

	public void setFile(File file) {
		this.file = file;
	}

}
