package simulation;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import network.Network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The world where the car will be running around in.
 */
public final class World extends Application {
	private static final int WIDTH = 1280, HEIGHT = 720;
	private static final double START_X = WIDTH / 2d, START_Y = HEIGHT / 2d;

	/** Controls the termination of the simulation thread. */
	private volatile boolean done = false;
	void terminate() { done = true; }


	// singleton
	private static World world;
	static World getWorld() { return world; }

	// number of updates consumed in this simulation run
	private volatile AtomicLong clock = new AtomicLong();


	private final Track track = null;   // TODO implement load track

	/** Each driver controls a car. */
	private final List<Driver> drivers = new ArrayList<>();

	void addDriver(final Network network) {
		drivers.add(
				new Driver(track, new Car(START_X, START_Y), network)
		);
	}

	void addDrivers(final Collection<? extends Network> networks) {
		networks.forEach(this::addDriver);
	}

	List<Driver> getDrivers() { return new ArrayList<>(drivers); }


	private volatile boolean simRan = false;
	/** Runs a simulation after the desired setup has been arranged. */
	synchronized void runSimulation() {
		if (!simRan)
			simRan = true;
		else
			throw new IllegalStateException("this simulation has run or is running");

		// run sim
		try {
			while (!done) {
				Thread.sleep(10);

				getDrivers().forEach(Driver::drive);
				// TODO instead of updating car, update view of surrounding

				clock.getAndIncrement();
			}
		}
		catch (InterruptedException e) {
			System.err.println("Simulation Interrupted");
			Thread.currentThread().interrupt();
			terminate();
		}
	}

	/** Resets the operation counter and remove all drivers. */
	synchronized void reset() {
		clock.set(0);
		drivers.clear();
		simRan = false;
	}



	/* ****************************************
	Everything below is mostly display related.
	******************************************/

	/** A manually controlled car for debug */
	private final Car debugCar = new Car(START_X, START_Y, Color.LIGHTGREEN);
	/** debug flag. if true, debugCar will be displayed. */
	private volatile boolean debug = false;
	private final Object debugLock = new Object();

	private Stage stage;
	/** the root node upon which entities to be displayed may be added */
	private final Pane root = new Pane();
	private final Scene scene = new Scene(root, WIDTH, HEIGHT);



    public void start(Stage stage) {
		world = this;
		this.stage = stage;

		stage.setResizable(false);

		setUpKeyHandlers(scene);

		stage.setTitle("CarSim");

		stage.setOnCloseRequest(event -> terminate());

		stage.setScene(scene);
		stage.show();
    }


	/** Debug and used by SimEvaluator to launch JavaFX. */
	public static void main(String... args) { launch(args); }

	/** Sets up quit keys and debugging controls */
    private void setUpKeyHandlers(Scene scene) {
		scene.setOnKeyPressed(event -> {
			// manual control for debug
			if (debug) {
				switch (event.getCode()) {
					case A: case LEFT:
						debugCar.setIsTurningLeft(true); break;
					case D: case RIGHT:
						debugCar.setIsTurningRight(true); break;
					case W: case UP:
						debugCar.setIsAccelerating(true); break;
					case S: case DOWN:
						debugCar.setIsDecelerating(true); break;
					case SPACE: case SHIFT:
						debugCar.setIsBraking(true); break;
				}
			}

			switch (event.getCode()) {
				case ESCAPE: case Q:
					world.terminate(); stage.close(); break;
				case V:
					synchronized (debugLock) {
						debug = !debug;
					}
					break;
			}
		});

		scene.setOnKeyReleased(event -> {
			// manual control for debug
			if (debug) {
				switch (event.getCode()) {
					case A: case LEFT:
						debugCar.setIsTurningLeft(false); break;
					case D: case RIGHT:
						debugCar.setIsTurningRight(false); break;
					case W: case UP:
						debugCar.setIsAccelerating(false); break;
					case S: case DOWN:
						debugCar.setIsDecelerating(false); break;
					case SPACE: case SHIFT:
						debugCar.setIsBraking(false); break;
				}
			}
		});
	}
}
