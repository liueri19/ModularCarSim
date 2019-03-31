package network;

import util.DeepCopyable;
import util.IdentityHashSet;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Fills in the basic stuff for implementing a Node.
 */
public abstract class Node<N extends Node<N>> implements Comparable<N>, DeepCopyable<N> {

	/** Used in the depth first search for detecting loops. */
	private boolean visited = false;
	boolean isVisited() { return visited; }
	void visit() { visited = true; }
	/** Sets the visited flag to false. */
	void leave() { visited = false; }


	/** For faster equals() checks. */
	private final long ID;
	public long getId() { return ID; }

	private final List<Connection> inputs = new ArrayList<>();
	private final List<Connection> outputs = new ArrayList<>();


	Node(final long id,
	     Collection<Connection> inputs,
	     Collection<Connection> outputs) {
		ID = id;

		// addAll method does not accept null argument
		if (inputs == null) inputs = Collections.emptyList();
		if (outputs == null) outputs = Collections.emptyList();

		this.inputs.addAll(inputs);
		this.outputs.addAll(outputs);
	}


	private double result;
	private boolean cached = false;
	boolean isCached() { return cached; }



	/** @see Node#encode() */
	static Node parseNode(String strID) throws IllegalArgumentException {
		Objects.requireNonNull(strID);
		final String errorMessage = "Malformatted node: " + strID;

		final String[] parts = strID.split("-");

		if (parts.length != 2)
			throw new IllegalArgumentException(errorMessage);

		final String nodeTypeStr = parts[0];

		final NodeType nodeType;
		try {
			nodeType = NodeType.of(nodeTypeStr);
		}
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(errorMessage, e);
		}

		final long id = Long.parseLong(parts[1], 16);

		return new NodeBuilder(nodeType).setId(id).build();
	}


	/**
	 * Reads a value from this Node. Returns the cached value if available, or fetches
	 * new values from previous Connections if not.
	 */
	double read() {
		if (!isCached()) {
			result = getInputs().stream()
							.mapToDouble(Connection::fetch)
							.sum();
			cached = true;
		}

		return result;
	}

	/**
	 * Resets this Node by discarding the current cached value.
	 */
	void discardCache() { cached = false; }


	boolean addInput(Connection connection) {
		return inputs.add(connection);
	}

	boolean removeInput(Connection connection) {
		return inputs.remove(connection);
	}

	boolean addOutput(Connection connection) {
		return outputs.add(connection);
	}

	boolean removeOutput(Connection connection) {
		return outputs.remove(connection);
	}

	List<Connection> getInputs() {
		return Collections.unmodifiableList(inputs);
	}

	List<Connection> getOutputs() {
		return Collections.unmodifiableList(outputs);
	}


	String encode() {
		return getClass().getSimpleName() + '-' + Long.toHexString(getId());
		// the dash cannot be used in an identifier, no ambiguity possible
	}

	@Override
	public String toString() {
		return encode();
	}

	@Override
	public int compareTo(Node node) {
		return Long.compareUnsigned(getId(), node.getId());
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Node && compareTo((Node) obj) == 0;
	}



	// cloning

	/**
	 * Constructs a deep copy of the specified Node.
	 * @param original	the Node to be copied
	 */
	protected Node(
			Node<N> original,
			IdentityHashMap<Object, Object> clones,
			IdentityHashSet<Object> cloning) {
		this(original.ID,
				original.inputs.stream()
						.map(c -> c.copy(clones, cloning))
						.collect(Collectors.toList()),
				original.outputs.stream()
						.map(c -> c.copy(clones, cloning))
						.collect(Collectors.toList()));

		this.visited = original.visited;
		this.result = original.result;
		this.cached = original.cached;
	}


	// TODO implement fixNulls in subclasses
	@Override
	public void fixNulls(N original, IdentityHashMap<Object, Object> clones) {
		DeepCopyable.fixCollection(original.getInputs(), inputs, clones);
		DeepCopyable.fixCollection(original.getOutputs(), outputs, clones);
	}
}


/**
 * An Node that does not have any incoming Connections.
 */
abstract class ExitOnlyNode<N extends ExitOnlyNode<N>> extends Node<N> {
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
		return null;
	}
}