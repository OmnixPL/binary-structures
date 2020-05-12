package lexer;
public class LexerException extends Exception {
	// Because of warning: The serializable class LexerException does not 
	// declare a static final serialVersionUID field of type long
	private static final long serialVersionUID = 4541555037192622030L; 
	
	
	public LexerException(String message) {
		super(message);
	}
}
