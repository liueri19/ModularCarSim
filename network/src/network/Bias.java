package network;

import java.util.Collection;

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
}
