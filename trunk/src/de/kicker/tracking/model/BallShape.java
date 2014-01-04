package de.kicker.tracking.model;

import javafx.scene.paint.Color;

public class BallShape {

	private int radius;
	private Color color;

	public BallShape(int radius, Color color) {
		setRadius(radius);
		setColor(color);
	}

	public int getRadius() {
		return this.radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public Color getColor() {
		return this.color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

}
