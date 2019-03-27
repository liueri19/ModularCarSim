package simulation;

import network.Network;
import service.Evaluator;

import java.util.*;

public final class SimEvaluator implements Evaluator {

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
	public double evaluate(final Network network) {
		if (!javaFxStarted) {
			// initializes the World class, which would init JavaFX
			World.main();
			javaFxStarted = true;
		}

		World world = World.getWorld();

		world.addDriver(network);

		// blocks until completion
		world.runSimulation();

		// cache and reset
		final double eval = world.getDrivers().get(0).getEvaluation();
		world.reset();

		return eval;
	}


	/**
	 * Evaluates the specified Networks.
	 * This method starts the JavaFX runtime the first time it is invoked across all
	 * instances.
	 * @param networks  the networks to be evaluated
	 * @return  a sorted Map of Networks to scores
	 */
	@Override
	public Map<Network, Double> evaluate(final Collection<? extends Network> networks) {
		if (!javaFxStarted) {
			// initializes the World class, which would init JavaFX
			World.main();
			javaFxStarted = true;
		}

		World world = World.getWorld();

		world.addDrivers(networks);

		// blocks until completion
		world.runSimulation();

		// get result
		final List<Driver> drivers = world.getDrivers();

		// reset simulation
		world.reset();

		// collect results and return
		final Comparator<Driver> driverComparator = Driver::compareTo;

		return drivers.stream()
				       .sorted(driverComparator.reversed())
				       .map(driver -> Map.entry(driver.getNetwork(), driver.getEvaluation()))
				       .collect(HashMap::new,
						       (map, entry) -> map.put(entry.getKey(), entry.getValue()),
						       HashMap::putAll);
	}


	/**
	 * Evaluates the specified driver based on its degree of completion and number of
	 * operations.
	 */
	static double evaluateDriver(final Driver driver) {
		final double completion = driver.getCompletion();
		final double operations = driver.getOperations();

		if (completion < 0)
			throw new IllegalArgumentException("Uninitialized completion in Driver instance " + driver);
		if (operations < 0)
			throw new IllegalArgumentException("Uninitialized operations in Driver instance " + driver);

		return completion * completion * 1E7 - operations;
	}
}
