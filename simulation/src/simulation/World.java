package simulation;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The world where the car will be running around in.
 */
public final class World extends Application {
	private static final int WIDTH = 1280, HEIGHT = 720;

	private volatile boolean done = false;
	void terminate() { done = true; }

	// getters for CarEvaluator
	private Car car;
	Car getCar() { return car; }

	private static World world;
	static World getWorld() { return world; }

	// number of updates consumed in this simulation run
	private volatile AtomicLong operations = new AtomicLong();
	long getOperations() { return operations.longValue(); }

	

	@Override
    public void start(Stage primaryStage) {
		world = this;

		primaryStage.setResizable(false);

		car = new Car(WIDTH / 2, HEIGHT / 2);

		final Pane root = new Pane(car.getDisplay());

		final Scene scene = new Scene(root, WIDTH, HEIGHT);

		setUpKeyHandlers(scene, car);

		primaryStage.setTitle("CarSim");

		primaryStage.setOnCloseRequest(event -> terminate());

		primaryStage.setScene(scene);
		primaryStage.show();

		// run simulation
		new Thread(() -> {
			while (!done) {
				try { Thread.sleep(10); }
				catch (InterruptedException e) {
					System.err.println("Simulation Interrupted");
					Thread.currentThread().interrupt();
					terminate();
				}

				car.update();
				// TODO instead of updating car, update view of surrounding

				operations.getAndIncrement();
			}
		}).start();
    }


	// for debug only
	public static void main(String... args) { launch(args); }

	// key press handling for debug
    private static void setUpKeyHandlers(Scene scene, Car car) {
		scene.setOnKeyPressed(event -> {
			switch (event.getCode()) {
				case A: case LEFT:
					car.setIsTurningLeft(true); break;
				case D: case RIGHT:
					car.setIsTurningRight(true); break;
				case W: case UP:
					car.setIsAccelerating(true); break;
				case S: case DOWN:
					car.setIsDecelerating(true); break;
				case SPACE: case SHIFT:
					car.setIsBraking(true); break;
			}
		});

		scene.setOnKeyReleased(event -> {
			switch (event.getCode()) {
				case A: case LEFT:
					car.setIsTurningLeft(false); break;
				case D: case RIGHT:
					car.setIsTurningRight(false); break;
				case W: case UP:
					car.setIsAccelerating(false); break;
				case S: case DOWN:
					car.setIsDecelerating(false); break;
				case SPACE: case SHIFT:
					car.setIsBraking(false); break;
			}
		});
	}
}
