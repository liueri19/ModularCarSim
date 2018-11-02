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


		final int populationSize, numInputs, numOutputs;
		populationSize =
				Integer.parseInt(config.getProperty("population_size"));
		numInputs =
				Integer.parseInt(config.getProperty("num_input_nodes"));
		numOutputs =
				Integer.parseInt(config.getProperty("num_output_nodes"));

		final Collection<Network> population =
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
