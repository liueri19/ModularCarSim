package network;

public enum NodeType {
	INPUT("Input"),
	OUTPUT("Output"),
	HIDDEN("Hidden"),
	BIAS("Bias");

	private final String code;

	NodeType(String code) {
		this.code = code;
	}

	@Override
	public String toString() { return code; }

	/**
	 * Gets the NodeType with the matching code.
	 * @return	the NodeType with the matching code
	 * @throws IllegalArgumentException if the code does not have a matching NodeType
	 */
	public static NodeType of(String code) throws IllegalArgumentException {
		switch (code) {
			case "Input":	return INPUT;
			case "Output":	return OUTPUT;
			case "Hidden":	return HIDDEN;
			case "Bias":	return BIAS;
			default:
				throw new IllegalArgumentException("Node type '" + code + "' is not found");
		}
	}
}
