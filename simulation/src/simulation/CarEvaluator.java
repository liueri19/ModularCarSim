package simulation;

import network.Network;
import service.Evaluator;

public final class CarEvaluator implements Evaluator {

	/*
	Evaluator must accommodate for life cycle of the javafx application, i.e.,
	Application class (World) can only be initialized once, therefore simulation and
	display of which must be implemented with one single run
	 */
	private static volatile boolean javaFxStarted = false;

	/**
	 * Evaluates the specified Network.
	 * This method starts the JavaFX runtime the first time it is invoked across all
	 * instances.
	 * @param network	evaluation target
	 * @return the fitness of the network
	 */
	@Override
	public double evaluate(Network network) {
		if (!javaFxStarted) {
			// initializes the World class, which would init JavaFX
			World.main();
			javaFxStarted = true;
		}

		World world = World.getWorld();
		while (world == null)	// spin wait for non-null value
			world = World.getWorld();

		// TODO evaluate car progress

		final long operations = world.getOperationsConsumed();

		final double progress = 0;

		return progress * progress / operations;
	}
}
