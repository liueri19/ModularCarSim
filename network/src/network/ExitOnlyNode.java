package network;

import util.IdentityHashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * An Node that does not have any incoming Connections.
 */
public abstract class ExitOnlyNode<N extends ExitOnlyNode<N>> extends Node<N> {
	protected ExitOnlyNode(long id, Collection<Connection> outputs) {
		super(id, null, outputs);
	}

	protected ExitOnlyNode(
			ExitOnlyNode<N> original,
			IdentityHashMap<Object, Object> clones,
			IdentityHashSet<Object> cloning) {
		super(original, clones, cloning);
	}

	@Override
	boolean addInput(Connection connection) {
		return false;
	}

	@Override
	boolean removeInput(Connection connection) {
		return false;
	}

	@Override
	List<Connection> getInputs() {
		return Collections.emptyList();
	}
}
