package network;

import java.util.Collection;
import java.util.function.DoubleUnaryOperator;

/**
 * Represents a hidden node. A HiddenNode has connections going both in and out. A
 * HiddenNode applies an activation function to received results from incoming
 * connections.
 */
public final class HiddenNode extends Node {
	private final DoubleUnaryOperator actFunc;

	public HiddenNode(long id,
					  Collection<Connection> inputs,
					  Collection<Connection> outputs,
					  DoubleUnaryOperator activationFunction) {
		super(id, inputs, outputs);
		actFunc = (activationFunction == null) ? Math::tanh : activationFunction;
	}


	private double result;

	@Override
	double read() {
		if (!isCached()) {
			result = actFunc.applyAsDouble(super.read());
		}
		return result;
	}
}