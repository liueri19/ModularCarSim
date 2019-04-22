package network;

import util.IdentityHashSet;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.function.DoubleUnaryOperator;

/**
 * Represents a hidden node. A HiddenNode has connections going both in and out. A
 * HiddenNode applies an activation function to received results from incoming
 * connections.
 */
public final class HiddenNode extends Node<HiddenNode> {
	private final DoubleUnaryOperator actFunc;

	/**
	 * Creates a HiddenNode in the Network.
	 * The {@code activationFunction} must be stateless for proper cloning.
	 */
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


	private HiddenNode(
			HiddenNode original,
			IdentityHashMap<Object, Object> clones,
			IdentityHashSet<Object> cloning) {
		super(original, clones, cloning);
		this.actFunc = original.actFunc;
	}

	@Override
	public HiddenNode copy(
			IdentityHashMap<Object, Object> clones,
			IdentityHashSet<Object> cloning) {
		if (cloning.contains(this))
			return null;

		cloning.add(this);

		final HiddenNode clone;
		if (clones.containsKey(this))
			clone = (HiddenNode) clones.get(this);
		else
			clone = new HiddenNode(this, clones, cloning);

		cloning.remove(this);
		clones.put(this, clone);
		return clone;
	}
}
