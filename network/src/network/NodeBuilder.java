package network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleUnaryOperator;

/**
 * All AbstractNode instances should be constructed using this Builder.
 */
public class NodeBuilder {
	/*
	The IDs helps find matching nodes in a cross over.
	Each NEW node holds a new ID. Nodes construct as a result of a Network copy should not
	be assigned a new ID.
	 */
	private static final AtomicLong NODE_ID = new AtomicLong();
	private static long getNextNodeID() { return NODE_ID.getAndIncrement(); }

	private long id = -1;
	private final NodeType type;
	private Collection<Connection> prevConnections, nextConnections;
	private DoubleUnaryOperator actFunc;
	private double value;    // for bias node

	public NodeBuilder(NodeType nodeType) {
		type = nodeType;
	}

	public long getId() { return id; }
	public NodeBuilder setId(long id) {
		this.id = id;
		return this;
	}

	public Collection<Connection> getInputs() {
		return prevConnections;
	}
	public NodeBuilder setInputs(Collection<Connection> prevConnections) {
		this.prevConnections = prevConnections;
		return this;
	}

	public Collection<Connection> getOutputs() {
		return nextConnections;
	}
	public NodeBuilder setOutputs(Collection<Connection> nextConnections) {
		this.nextConnections = nextConnections;
		return this;
	}

	public DoubleUnaryOperator getActivationFunction() {
		return actFunc;
	}
	public NodeBuilder setActivationFunction(DoubleUnaryOperator actFunc) {
		this.actFunc = actFunc;
		return this;
	}

	public double getValue() {
		return value;
	}

	/**
	 * Sets the value to be used by the constructed Bias node. If the NodeType is not BIAS
	 * at the time of invoking build(), this value is discarded.
	 */
	public NodeBuilder setValue(double value) {
		this.value = value;
		return this;
	}

	/**
	 * Builds a Node based on the specified parameters. It is safe to cast the returned
	 * object to the matching subclass of the specified NodeType.
	 * @return	a Node with the specified parameters
	 */
	public Node build() {
		if (type == null) throw new IllegalStateException("NodeType must be specified");
		if (id < 0) id = getNextNodeID();
		if (prevConnections == null) prevConnections = new ArrayList<>();
		if (nextConnections == null) nextConnections = new ArrayList<>();

//		/*
//		find the node with the largest layer number among incoming connections and add 1
//		for layer number of this node.
//		 */
//		final int layer = prevConnections.stream()
//				                  .map(Connection::getPrevNode)
//				                  .mapToInt(Node::getLayer)
//				                  .max().orElse(0) + 1;

		switch (type) {
			case INPUT:
				return new InputNode(getId(), getOutputs());
			case OUTPUT:
				return new OutputNode(getId(), getInputs(), getActivationFunction());
			case HIDDEN:
				return new HiddenNode(getId(), getInputs(), getOutputs(), getActivationFunction());
			case BIAS:
				return new Bias(getId(), getValue(), getOutputs());
			default:
				throw new RuntimeException("Unknown node type: " + type);
		}
	}
}
