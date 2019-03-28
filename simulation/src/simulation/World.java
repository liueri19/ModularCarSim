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
	/** debug flag. if true, debugCar will be displayed. */
	private volatile boolean debug =
			Boolean.parseBoolean(ConfigLoader.getConfig().getProperty("debug"));

	private final Logger LOGGER =
			new Logger(System.out);
//			new Logger("sim_" + LocalDateTime.now() + ".log");



	private static final int WIDTH = 1280, HEIGHT = 720;
	private static final double CENTER_X = WIDTH/2d, CENTER_Y = HEIGHT/2d;

	/** Controls the termination of the simulation thread. */
	private volatile boolean done = false;
	void terminate() {
		done = true;
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

	/** Each driver controls a car. */
	private final List<Driver> drivers = new ArrayList<>();

	/** No conceptual significance, purely for implementation convenience. */
	private final Map<Car, Driver> carToDrivers = new HashMap<>();


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
	List<Driver> getDrivers() { return new ArrayList<>(drivers); }


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
			while (!done) {

				Thread.sleep(10);

				// for each car, true if car is fine, false if crashed
				final Map<Car, Boolean> crashStatus = new ConcurrentHashMap<>();

				synchronized (graphicsLock) {
					Platform.runLater(() -> {
						for (final var car : carToDrivers.keySet()) {
							crashStatus.put(
									car,
									Shape.subtract(car.asShape(), TRACK.asShape())
											.getBoundsInLocal().isEmpty()
							);
						}
					});
				}

				// wait for the runLater to complete
				while (crashStatus.size() != carToDrivers.size())
					Thread.onSpinWait();


				// check for collision with track edges
				for (final var it = carToDrivers.entrySet().iterator(); it.hasNext(); ) {

					final var entry = it.next();
					final Car car = entry.getKey();
					final Driver driver = entry.getValue();

					LOGGER.logf("%s: (%f. %f)%n", car, car.getX(), car.getY());
					if (!crashStatus.get(car)) {
						// drove out of the track
						LOGGER.logf("CRASH: %s at (%f. %f)%n", car, car.getX(), car.getY());
						// TODO uncomment this after network stuff is implemented
//						driver.setDistance(driver.getCar().getDistance());
//						driver.setOperations(opsCount.get());

						it.remove();
					}
				}

				// let networks do their thing
				// TODO uncomment this after network stuff is implemented
//				drivers.forEach(Driver::drive);

				// update car position (no graphics here)
				carToDrivers.keySet().forEach(Car::update);

				leader = findBestBy(carToDrivers.keySet(), Car::getDistance);

				opsCount.getAndIncrement();
			}
		}
		catch (InterruptedException e) {
			System.err.println("Simulation interrupted");
			Thread.currentThread().interrupt();
			terminate();
		}
	}

	/** Resets the operation counter and remove all drivers. */
	synchronized void reset() {
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
//	/** Gets with spin wait to prevent NPE. */
//	private Stage getStage() {
//		while (stage == null) Thread.onSpinWait();
//		return stage;
//	}
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
			carToDrivers.put(debugCar, null);
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
