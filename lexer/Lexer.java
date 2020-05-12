package lexer;
import java.io.IOException;
import java.util.HashMap;

import reader.CodeReader;
import reader.Pos;

public class Lexer {
	private HashMap<Character, Tokens> singleChar = new HashMap<Character, Tokens>();
	private HashMap<String, Tokens> keywords = new HashMap<String, Tokens>();
	private CodeReader cr;
	private char current;
	private Pos pos;
	
	public Lexer(CodeReader cr) {
		populateHashMap();
		this.cr = cr;
	}

	public Token getNextToken() throws IOException, LexerException {
		Token token;
		Tokens tName;
		
		current = cr.peek();
		
		skipWhitespace();

		pos = new Pos(cr.getPos());
		
		if ( (tName = singleChar.get(current)) != null) {	// single char tokens
			token = new Token(tName, pos, Character.toString(current));
			cr.consume();
		}
		else if (current == '"') { 							// strings between " and "
			token = parseString();
		}
		else if (isazAZ(current)) {							// identifiers
			token = parseIdentifier();
		}
		else if (is09(current)) {							// number
			token = parseNumber();
		}
		else if (current == 0xFFFF) {
			token = new Token(Tokens.EOF, pos, null);
		}
		else {
			cr.consume();
			throw new LexerException("Tokenization failed: " + current);
		}
		// TODO this:
		//System.out.println(token.getTokenName() + " " + token.getPos().getLine() + " " + token.getPos().getColumn() + " >" + token.getMsg() + "< ");
		
		return token;
	}
	
	private Token parseString() throws IOException {
		StringBuilder string;
		
		cr.consume();
		string = new StringBuilder();
		while ( (current = cr.peek()) != '"') {
			string.append(current);
			cr.consume();
		}
		cr.consume();	// consume " because it signals end of string (usually you don't want to consume unused char) 
		return new Token(Tokens.STRING, pos, string.toString());
	}
	
	private Token parseIdentifier() throws IOException {
		StringBuilder string;
		Tokens tName;
		
		string = new StringBuilder();
		string.append(current);
		cr.consume();
		
		while (isazAZ(current = cr.peek())) {
			string.append(current);
			cr.consume();
		}
		
		if ( (tName = keywords.get(string.toString().toUpperCase())) != null) {
			return new Token(tName, pos, null);	
		}
		else return new Token(Tokens.IDENTIFIER, pos, string.toString());			
	}
	
	private Token parseNumber() throws IOException, LexerException {
		StringBuilder string;
		Token token;
		
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
			token = new Token(Tokens.INT, pos, i.toString());
		} catch (NumberFormatException e) {
			try {
				Double d;
				d = Double.parseDouble(string.toString());
				token = new Token(Tokens.DOUBLE, pos, d.toString());
			} catch (NumberFormatException e2) {
				throw new LexerException("Parsing number failed: " + string.toString());
			}
		}
		return token;
	}
	
	private void skipWhitespace() throws IOException {
		while (Character.isWhitespace(current)) {
			cr.consume();
			current = cr.peek();
		}
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
		singleChar.put('{', Tokens.OPEN_CURLY);
		singleChar.put('}', Tokens.CLOSE_CURLY);
		singleChar.put('<', Tokens.OPEN_ANGLE);
		singleChar.put('>', Tokens.CLOSE_ANGLE);
		singleChar.put('[', Tokens.OPEN_SQUARE);
		singleChar.put(']', Tokens.CLOSE_SQUARE);
		singleChar.put('(', Tokens.OPEN_ROUND);
		singleChar.put(')', Tokens.CLOSE_ROUND);
		singleChar.put('+', Tokens.ADDITIVE_OP);
		singleChar.put('-', Tokens.ADDITIVE_OP);
		singleChar.put('*', Tokens.MULTIPLICATIVE_OP);
		singleChar.put('/', Tokens.MULTIPLICATIVE_OP);
		singleChar.put('.', Tokens.DOT);
		singleChar.put(',', Tokens.COMMA);
		singleChar.put(';', Tokens.SEMICOLON);
		singleChar.put(':', Tokens.COLON);
		singleChar.put('=', Tokens.EQUAL);
		
		keywords.put("MESSAGE", Tokens.MESSAGE);
		keywords.put("TYPES", Tokens.TYPES);
		keywords.put("VALUES", Tokens.VALUES);
	}
}
