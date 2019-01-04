package main;

import network.Network;
import service.Evaluator;
import service.Evolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public class Main {

	public static void main(String... args) {
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

		final Properties config = loadConfig(args[0]);


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
		final int harshness =
				Integer.parseInt(config.getProperty("harshness"));

		// init first generation, to be updated later, must be mutable
		Collection<Network> population =
				evolver.initPopulation(populationSize, numInputs, numOutputs);


		while (true) {	// TODO implement this
			// evaluate networks
			final Map<Network, Double> scores =
					population.stream().collect(Collectors.toMap(n -> n, evaluator::evaluate));
		}

		// TODO find champ
	}


	private static Properties loadConfig(String file) {
		final Properties config = new Properties();

		try {
			config.load(Files.newInputStream(Paths.get(file)));
		}
		catch (IOException e) {
			System.err.println("Failed to read config file '" + file + "'");
			e.printStackTrace();
		}

		return config;
	}
}
