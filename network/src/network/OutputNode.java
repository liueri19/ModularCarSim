package network;

import java.util.Collection;
import java.util.function.DoubleUnaryOperator;

/**
 * An output node takes input from incoming connections and apply a specified isActivated
 * function to the sum. There are not exiting connections from an OutputNode, and the
 * result can be read using {@link OutputNode#read()}.
 */
public final class OutputNode extends Node {
	private final DoubleUnaryOperator actFunc;

	public OutputNode(long id,
					  Collection<Connection> inputs,
					  DoubleUnaryOperator activationFunction) {
		super(id, inputs, null);
		// activation function defaults to tanh
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
