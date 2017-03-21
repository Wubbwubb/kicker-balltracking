package de.kicker.tracking.model;

public class Position {

	private int x;
	private int y;

	public static final Position POSITION_NOT_FOUND = new Position(-1, -1);

	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public boolean isNotFound() {
		return equals(POSITION_NOT_FOUND);
	}

	@Override
	public String toString() {
		return "x: " + x + " y: " + y;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Position) {
			Position object = (Position) obj;
			return getX() == object.getX() && getY() == object.getY();
		}
		return false;
	}

	public static double getDistance(Position p1, Position p2) {
		return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
	}

}
