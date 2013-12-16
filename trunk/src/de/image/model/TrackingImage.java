package de.image.model;

import java.io.File;

public class TrackingImage {

	private File file;
	private Ball ball;

	public TrackingImage(File file, Ball ball) {
		this.file = file;
		this.ball = ball;
	}

	public Ball getBall() {
		return ball;
	}

	public void setPosition(Position position) {
		if (ball == null) {
			ball = new Ball();
		}
		ball.setPosition(position);
	}

	public File getFile() {
		return file;
	}

}
