package service;

import network.Network;

import java.util.*;

/**
 * An Evaluator scores a Network.
 */
public interface Evaluator {
	/**
	 * Evaluates the specified Network.
	 * @param network	evaluation target
	 * @return	the score achieved by the Network
	 */
	double evaluate(Network network);

	/**
	 * Evaluates the specified Networks and sort the networks in the order of decreasing
	 * fitness.
	 * @param networks  the networks to be evaluated
	 * @return  a sorted Map of Networks to scores
	 */
	default Map<Network, Double> evaluate(Collection<? extends Network> networks) {
		// need to preserve order, doing that with map is too awkward
		final List<Map.Entry<Network, Double>> scores = new ArrayList<>();

		// evaluate networks and add them to scores
		networks.forEach(network -> scores.add(
				Map.entry(network, this.evaluate(network))
		));

		// sort scores
		scores.sort(Map.Entry.<Network, Double>comparingByValue().reversed());

		// LinkedHashMap to preserve ordering
		final Map<Network, Double> result = new LinkedHashMap<>();
		scores.forEach(entry -> result.put(entry.getKey(), entry.getValue()));

		return result;
	}
}
