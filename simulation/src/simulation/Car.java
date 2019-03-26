package simulation;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The car that runs around in the World.
 */
final class Car {

	/**
	 * This is the acceleration constant used when {@link #isAccelerating} or
	 * {@link #isDecelerating} is true.
	 */
	private static final double ACCELERATION = 0.05;

	/** Amount of decrease in velocity when braking. */
	private static final double DECELERATION = 0.1;	//may be changed to be different from acceleration

	/** In radians. */
	private static final double TURN_AMOUNT = Math.PI / 180;

	/** width and length of the rectangle representing the car */
	private static final int WIDTH = 40, LENGTH = 70;

	private final Rectangle DISPLAY = new Rectangle(WIDTH, LENGTH);


	private volatile double x, y;
	// heading is in radians, increasing counter-clockwise, 0 facing right.
	private volatile double velocity, heading;

	private volatile boolean isAccelerating, isDecelerating, isBraking, isTurningLeft, isTurningRight;
	/*
	Accelerating:	car speed increasing;
	Decelerating:	car speed decreasing at the same rate as accelerating;
	Braking:		car speed approaches 0 at a higher rate than acceleration;
	Turning:		changes heading by a constant each update.
	 */


	// sensors used for range finding
	/**
	 * The angles in radians at which the sensors point relative to the direction of this
	 * Car.
	 */
	private final double
			FRONT       = 0,
			BACK        = Math.PI,
			LEFT        = Math.PI / 2,
			RIGHT       = -Math.PI / 2,
			FRONT_LEFT  = Math.PI / 6,
			FRONT_RIGHT = -Math.PI / 6,
			LEFT_FRONT  = Math.PI / 3,
			RIGHT_FRONT = -Math.PI / 3;
	/** For easier enumeration. */
	private final List<Double> RANGE_FINDER_DIRS = List.of(
			FRONT, BACK, LEFT, RIGHT, FRONT_LEFT, FRONT_RIGHT, LEFT_FRONT, RIGHT_FRONT
	);

	/**
	 * Finds the range to the closest obstacle.
	 * @param x x location to find range from
	 * @param y y location to find range from
	 * @param direction angle in radians between x axis and the direction to find range in
	 * @param obstacles collection of obstacles to be checked for range
	 * @return  distance to the closest intercept with an obstacle
	 */
	private static double findRange(
			final double x, final double y, final double direction,
			final Collection<? extends Obstacle> obstacles) {
		double range = Double.MAX_VALUE;

		/*
		Finds the intercept between the line from the obstacle and the line from the range
		founder. Checks if the intercept is within the bounds of the obstacle.
		 */

		for (var obstacle : obstacles) {
			// deltas used to find slope of the obstacle line
			final double deltaX = obstacle.getX2() - obstacle.getX1();
			final double deltaY = obstacle.getY2() - obstacle.getY1();

			// direction is used to find slope of the range finder line
			// used a lot, a cache; also avoids divide by zero
			final double tanTheta =
					(Math.tan(direction) == 0) ? Double.MIN_VALUE : Math.tan(direction);

			final double interceptX, interceptY;


			// if both delta are 0, obstacle is a point and no intercept
			if (deltaX == 0 && deltaY == 0) continue;

			/* if either delta is zero, the obstacle is a vertical or horizontal line, no
			 * need to solve for intercept as linear functions. */
			if (deltaX == 0) {
				interceptX = obstacle.getX1();
				interceptY = (interceptX - x) * tanTheta + y;
			}
			else if (deltaY == 0) {
				interceptY = obstacle.getY1();
				interceptX = (interceptY - y) / tanTheta + x;
			}

			// solve as 2 linear functions
			else {
				interceptX =
						(y - obstacle.getY1() + (deltaY / deltaX) * obstacle.getX1() - tanTheta * x)
								/ ((deltaY / deltaX) - tanTheta);
				interceptY = (interceptX - x) * tanTheta + y;
			}


			// check if intercept is on obstacle segment
			// one check is sufficient as the intercept is known to be on the line
			if (!isBetween(obstacle.getX1(), obstacle.getX2(), interceptX))
				continue;

			final double distance = Math.hypot(interceptX - x, interceptY - y);

			if (distance < range)
				range = distance;
		}

		return range;
	}

	/**
	 * Checks if the value is within or on the edge of the bounds. This method does not
	 * rely on any relationship between the bounds.
	 */
	private static boolean isBetween(final double bound1,
	                                 final double bound2,
	                                 final double value) {
		if (bound1 == bound2)
			return value == bound1;

		final double lower, upper;

		if (bound1 < bound2) {
			lower = bound1; upper = bound2;
		}
		else {
			lower = bound2; upper = bound1;
		}

		return value >= lower && value <= upper;
	}


	/**
	 * @return  a list of range finder readings that guarantees a consistent ordering
	 * between invocations
	 */
	List<Double> getRangeReadingsAsList(final Collection<? extends Obstacle> obstacles) {
		return RANGE_FINDER_DIRS.stream()
				       .map(direction -> findRange(getX(), getY(), direction + getHeading(), obstacles))
				       .collect(Collectors.toList());
	}


	/**
	 * Creates a Car at the specified location with default fill of {@link Color#DARKGREY}.
	 */
	Car(double x, double y) {
		this(x, y, Color.DARKGREY);
	}

	/**
	 * Creates a Car at the specified location with the specified Color for fill.
	 */
	Car(double x, double y, Color fill) {
		this.x = x; this.y = y;

		DISPLAY.setStroke(Color.BLACK);
		DISPLAY.setFill(fill);
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
			velocity += ACCELERATION;
		if (isDecelerating())
			velocity -= ACCELERATION;
		if (isBraking())
			brake();

		// new location based on velocity
		x += getVelocity() * Math.cos(Math.toRadians(getHeading()));
		y += getVelocity() * Math.sin(Math.toRadians(getHeading()));

		// graphics must be run by JavaFX application thread
		Platform.runLater(this::updateDisplay);
	}

	private synchronized void brake() {
		if (getVelocity() > DECELERATION)
			velocity -= DECELERATION;
		else if (getVelocity() < -DECELERATION)
			velocity += DECELERATION;
		else
			velocity = 0;
	}

	/**
	 * Updates the location of the display rectangle to reflect the current location of
	 * this Car.
	 */
	private void updateDisplay() {
		DISPLAY.setX(getX() - WIDTH / 2d);
		DISPLAY.setY(getY() - LENGTH / 2d);
	}


	private synchronized void handleTurning() {
		if (getVelocity() == 0) return;	// no speed, no turning

		if (isTurningLeft())
			heading += TURN_AMOUNT;
		if (isTurningRight())
			heading -= TURN_AMOUNT;

		// pi/2 offset because display has 0 facing up, car has 0 facing right.
		// setRotate considers clockwise positive, thus the negation
		DISPLAY.setRotate(-Math.toDegrees( Math.PI/2 + getHeading() ));
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

	double getVelocity() { return velocity; }
	double getHeading() { return heading; }
}
