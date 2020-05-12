package parser;

public class ParserException extends Exception {
	// Because of warning: The serializable class ParserException does not 
	// declare a static final serialVersionUID field of type long
	private static final long serialVersionUID = -5552319238213812670L;

	public ParserException(String message) {
		super(message);
	}
}
