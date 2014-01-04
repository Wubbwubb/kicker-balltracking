package de.kicker.tracking.model;

public class Ball {

	private Position position;
	private BallShape ballShape;

	public Ball(Position position, BallShape ballShape) {
		setPosition(position);
		setBallShape(ballShape);
	}

	public Position getPosition() {
		return this.position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public BallShape getBallShape() {
		return this.ballShape;
	}

	public void setBallShape(BallShape ballShape) {
		this.ballShape = ballShape;
	}

}
