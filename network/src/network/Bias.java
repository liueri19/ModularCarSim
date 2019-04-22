package network;

import util.IdentityHashSet;

import java.util.Collection;
import java.util.IdentityHashMap;

/**
 * Represents a bias node.
 */
public final class Bias extends ExitOnlyNode<Bias> {

	private final long id;
	private final double value;

	/**
	 * Constructs a Bias node with the specified constant value as the output.
	 */
	public Bias(long id, double value, Collection<Connection> outputs) {
		super(id, outputs);
		this.id = id;
		this.value = value;
	}

	@Override
	double read() {
		return value;
	}

	@Override
	public long getId() {
		return id;
	}



	private Bias(
			Bias original,
			IdentityHashMap<Object, Object> clones,
			IdentityHashSet<Object> cloning) {
		super(original, clones, cloning);
		this.id = original.id;
		this.value = original.value;
	}

	@Override
	public Bias copy(
			IdentityHashMap<Object, Object> clones,
			IdentityHashSet<Object> cloning) {
		if (cloning.contains(this))
			return null;

		cloning.add(this);

		final Bias clone;
		if (clones.containsKey(this))
			clone = (Bias) clones.get(this);
		else
			clone = new Bias(this, clones, cloning);

		cloning.remove(this);
		clones.put(this, clone);
		return clone;
	}
}
