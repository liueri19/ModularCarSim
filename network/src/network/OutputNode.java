package network;

import util.IdentityHashSet;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.function.DoubleUnaryOperator;

/**
 * An output node takes input from incoming connections and apply a specified isActivated
 * function to the sum. There are not exiting connections from an OutputNode, and the
 * result can be read using {@link OutputNode#read()}.
 */
public final class OutputNode extends Node<OutputNode> {
	private final DoubleUnaryOperator actFunc;

	/**
	 * Creates an OutputNode of a Network.
	 * The {@code activationFunction} must be stateless for proper cloning.
	 */
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


	// cloning

	private OutputNode(OutputNode original,
					  IdentityHashMap<Object, Object> clones,
					  IdentityHashSet<Object> cloning) {
		super(original, clones, cloning);
		actFunc = original.actFunc;
	}

	@Override
	public OutputNode copy(IdentityHashMap<Object, Object> clones, IdentityHashSet<Object> cloning) {
		cloning.add(this);

		final OutputNode clone;
		if (clones.containsKey(this))
			clone = (OutputNode) clones.get(this);
		else
			clone = new OutputNode(this, clones, cloning);

		cloning.remove(this);
		return clone;
	}

	@Override
	public void fixNulls(OutputNode original, IdentityHashMap<Object, Object> clones) {
		super.fixNulls(original, clones);
	}
}
