package network;

import cloning.DeepCopyable;
import cloning.IdentityHashSet;

import java.util.*;
import java.util.function.DoubleUnaryOperator;


/**
 * Represents a neural network.
 * A Network contains the genetic information of a genotype, at the same time also
 * provides the functions of a phenotype.
 */
public class Network implements DeepCopyable<Network> {
	private final List<InputNode> inputs = new ArrayList<>();
	private final List<OutputNode> outputs = new ArrayList<>();

	private final SortedMap<Long, HiddenNode> hiddens = new TreeMap<>();

	private final SortedMap<Long, Connection> connections = new TreeMap<>();


	// cloning

	protected Network(
			Network original,
			IdentityHashMap<Object, Object> clones,
			IdentityHashSet<Object> cloning) {
		for (InputNode node : original.inputs)
			inputs.add(node.copy(clones, cloning));

		for (OutputNode node : original.outputs)
			outputs.add(node.copy(clones, cloning));

		for (Map.Entry<Long, HiddenNode> entry : original.hiddens.entrySet())
			hiddens.put(entry.getKey(), entry.getValue().copy(clones, cloning));

		for (Map.Entry<Long, Connection> entry : original.connections.entrySet())
			connections.put(entry.getKey(), entry.getValue().copy(clones, cloning));
	}


	@Override
	public Network copy(IdentityHashMap<Object, Object> clones, IdentityHashSet<Object> cloning) {
		cloning.add(this);

		final Network clone;
		if (clones.containsKey(this))
			clone = (Network) clones.get(this);
		else
			clone = new Network(this, clones, cloning);

		cloning.remove(this);
		return clone;
	}


	@Override
	public void fixNulls(Network original, IdentityHashMap<Object, Object> clones) {
		DeepCopyable.fixCollection(original.inputs, inputs, clones);
		DeepCopyable.fixCollection(original.outputs, outputs, clones);

		// TODO fix maps maybe?
	}

	////



	/**
	 * Constructs a Network with the specified number of input Nodes and output Nodes.
	 * @param numInputs		number of input Nodes in this Network
	 * @param numOutputs	number of output Nodes in this Network
	 * @param activationFunction	the activation function to be used by the output nodes
	 */
	public Network(int numInputs, int numOutputs, DoubleUnaryOperator activationFunction) {
		for (int i = 0; i < numInputs; i++)
			putNode(new NodeBuilder(NodeType.INPUT).build());

		for (int i = 0; i < numOutputs; i++)
			putNode(
					new NodeBuilder(NodeType.OUTPUT)
							.setActivationFunction(activationFunction)
							.build()
			);
	}

	/**
	 * Constructs a Network with the specified inputs and outputs.
	 * This constructor can be used to specify different activation functions for each
	 * output node.
	 */
	public Network(Collection<InputNode> inputs, Collection<OutputNode> outputs) {
		this.inputs.addAll(inputs);
		this.outputs.addAll(outputs);
	}


	/**
	 * Feeds the specified inputs to the input Nodes of this Network, returning a List
	 * of values from the output Nodes. If the inputs have more entries than there are
	 * input Nodes in this Network, Nodes with no corresponding data is fed with 0; if
	 * there are less entries than Nodes, the extra entries are discarded.
	 */
	public List<Double> compute(List<Double> inputs) {
		// clear cached values in Nodes
		for (Node node : getOutputs())
			recursivelyDiscardCache(node);

		if (inputs.size() != getInputs().size()) {
			throw new IllegalArgumentException(
					"Input size mismatch: expected " + getInputs().size() +
							", found " + inputs.size());
		}

		// write new values
		for (int i = 0; i < this.inputs.size(); i++) {
			InputNode node = this.inputs.get(i);
			double value = inputs.get(i);

			node.write(value);
		}

		// the read triggers evaluation
		List<Double> outputs = new ArrayList<>();
		for (OutputNode out : this.outputs)
			outputs.add(out.read());

		return outputs;
	}

	private void recursivelyDiscardCache(Node<?> node) {
		node.discardCache();

		// recursion ends when node has no input connections
		for (Connection connection : node.getInputs())
			recursivelyDiscardCache(connection.getPrevNode());
	}



	/** Finds a Node in this Network of the same NodeType and the same ID. */
	private <N extends Node> N findNode(N node) {

		List<? extends Node> searchSpace = null;

		if (node instanceof InputNode)
			searchSpace = inputs;
		else if (node instanceof OutputNode)
			searchSpace = outputs;


		Node candidate = null;

		if (searchSpace != null) {	//linear search for list
			for (Node n : searchSpace) {
				if (n.getId() == node.getId()) {
					candidate = n;
					break;
				}
			}
		}
		else
			candidate = hiddens.get(node.getId());

		if (candidate != null && node.getClass().isAssignableFrom(candidate.getClass()))
			return (N) candidate;
		else
			return null;
	}


