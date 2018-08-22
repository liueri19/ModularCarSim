package network;

import java.util.Collection;

/**
 * Represents an input node. An InputNode does not have connections coming in, only
 * connections going out. It does not have an activation function.
 */
public final class InputNode extends ExitOnlyNode {

	public InputNode(long id, Collection<Connection> outputs) {
		super(id, outputs);
	}

	@Override
	InputNode copy() {
		return null;
	}

	private double value;

//	@Override
	void write(double value) {
		this.value = value;
	}

	@Override
	double read() { return value; }
}
