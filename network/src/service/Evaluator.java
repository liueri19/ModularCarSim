package service;

import network.Network;

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
}
