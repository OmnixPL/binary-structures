import java.io.IOException;
import java.util.HashMap;

public class Lexer {
	private HashMap<Character, TName> singleChar = new HashMap<Character, TName>();
	private HashMap<String, TName> keywords = new HashMap<String, TName>();
	private CodeReader cr;
	
	public Lexer(CodeReader cr) {
		populateHashMap();
		this.cr = cr;
	}

	public Token getNextToken() throws IOException, LexerException {
		Token token;
		char current;
		TName tName;
		Pos pos;
		StringBuilder string;
		
		current = cr.peek();
		
		while (Character.isWhitespace(current)) {
			cr.consume();
			current = cr.peek();
		}

		pos = new Pos(cr.getPos());
		
		if ( (tName = singleChar.get(current)) != null) {	// single char tokens
			token = new Token(tName, pos, Character.toString(current));
			cr.consume();
		}
		else if (current == '"') { 							// strings between " and "
			cr.consume();
			string = new StringBuilder();
			while ( (current = cr.peek()) != '"') {
				string.append(current);
				cr.consume();
			}
			cr.consume();	// consume " because it signals end of string (usually you don't want to consume unused char) 
			token = new Token(TName.STRING, pos, string.toString());
		}
		else if (isazAZ(current)) {							// identifiers
			string = new StringBuilder();
			string.append(current);
			cr.consume();
			
			while (isazAZ(current = cr.peek())) {
				string.append(current);
				cr.consume();
			}
			
			if ( (tName = keywords.get(string.toString().toUpperCase())) != null) {
				token = new Token(tName, pos, null);	
			}
			else token = new Token(TName.IDENTIFIER, pos, string.toString());			
		}
		else if (is09(current)) {							// number
			string = new StringBuilder();
			string.append(current);
			cr.consume();
			stackDigits(string, current);
			if ((current = cr.peek()) == '.') {
				string.append(current);
				cr.consume();
				stackDigits(string, current);
			}
			
			try {
				Integer i;
				i = Integer.parseInt(string.toString());
				token = new Token(TName.INT, pos, i.toString());
			} catch (NumberFormatException e) {
				try {
					Double d;
					d = Double.parseDouble(string.toString());
					token = new Token(TName.DOUBLE, pos, d.toString());
				} catch (NumberFormatException e2) {
					throw new LexerException("Parsing number failed: " + string.toString());
				}
			}
			
		}
		else if (current == 0xFFFF) {
			token = new Token(TName.EOF, pos, null);
		}
		else {
			cr.consume();
			throw new LexerException("Tokenization failed: " + current);
		}
		System.out.println(token.getTokenName() + " " + token.getPos().getLine() + " " + token.getPos().getColumn() + " >" + token.getMsg() + "< ");
		
		return token;
	}

	private void stackDigits(StringBuilder string, char current) throws IOException {
		while (is09(current = cr.peek())) {
			string.append(current);
			cr.consume();
		}
	}
	
	private boolean is09(char c) {
		return ('0' <= c && c <= '9');
	}
	
	private boolean isazAZ(char c) {
		return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
	}
	
	private void populateHashMap() {
		singleChar.put('{', TName.OPEN_CURLY);
		singleChar.put('}', TName.CLOSE_CURLY);
		singleChar.put('<', TName.OPEN_ANGLE);
		singleChar.put('>', TName.CLOSE_ANGLE);
		singleChar.put('[', TName.OPEN_SQUARE);
		singleChar.put(']', TName.CLOSE_SQUARE);
		singleChar.put('(', TName.OPEN_ROUND);
		singleChar.put(')', TName.CLOSE_ROUND);
		singleChar.put('+', TName.ADDITIVE_OP);
		singleChar.put('-', TName.ADDITIVE_OP);
		singleChar.put('*', TName.MULTIPLICATIVE_OP);
		singleChar.put('/', TName.MULTIPLICATIVE_OP);
		singleChar.put('.', TName.DOT);
		singleChar.put(',', TName.COMMA);
		singleChar.put(';', TName.SEMICOLON);
		singleChar.put(':', TName.COLON);
		singleChar.put('=', TName.EQUAL);
		
		keywords.put("MESSAGE", TName.MESSAGE);
		keywords.put("TYPES", TName.TYPES);
		keywords.put("VALUES", TName.VALUES);
		keywords.put("STRUCT", TName.STRUCT);
		keywords.put("ENUM", TName.ENUM);
		keywords.put("CHOICE", TName.CHOICE);
	}
}
