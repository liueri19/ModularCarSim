package simulation;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * The car that runs around in the World.
 */
final class Car {

	/**
	 * This is the acceleration constant used when {@link Car#isAccelerating} is true.
	 */
	private static final double ACCELERATION = 0.05;

	/** Amount of decrease in velocity. */
	private static final double DECELERATION = 0.1;	//may be changed to be different from acceleration

	/** In degrees. */
	private static final double TURN_AMOUNT = 1;

	/** width and length of the rectangle representing the car */
	private static final int WIDTH = 40, LENGTH = 70;

	private final Rectangle DISPLAY = new Rectangle(WIDTH, LENGTH);
	Rectangle getDisplay() { return DISPLAY; }


	private volatile double x, y;
	// heading is in degrees, increasing clockwise, 0 facing right.
	private volatile double veloctiy, heading;

	private volatile boolean isAccelerating, isDecelerating, isBraking, isTurningLeft, isTurningRight;
	/*
	Accelerating:	car speed increasing;
	Decelerating:	car speed decreasing at the same rate as accelerating;
	Braking:		car speed approaches 0 at a higher rate than acceleration;
	Turning:		changes heading by a constant each update.
	 */


	Car(double x, double y) {
		this.x = x; this.y = y;
		updateDisplay();

		DISPLAY.setStroke(Color.BLACK);
		DISPLAY.setFill(Color.LIGHTGREEN);
		DISPLAY.setRotate(90);	// clockwise 90
	}


	/**
	 * Updates the states of the car, such as speed, heading, and location,
	 */
	synchronized void update() {
		handleTurning();
		handleLocation();
	}

	private synchronized void handleLocation() {
		if (isAccelerating())
			veloctiy += ACCELERATION;
		if (isDecelerating())
			veloctiy -= ACCELERATION;
		if (isBraking())
			brake();

		// new location based on velocity
		x += getVeloctiy() * Math.cos(Math.toRadians(getHeading()));
		y += getVeloctiy() * Math.sin(Math.toRadians(getHeading()));
		updateDisplay();
	}

	private synchronized void brake() {
		if (getVeloctiy() > DECELERATION)
			veloctiy -= DECELERATION;
		else if (getVeloctiy() < -DECELERATION)
			veloctiy += DECELERATION;
		else
			veloctiy = 0;
	}

	/**
	 * Updates the location of the display rectangle to reflect the current location of
	 * this Car.
	 */
	private void updateDisplay() {
		DISPLAY.setX(getX() - WIDTH / 2);
		DISPLAY.setY(getY() - LENGTH / 2);
	}


	private synchronized void handleTurning() {
		if (getVeloctiy() == 0) return;	// no speed, no turning

		if (isTurningLeft())
			heading -= TURN_AMOUNT;
		if (isTurningRight())
			heading += TURN_AMOUNT;

		// 90 offset because display has 0 facing up, car has 0 facing right.
		DISPLAY.setRotate(90 + getHeading());
	}


	//// controls
	boolean isAccelerating() { return isAccelerating; }
	void setIsAccelerating(boolean isAccelerating) {
		this.isAccelerating = isAccelerating;
	}

	boolean isDecelerating() { return isDecelerating; }
	void setIsDecelerating(boolean isDecelerating) {
		this.isDecelerating = isDecelerating;
	}

	boolean isBraking() { return isBraking; }
	void setIsBraking(boolean isBraking) {
		this.isBraking = isBraking;
	}

	boolean isTurningLeft() { return isTurningLeft; }
	void setIsTurningLeft(boolean isTurningLeft) {
		this.isTurningLeft = isTurningLeft;
	}

	boolean isTurningRight() { return isTurningRight; }
	void setIsTurningRight(boolean isTurningRight) {
		this.isTurningRight = isTurningRight;
	}


	double getX() { return x; }
	double getY() { return y; }

	double getVeloctiy() { return veloctiy; }
	double getHeading() { return heading; }
}
