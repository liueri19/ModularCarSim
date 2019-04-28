package main;

import logging.Logger;
import network.Network;
import service.Evaluator;
import service.Evolver;
import util.ConfigLoader;

import java.util.*;

public final class Main {

	public static void main(String... args) {

		Logger.addOutput(System.err);

		/*
		argument list:
			path to config file
		 */

		if (args == null || args.length < 1)
			throw new IllegalArgumentException("Need path to config file");


		final ServiceLoader<Evaluator> evaluators = ServiceLoader.load(Evaluator.class);
		final ServiceLoader<Evolver> evolvers = ServiceLoader.load(Evolver.class);

		final Evaluator evaluator = evaluators.findFirst().orElseThrow(() -> {
			throw new RuntimeException("No Evaluator service found");
		});

		final Evolver evolver = evolvers.findFirst().orElseThrow(() -> {
			throw new RuntimeException("No Evolver service found");
		});

		Logger.logln("Evaluator: " + evaluator);
		Logger.logln("Evolver:   " + evolver);

		ConfigLoader.loadConfig(args[0]);
		final Properties config = ConfigLoader.getConfig();


		// number of networks in each generation
		final int populationSize =
				Integer.parseInt(config.getProperty("population_size"));
		// number of input nodes in each network
		final int numInputs =
				Integer.parseInt(config.getProperty("num_input_nodes"));
		// number of output nodes in each network
		final int numOutputs =
				Integer.parseInt(config.getProperty("num_output_nodes"));
		// ratio of number of networks to be eliminated each new generation
		// e.g. a harshness of 0.75 means 75% of networks will not survive and only the
		// top 25% will survive to the next generation
		final double harshness =
				Double.parseDouble(config.getProperty("harshness"));
		final double minFitness =
				Double.parseDouble(config.getProperty("min_fitness"));

		Logger.logln("Initializing generation 0");
		// init first generation, to be updated later, must be mutable
		Collection<Network> population =
				evolver.initPopulation(populationSize, numInputs, numOutputs);


		double bestFitness;
		int generationCount = 0;
		do {
//			Logger.logln("Evaluating generation " + generationCount);
			// evaluate networks
			final Map<Network, Double> evaluatedNetworks = evaluator.evaluate(population);
			bestFitness = evaluatedNetworks.values().iterator().next(); // first element

			generationCount++;
//			Logger.logln("Initializing generation " + generationCount);
			// next generation
			population = evolver.nextGeneration(evaluatedNetworks, populationSize, harshness);

			if (generationCount % 50 == 0)
				Logger.logf("generation: %s; best: %f%n", generationCount, bestFitness);

		} while (bestFitness < minFitness);
//		} while (true);

		Logger.logf("generation: %s; best: %f%n", generationCount, bestFitness);


		// TODO write champ to file (implement NetworkIO)
	}
}
