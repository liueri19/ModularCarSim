package network;

import util.DeepCopyable;
import util.IdentityHashSet;

import java.util.IdentityHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a connection between 2 neurons.
 * A connection has a weight and an innovation number.
 */
public class Connection implements DeepCopyable<Connection> {
	private Node prevNode;
	private Node nextNode;
	private double weight;
	private long innovNum;

	public Connection(long innovationNumber,
					  double weight,
					  Node prevNode,
					  Node nextNode) {
		innovNum = innovationNumber;
		this.weight = weight;
		this.prevNode = prevNode;
		this.nextNode = nextNode;
	}

	/**
	 * Constructs a copy of the specified object, handling circular references.
	 */
	public Connection(
			Connection original,
			IdentityHashMap<Object, Object> clones,
			IdentityHashSet<Object> cloning) {
		innovNum = original.innovNum;
		weight = original.weight;


	}

	@Override
	public Connection copy(IdentityHashMap<Object, Object> clones, IdentityHashSet<Object> cloning) {
		cloning.add(this);

		if (clones.containsKey(this))
			return (Connection) clones.get(this);
		return new Connection(this, clones, cloning);
	}

	@Override
	public void fixNulls(Connection connection, IdentityHashMap<Object, Object> clones) {
		// TODO fix
	}

	/**
	 * This method should be called by an activated ending node (nextNode). Causes this
	 * Connection to read from the previous Node and write the result to the next Node.
	 */
	double fetch() {
		double value = getPrevNode().read();
		value *= getWeight();
		return value;
	}


	@Override
	public String toString() {
		return getInnovationNumber() + ":\t" +
				getPrevNode().toString() + "->" +
				getWeight()  + "->" +
				getNextNode().toString();
	}

	/**
	 * The reverse of toString(). Take a String representation of Connection and returns
	 * a new Connection instance with corresponding data.
	 * @throws IllegalArgumentException if the connection entry is malformatted, or if the
	 * start node is not a ReadableNode or if the end node is not a WritableNode
	 */
	public static Connection parseConnection(String s) throws IllegalArgumentException {
		/* innovNum:	N_id0->weight->N_id1 */
		String[] components = s.split("->");
		/* innovNum:	N_id0, weight, N_id1 */

		if (components.length != 3)
			throw new IllegalArgumentException("Incomplete connection entry: " + s);

		String[] innovNumAndStrID = components[0].split(":\t");
		/* [innovNum, N_id0], weight, N_id1 */
		final String weightStr = components[1];
		/* [innovNum, N_id0], weight, N_id1 */

		if (innovNumAndStrID.length != 2)
			throw new IllegalArgumentException("Incomplete connection entry: " + s);

		long innovNum = Long.parseLong(innovNumAndStrID[0]);
		String n0StrId = innovNumAndStrID[1];

		double weight = Double.parseDouble(weightStr);

		String n1StrId = components[2];

		final Node prevNode = Node.parseNode(n0StrId);
		final Node nextNode = Node.parseNode(n1StrId);

		return new Connection(innovNum, weight, prevNode, nextNode);
	}


	/**
	 * Two Connections are logically equal if they both connect the same Nodes, even if
	 * they have different weights, bias, or innovation numbers.
	 */
	@Override
	public boolean equals(Object c) {
		return c instanceof Connection &&
				getPrevNode().equals(((Connection) c).getPrevNode()) &&
				getNextNode().equals(((Connection) c).getNextNode());
	}


	private static AtomicLong globalInnovationNumber = new AtomicLong();

	// TODO move to carsim.neat?
	static synchronized long getNextGlobalInnovationNum() {
		return globalInnovationNumber.getAndIncrement();
	}

	//////////////////////////////
	//basic getters and setters
	public long getInnovationNumber() { return innovNum; }

	public double getWeight() { return weight; }
	public void setWeight(double weight) { this.weight = weight; }

	public Node getNextNode() { return nextNode; }

	public Node getPrevNode() { return prevNode; }
}
