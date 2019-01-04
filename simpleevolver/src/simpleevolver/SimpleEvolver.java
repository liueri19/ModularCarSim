package simpleevolver;

import network.EvaluatedNetwork;
import service.Evolver;
import network.Network;
import network.Node;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides a very simple implementation of Evolver that performs no crossovers between
 * networks, survivors of each generation only gets multiplied with possibly random
 * mutations.
 */
public final class SimpleEvolver implements Evolver {

	private final Random random = new Random();


	/**
	 * Constructs a SimpleEvolver.
	 */
	public SimpleEvolver() {}	// ServiceLoader uses this


	/**
	 * Provides a Collection of Networks with only input nodes and output nodes with
	 * random connections.
	 * @throws IllegalArgumentException	if the property 'population_size' in the supplied
	 * properties file is not an integer.
	 */
	@Override
	public Collection<Network> initPopulation(
			int populationSize, int numInputs, int numOutputs)
			throws IllegalArgumentException {
		final Stream<Network> supply =
				Stream.generate(() -> initNetwork(numInputs, numOutputs));

		return supply.limit(populationSize).collect(Collectors.toList());
	}


	/**
	 * Creates a Network with random connection from input to output nodes. The resulting
	 * network has no hidden nodes.
	 * @param numInputs     the number of input nodes of the resulting network
	 * @param numOutputs    the number of output nodes of the resulting network
	 * @return  a random Network with no hidden nodes
	 */
	@Override
	public Network initNetwork(int numInputs, int numOutputs) {
		final Network network = new Network(numInputs, numOutputs, Math::tanh);

		// for each pair of input to output
		for (Node input : network.getInputs()) {
			for (Node output : network.getOutputs()) {
				final boolean doConnect = random.nextBoolean();
				final double weight = random.nextDouble();

				if (doConnect)
					network.connect(input, output, weight);
			}
		}

		return network;
	}


	/**
	 * Removes Networks with insufficient fitness, then multiply the survivors adding
	 * random mutations.
	 * @param harshness ratio of number of networks to be eliminated
	 */
	@Override
	public Collection<Network> nextGeneration(
			Collection<? extends EvaluatedNetwork> prevGen,
			int nextGenSize,
			double harshness) {
		if (nextGenSize < 0)
			throw new IllegalArgumentException("nextGenSize cannot be negative");
		if (harshness < 0 || harshness > 1)
			throw new IllegalArgumentException("harshness must be between 0 and 1 inclusive");

		// eliminate
		final List<EvaluatedNetwork> sortedNetworks = new ArrayList<>(prevGen);
		sortedNetworks.sort(
				Comparator.comparingDouble(EvaluatedNetwork::getFitness).reversed()
		);

		final int numSurvivors =
				Math.toIntExact(Math.round(sortedNetworks.size() * (1 - harshness)));

		final List<Network> survivors =
				sortedNetworks.stream()
						.limit(numSurvivors)
						.map(EvaluatedNetwork::getNetwork)
						.collect(Collectors.toList());


		// generate new networks
		final List<Network> nextGen = new ArrayList<>(survivors);
		while (nextGen.size() < nextGenSize) {
			final Network randomNetwork = survivors.get(random.nextInt(survivors.size()));

			if (random.nextBoolean())
				nextGen.add(randomlyAddNode(randomNetwork));
			else
				nextGen.add(randomlyAddConnection(randomNetwork));
		}


		return nextGen;
	}


	/**
	 * Randomly adds a Node to a copy of the specified Network.
	 * The original network is not modified.
	 */
	private Network randomlyAddNode(Network network) {
		// TODO implement this
		return null;
	}

	/**
	 * Randomly adds a Connection to a copy of the specified Network.
	 * The original network is not modified.
	 */
	private Network randomlyAddConnection(Network network) {
		// TODO implement this
		return null;
	}
}
