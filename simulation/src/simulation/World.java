package simulation;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;
import logging.Logger;
import network.Network;
import util.ConfigLoader;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * The world where the car will be running around in.
 */
public final class World extends Application {


	/** A manually controlled car for debug */
	private final Car debugCar = new Car(0, 0, Color.rgb(0, 255, 0, 0.5));
	/** A dummy object for debug, designed to do nothing. */
	private final Driver debugDriver = new DebugDriver();
	/** debug flag. if true, debugCar will be displayed. */
	private volatile boolean debug =
			Boolean.parseBoolean(ConfigLoader.getConfig().getProperty("debug"));



	private static final int WIDTH = 1280, HEIGHT = 720;
	private static final double CENTER_X = WIDTH/2d, CENTER_Y = HEIGHT/2d;

	/** Terminates if no Car moves for IDLE_THRESHOLD milliseconds. */
	private static final long IDLE_THRESHOLD = 5000;


	/** Controls the termination of the simulation thread. */
	private volatile boolean done = false;
	/** Only true when terminate() method is used. Mark this World as irrecoverably terminated. */
	private volatile boolean terminated = false;
	/** Hard terminate, shuts down JavaFX runtime. */
	private void terminate() {
		done = true;
		terminated = true;
		getGraphicsHandler().stop();
		stage.close();
	}


	// singleton
	private volatile static World instance;
	static World getWorld() {
		while (instance == null || !instance.isGraphicsReady)
			Thread.onSpinWait();

		return instance;
	}

	// number of updates consumed in this simulation run
	private final AtomicLong opsCount = new AtomicLong();


	private final Track TRACK = Track.load(ConfigLoader.getConfig().getProperty("track"));

	/**
	 * No conceptual significance, purely for implementation convenience.
	 * Used to both display Car graphics and fetch controls from Drivers.
	 */
	private final Map<Car, Driver> carToDrivers = new ConcurrentHashMap<>();

	/** The list is for external use via the getter. It does not have the debug Driver. */
	private final List<Driver> drivers = new ArrayList<>();
	List<Driver> getDrivers() { return new ArrayList<>(drivers); }
	/**
	 * Constructs a new Driver and Car for the specified Network and adds them to this
	 * World for evaluation and graphics.
	 */
	void addDriver(final Network network) {
		final Car car = new Car();
		final Driver driver = new Driver(TRACK, car, network);

		drivers.add(driver);
		carToDrivers.put(car, driver);
		// add to display
		Platform.runLater(() -> root.getChildren().add(car.asShape()));
	}
	void addDrivers(final Collection<? extends Network> networks) {
		networks.forEach(this::addDriver);
	}


	/** Updated simulation thread, used by graphics thread. */
	private volatile Car leader;

	private volatile boolean simRan = false;
	/** Runs a simulation after the desired setup has been arranged. */
	synchronized void runSimulation() {
		if (!simRan)
			simRan = true;
		else
			throw new IllegalStateException("this simulation has run or is running");

		// start graphics
		Platform.runLater(() -> getGraphicsHandler().play());

		// run sim
		try {
			// for checking idle time and terminate if exceeding IDLE_THRESHOLD
			long idleTimestamp = System.currentTimeMillis();

			while (!done) {

				Thread.sleep(10);

				// for each car, true if car is fine, false if crashed
				final Map<Car, Boolean> crashStatus = new ConcurrentHashMap<>();

				Platform.runLater(() -> {
					synchronized (graphicsLock) {
						for (final var car : carToDrivers.keySet()) {
							crashStatus.put(
									car,
									Shape.subtract(car.asShape(), TRACK.asShape())
											.getBoundsInLocal().isEmpty()
							);
						}
					}
				});

				// wait for the runLater to complete
				while (crashStatus.size() != carToDrivers.size()) {
					// Irrecoverable termination. caused by terminate()
					if (terminated)
						return;

					if (done) {
					/*
					Manually terminated. This may happen as a result of terminate() or
					stopSimulation(). Stop waiting and assume unchecked Cars to be not
					crashed.
					 */
						carToDrivers.forEach((car, v) -> crashStatus.putIfAbsent(car, true));
						break;
					}

					Thread.onSpinWait();
				}


				// check for collision with track edges
				for (final var it = carToDrivers.entrySet().iterator(); it.hasNext(); ) {

					final var entry = it.next();
					final Car car = entry.getKey();
					final Driver driver = entry.getValue();

//					Logger.logf("%s: (%f. %f)%n", car, car.getX(), car.getY());
					if (!crashStatus.get(car)) {
						// drove out of the track
						Logger.logf("CRASH: %s at (%f. %f)%n", car, car.getX(), car.getY());
						driver.setDistance(driver.getCar().getDistance());
						driver.setOperations(opsCount.get());

						it.remove();
					}
				}

				// let networks do their thing
				carToDrivers.values().forEach(Driver::drive);

				// update car position (no graphics here)
				carToDrivers.keySet().forEach(Car::update);

				leader = findBestBy(carToDrivers.keySet(), Car::getDistance);

				opsCount.getAndIncrement();

				// if any car is moving, reset timestamp
				if (carToDrivers.keySet().stream().anyMatch(car -> car.getSpeed() != 0))
					idleTimestamp = System.currentTimeMillis();
				else
					done = System.currentTimeMillis() - idleTimestamp >= IDLE_THRESHOLD;
			}

			// handle un-crashed cars
			carToDrivers.forEach((k, driver) -> {
				if (driver.getOperations() < 0)
					driver.setOperations(opsCount.get());
			});
		}
		catch (InterruptedException e) {
			System.err.println("Simulation interrupted");
			Thread.currentThread().interrupt();
			terminate();
		}
	}

