package service;

import network.EvaluatedNetwork;
import network.Network;

import java.util.Collection;

/**
 * An Evolver can provide an initial population to be used in neural evolution and produce
 * new generations of networks.
 */
public interface Evolver {
	/** Generates an initial population. */
	Collection<Network> initPopulation(int populationSize, int numInputs, int numOutputs);

	/** Generates a single Network. */
	Network initNetwork(int numInputs, int numOutputs);

	/**
	 * Produces the next generation based on the previous generation.
	 * @param prevGen    the previous generation
	 * @param nextGenSize    the number of Networks in the next generation
	 * @param harshness    the ratio of Networks allowed to survive in the current generation
	 * @return	the next generation
	 */
	Collection<Network> nextGeneration(
			Collection<? extends EvaluatedNetwork> prevGen,
			int nextGenSize,
			double harshness);
}
