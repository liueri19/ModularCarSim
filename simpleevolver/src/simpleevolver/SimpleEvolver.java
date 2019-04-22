package simpleevolver;

import logging.Logger;
import network.*;
import service.Evolver;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
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

		// connect input to output nodes
		for (Node input : network.getInputs()) {
			for (Node output : network.getOutputs()) {
				final double weight = random.nextDouble();
				network.connect(input, output, weight);
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

		final List<Map.Entry<? extends Network, ? extends Double>> entries =
				new ArrayList<>(prevGenToFitness.entrySet());

		// eliminate
		final long numSurvivors =
				Math.round(prevGenToFitness.size() * (1 - harshness));

		Comparator<Double> comp = Double::compare;
		comp = comp.reversed();
		// sort networks by fitness
		// also scramble the order of equal fitness networks
		final var sortedEntries = scrambleSort(entries, Map.Entry::getValue, comp);
		final List<Network> survivors =
				sortedEntries.stream()
						.map(Map.Entry::getKey)
						.limit(numSurvivors)
						.collect(Collectors.toList());


		// generate new networks
		final List<Network> nextGen = new ArrayList<>(survivors);
		while (nextGen.size() < nextGenSize) {
			final Network survivor = survivors.get(random.nextInt(survivors.size()));

			// clone survivor
			final Network clone = survivor.copy();

			if(random.nextBoolean()) {
				randomlyChangeWeight(clone);
			}
			else {
				if (random.nextBoolean())
					randomlyAddConnection(clone);
				if (random.nextBoolean())
					randomlyAddNode(clone);
			}

			nextGen.add(clone);
		}


		return nextGen;
	}


	/**
	 * Randomly changes the weight of a single connection in the network.
	 */
	private void randomlyChangeWeight(Network network) {
		// don't do anything if empty
		final var connections = network.getConnections();
		if (connections.size() == 0) return;

		final var it = connections.iterator();
		final int randInt = random.nextInt(connections.size());

		// does not support indexed access, iterate to desired index
		for (int i = 0; i < randInt; i++) it.next();
		final var connection = it.next();

		// random weight
		connection.setWeight(random.nextDouble());
	}

	/**
	 * Randomly adds a Node to a copy of the specified Network. The new node may be
	 * placed on an existing connection and splits the connection, or a bias node creating
	 * a new connection.
	 */
	private void randomlyAddNode(Network network) {
		// add a random bias
		if (random.nextBoolean()) {
			final var bias =
					new NodeBuilder(NodeType.BIAS).setValue(random.nextDouble()).build();

			// random target to connect to
			final var tos = new ArrayList<Node<?>>();
			tos.addAll(network.getHiddens());
			tos.addAll(network.getOutputs());

			network.putNode(bias);

			final var target = tos.get(random.nextInt(tos.size()));

			network.tryConnect(bias, target, random.nextDouble());
		}
		// add new node and split connection
		else {
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
			final double weight = random.nextDouble();

			// avoid cycle
			// Find a path to -> from. If path exist, a cycle would be introduced by adding
			// the connection from -> to.
//			final boolean pathExists = network.findPath(to, from);
//			network.leaveAll();
//			if (!pathExists) {
//				network.connect(from, to, random.nextDouble());
//				return;
//			}
			if (network.tryConnect(from, to, weight)) {
				return;
			}
		}

//		Logger.logln("Options depleted when trying to add random Connection");
	}


	/**
	 * Sorts the specified List and deliberately scrambles the sections with equal
	 * elements without breaking the sort order.
	 * @param toKeyFunction converts elements in original to keys for the Comparator
	 */
	private <T, K> List<T> scrambleSort(
			final List<? extends T> original,
			final Function<? super T, K> toKeyFunction,
			final Comparator<? super K> comparator) {
		// divide into buckets based on toKeyFunction
		final SortedMap<K, List<T>> buckets = new TreeMap<>(comparator);
		// for every element in original
		for (final T element : original) {
			// computing key
			buckets.compute(toKeyFunction.apply(element), (key, bucket) -> {
				// if bucket for key is null, make one
				if (bucket == null) {
					final List<T> newBucket = new ArrayList<>();
					newBucket.add(element);
					return newBucket;
				}
				// otherwise add to existing bucket
				else {
					bucket.add(element);
					return bucket;
				}
			});
		}

		// scramble each bucket
//		buckets.values().forEach(Collections::shuffle);
		for (final var list : buckets.values())
			Collections.shuffle(list);

		// combine and return
		return buckets.values().stream()
				       .flatMap(List::stream)
				       .collect(Collectors.toList());
	}
}