	/** Resets the operation counter and remove all drivers. */
	synchronized void reset() {
		if (terminated)
			throw new IllegalStateException("World has been irrecoverably terminated");

		done = false;
		opsCount.set(0);
		drivers.clear();
		carToDrivers.clear();
		// remove all Car displays
		Platform.runLater(() -> root.getChildren().removeIf(e -> e instanceof Rectangle));
		simRan = false;
	}



	/* ****************************************
	Everything below is mostly display related.
	******************************************/

	private volatile Stage stage;
	/** the root node upon which entities to be displayed may be added */
	private final Pane root = new Pane();
	private final Scene scene = new Scene(root, WIDTH, HEIGHT);

	private volatile Timeline graphicsHandler;
	private Timeline getGraphicsHandler() {
		while (graphicsHandler == null) Thread.onSpinWait();
		return graphicsHandler;
	}

	private volatile boolean isGraphicsReady = false;


	/** Force the read/write of graphics properties to be atomic. */
	private final Object graphicsLock = new Object();


	/** Desired FPS for display. */
	private final double TARGET_FPS = 60;

	/** The content of the runnable is executed once every graphics update. */
	private final Runnable graphicsLoop = () -> {
		// TODO fix graphic jitters
		synchronized (graphicsLock) {
			carToDrivers.keySet().forEach(Car::updateGraphics);

			// graphics thread may get ahead
			if (leader == null) return;

			root.setTranslateX(CENTER_X - leader.getX());
			root.setTranslateY(CENTER_Y + leader.getY());
		}
	};


	@Override
	public void init() throws Exception {
		super.init();

		root.getChildren().add(TRACK.asShape());

		// debug
		if (debug) {
			carToDrivers.put(debugCar, debugDriver);
			root.getChildren().add(debugCar.asShape());
		}

		// setup graphics loop
		graphicsHandler = new Timeline(
				new KeyFrame(
						Duration.seconds(1 / TARGET_FPS),
						ea -> graphicsLoop.run())
		);
		graphicsHandler.setCycleCount(Animation.INDEFINITE);
	}

	@Override
    public void start(Stage stage) {
		instance = this;
		this.stage = stage;

		stage.setResizable(false);

		setUpKeyHandlers(scene);

		stage.setTitle("CarSim");

		stage.setOnCloseRequest(event -> terminate());

		stage.setScene(scene);
		stage.show();

		isGraphicsReady = true;
    }


	/** Debug and used by SimEvaluator to launch JavaFX. */
	public static void main(String... args) {
		final var thread = new Thread(() -> launch(args));
		thread.setName("WorldGraphics");    // for debug
		thread.start();
	}

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
					instance.terminate();
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


	/**
	 * Searches through the given Collection for the best as specified by the keyExtractor.
	 */
	private static <T, K extends Comparable<K>> T findBestBy(
			final Collection<? extends T> objects,
			final Function<? super T, K> keyExtractor) {
    	T best = null;
    	K bestKey = null;

    	for (final T obj : objects) {
    		final K key = keyExtractor.apply(obj);
    		if (best == null || bestKey == null ||
				        bestKey.compareTo(key) < 0) {
    			best = obj;
    			bestKey = key;
		    }
	    }

    	return best;
	}
}
