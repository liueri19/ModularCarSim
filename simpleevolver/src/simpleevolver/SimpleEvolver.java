package simpleevolver;

import logging.Logger;
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

		// randomly connect input to output nodes
		for (Node input : network.getInputs()) {
			for (Node output : network.getOutputs()) {
				final boolean doConnect = random.nextBoolean();

				if (doConnect) {
					final double weight = random.nextDouble();
					network.connect(input, output, weight);
				}
			}
		}

		return network;
	}


	/**
	 * Removes the bottom portion of the population as specified by harshness, then
	 * multiply the survivors by adding random mutations.
	 * @param harshness ratio of number of networks to be eliminated
	 */
	@Override
	public Collection<Network> nextGeneration(
			final Map<? extends Network, ? extends Double> prevGenToFitness,
			final int nextGenSize,
			final double harshness) {
		if (nextGenSize < 0)
			throw new IllegalArgumentException("nextGenSize cannot be negative");
		if (harshness < 0 || harshness > 1)
			throw new IllegalArgumentException("harshness must be between 0 and 1 inclusive");

		// sort networks by fitness
		final List<Map.Entry<? extends Network, ? extends Double>> entries =
				new ArrayList<>(prevGenToFitness.entrySet());
		entries.sort(Comparator.comparingDouble(Map.Entry::getValue));

		final List<Network> sortedNetworks = new ArrayList<>();
		entries.forEach(entry -> sortedNetworks.add(entry.getKey()));

		// eliminate
		final long numSurvivors =
				Math.round(prevGenToFitness.size() * (1 - harshness));

		final List<Network> survivors =
				sortedNetworks.stream()
						.limit(numSurvivors)
						.collect(Collectors.toList());


		// generate new networks
		final List<Network> nextGen = new ArrayList<>(survivors);
		while (nextGen.size() < nextGenSize) {
			final Network survivor = survivors.get(random.nextInt(survivors.size()));

			// clone survivor
			final Network clone = survivor.copy();

			if (random.nextBoolean())
				randomlyAddConnection(clone);
			if (random.nextBoolean())
				randomlyAddNode(clone);

			nextGen.add(clone);
		}


		return nextGen;
	}



	/**
	 * Randomly adds a Node to a copy of the specified Network.
	 */
	private void randomlyAddNode(Network network) {
		// prepare connections, size of that, and a random connection to add Node to
		final var connections = network.getConnections();
		final var size = connections.size();
		if (size == 0) return;

		final var target = random.nextInt(size);

		// does not support indexed access, iterate elements to skip to target
		final var it = connections.iterator();
		for (int i = 0; i < target; i++) it.next();

		network.addNode(it.next());
	}

	/**
	 * Randomly adds a Connection to a copy of the specified Network.
	 */
	private void randomlyAddConnection(Network network) {
		// options to randomly select from
		final var froms = new ArrayList<Node<?>>();
		froms.addAll(network.getInputs());
		froms.addAll(network.getHiddens());

		final var tos = new ArrayList<Node<?>>();
		tos.addAll(network.getHiddens());
		tos.addAll(network.getOutputs());

		while (!froms.isEmpty() && !tos.isEmpty()) {
			// a random connection
			final int fromIndex = random.nextInt(froms.size());
			final var from = froms.remove(fromIndex);
			final int toIndex = random.nextInt(tos.size());
			final var to = tos.remove(toIndex);

			// avoid cycle
			// Find a path to -> from. If path exist, a cycle would be introduced by adding
			// the connection from -> to.
			final boolean pathExists = network.findPath(to, from);
			network.leaveAll();
			if (!pathExists) {
				network.connect(from, to, random.nextDouble());
				break;
			}
		}

		Logger.logln("Options depleted when trying to add random Connection");

	}
}
