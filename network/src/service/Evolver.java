package service;

import network.Network;

import java.util.Collection;
import java.util.Map;

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
	 * @param prevGenToFitness    map of previous generation networks to fitness
	 * @param nextGenSize    the number of Networks in the next generation
	 * @param harshness    the ratio of Networks to be eliminated to the number of
	 *                        Networks in the current generation
	 * @return	the next generation
	 */
	Collection<Network> nextGeneration(
			Map<? extends Network, ? extends Double> prevGenToFitness,
			int nextGenSize,
			double harshness);
}
