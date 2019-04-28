package xor;

import network.Network;
import service.Evaluator;

import java.util.List;

public final class XorTest implements Evaluator {
	@Override
	public double evaluate(Network network) {
		double fitness = 0;

		fitness += test(network, 0, 0d, 0d);
		fitness += test(network, 1, 0d, 1d);
		fitness += test(network, 1, 1d, 0d);
		fitness += test(network, 0, 1d, 1d);

		// size penalty
		final int size = network.getHiddens().size();
		if (size > 3)
			fitness *= (50 - size)/50d;

//		fitness = Math.pow(fitness, 2);
		return fitness;
	}

	private static double test(
			final Network network,
			final double expectedValue,
			final Double... inputs) {

		final double output = network.compute(List.of(inputs)).get(0);
		double error;
//		error = expectedValue - output;
		error = Math.abs(expectedValue - output);

		double fitness;
		// Delta fitness = C_1 * e^(-|u - C_2 x|)
		fitness = Math.exp(-error);
		// Delta fitness = C_1 * (-(u - x)^2 + 1)
//		fitness = (-Math.pow(error, 2) + 1);
//		if (expectedValue > 0)
//			fitness = (output > expectedValue) ? 1 : output;
//		else
//			fitness = (output < expectedValue) ? 1 : 1-output;

		return fitness;
	}

	private static double error(
			final Network network,
			final double expectedValue,
			final Double... inputs) {
		final double output = network.compute(List.of(inputs)).get(0);
		return Math.abs(expectedValue - output);
	}
}