	/**
	 * Puts the specified AbstractNode into this network. Returns true if the operation
	 * succeeded. If this network already contains the specified AbstractNode, the operation fails
	 * and returns false.
	 * Note that this is not the "add node" mutation.
	 */
	public boolean putNode(Node node) {

		if (node instanceof InputNode && !inputs.contains(node))	//don't add duplicates
			return inputs.add((InputNode) node);

		else if (node instanceof OutputNode && !outputs.contains(node))
			return outputs.add((OutputNode) node);

		else if (node instanceof HiddenNode && !hiddens.containsKey(node.getId())) {
			hiddens.put(node.getId(), (HiddenNode) node);
			return true;
		}

		return false;
	}


	/**
	 * Puts the specified Connection into this network. If any of the end Nodes is not in
	 * this Network, it is put in this Network. If any of the end Nodes has an equivalent
	 * entity in the Network, that instance is used as the end Nodes and the original
	 * connection instance is not modified.
	 * Note that this is not the "add connection" mutation.
	 */
	public void putConnection(Connection connection) {
//		Node[] endNodes =
//				new Node[] { connection.getPrevNode(), connection.getNextNode() };

		Node prevNode = connection.getPrevNode();
		Node nextNode = connection.getNextNode();

//		final List<Node> endNodes = List.of(prevNode, nextNode);
		// cannot do this because mutability of resulting list is unknown

//		final List<Node> endNodes = new ArrayList<>();
//		endNodes.add(prevNode); endNodes.add(nextNode);

		boolean nodeChanged = false;
		if (!putNode(prevNode)) {
			prevNode = findNode(prevNode);
			nodeChanged = true;
		}
		if (!putNode(nextNode)) {
			nextNode = findNode(nextNode);
			nodeChanged = true;
		}


		if (nodeChanged) {	//need to modify connection
			connection =
					new Connection(
							connection.getInnovationNumber(),
							connection.getWeight(),
							prevNode, nextNode
					);
		}


		final List<Connection> prevNodeConnections =
				connection.getPrevNode().getOutputs();
		if (!prevNodeConnections.contains(connection))
			connection.getPrevNode().addOutput(connection);

		final List<Connection> nextNodeConnections =
				connection.getNextNode().getInputs();
		if (!nextNodeConnections.contains(connection))
			connection.getNextNode().addInput(connection);

		connections.put(connection.getInnovationNumber(), connection);
	}


	//////////////////////////////
	//NE related

	/**
	 * Connects an existing AbstractNode to another existing AbstractNode, making a new Connection.
	 * This is the "add connection" mutation.
	 */
	public void connect(Node from, Node to, double weight) {
		from = findNode(from);
		to = findNode(to);

		if (from == null || to == null)
			throw new IllegalArgumentException("End node is not in this network");

		Connection c =
				new Connection(Connection.getNextGlobalInnovationNum(), weight, from, to);

		putConnection(c);
	}

	/**
	 * Splits an existing connection into 2 new connections, inserting a new AbstractNode in
	 * between. This is the "add node" mutation.
	 * The incoming connection of the new AbstractNode will have the same weight as the old
	 * connection.
	 * @param connection	The Connection to place the new AbstractNode on
	 */
	public void addNode(Connection connection) {
		if (!connections.containsKey(connection.getInnovationNumber()))
			throw new IllegalArgumentException("Connection is not in this network");

//		connection.setEnabled(false);

		final HiddenNode newNode =
				(HiddenNode) new NodeBuilder(NodeType.HIDDEN).build();

		// keeps identical weight and bias
		final Connection connection1 =
				new Connection(
						Connection.getNextGlobalInnovationNum(),
						connection.getWeight(),
						connection.getPrevNode(),
						newNode
				);
		// weight of 1
		final Connection connection2 =
				new Connection(
						Connection.getNextGlobalInnovationNum(),
						1,
						newNode,
						connection.getNextNode()
				);

		newNode.addInput(connection1);
		newNode.addOutput(connection2);

		getHiddens().put(newNode.getId(), newNode);
		getConnections().put(connection1.getInnovationNumber(), connection1);
		getConnections().put(connection2.getInnovationNumber(), connection2);
	}


	//////////////////////////////
	//basic getters - nothing interesting past this point

	public Map<Long, HiddenNode> getHiddens() { return hiddens; }
	public List<InputNode> getInputs() { return inputs; }
	public List<OutputNode> getOutputs() { return outputs; }
	public Map<Long, Connection> getConnections() { return connections; }
}
