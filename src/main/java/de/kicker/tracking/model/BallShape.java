package de.kicker.tracking.model;

import de.kicker.tracking.util.FXUtil;

public class BallShape {

	private int radius;
	private javafx.scene.paint.Color fxColor;
	private java.awt.Color awtColor;

	public BallShape(int radius, javafx.scene.paint.Color color) {
		setRadius(radius);
		setColor(color);
	}

	public int getRadius() {
		return this.radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public javafx.scene.paint.Color getFXColor() {
		return this.fxColor;
	}

	public void setColor(javafx.scene.paint.Color color) {
		this.fxColor = color;
		this.awtColor = convertFXColorToAWTColor(color);
	}

	public java.awt.Color getAWTColor() {
		return awtColor;
	}

	@Override
	public String toString() {
		return "radius: " + radius + " color: " + FXUtil.getColorString(fxColor);
	}

	private java.awt.Color convertFXColorToAWTColor(javafx.scene.paint.Color color) {
		int red = (int) Math.round(255 * color.getRed());
		int green = (int) Math.round(255 * color.getGreen());
		int blue = (int) Math.round(255 * color.getBlue());
		return new java.awt.Color(red, green, blue);
	}

}
