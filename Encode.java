import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class Encode {
	public static void main(String[] args) {
		CodeReader cr;
		Lexer lexer;
		Token token;
		LinkedList<Token> output = new LinkedList<Token>();
		LinkedList<Token> expected = new LinkedList<Token>();
		boolean success = true;
		
		// parser placeholder
		try (BufferedReader bf = new BufferedReader(new FileReader(args[0]))) {
			cr = new FileCodeReader(bf);
			lexer = new Lexer(cr);
			
			for(@SuppressWarnings("unused")	int i = 0 ; ; i++) {
				//System.out.println(i);
				try {
					token = lexer.getNextToken();
					output.add(token);
					if (token.getTokenName() == TName.EOF) break;
				} catch (LexerException e) {
					System.out.println(e);
				}
			}
			
			
		}
		catch (FileNotFoundException e) {
		    e.printStackTrace();
		}
		catch (IOException e) {
		    e.printStackTrace();
		}
		
		// testing
		if(args[0].equals("Tokens")) {
			populateExpected(expected);
			while (!output.isEmpty()) {
				if (!output.remove().equals(expected.remove())) {
					System.out.println("Output token doesn't match expected");
					success = false;
				}
			}
			if(success)
				System.out.println("All tokens are correct");
		}
		System.out.println("Finished");
	}
	
	private static void populateExpected(LinkedList<Token> expected) {
		expected.add(new Token(TName.MESSAGE, new Pos(1, 1), null));
		expected.add(new Token(TName.TYPES, new Pos(2, 1), null));
		expected.add(new Token(TName.VALUES, new Pos(3, 1), null));
		expected.add(new Token(TName.STRUCT, new Pos(4, 1), null));
		expected.add(new Token(TName.ENUM, new Pos(4, 8), null));
		expected.add(new Token(TName.CHOICE, new Pos(4, 13), null));
		expected.add(new Token(TName.IDENTIFIER, new Pos(4, 20), "ident"));
		expected.add(new Token(TName.IDENTIFIER, new Pos(4, 26), "Identyfikator"));
		expected.add(new Token(TName.INT, new Pos(6, 1), "0"));
		expected.add(new Token(TName.INT, new Pos(6, 3), "3"));
		expected.add(new Token(TName.INT, new Pos(6, 5), "4"));
		expected.add(new Token(TName.DOUBLE, new Pos(6, 7), "0.4"));
		expected.add(new Token(TName.ADDITIVE_OP, new Pos(6, 11), "-"));
		expected.add(new Token(TName.INT, new Pos(6, 12), "3"));
		expected.add(new Token(TName.DOUBLE, new Pos(6, 14), "0.02"));
		expected.add(new Token(TName.OPEN_CURLY, new Pos(8, 1), "{"));
		expected.add(new Token(TName.CLOSE_CURLY, new Pos(8, 2), "}"));
		expected.add(new Token(TName.OPEN_SQUARE, new Pos(8, 3), "["));
		expected.add(new Token(TName.CLOSE_SQUARE, new Pos(8, 4), "]"));
		expected.add(new Token(TName.OPEN_ROUND, new Pos(8, 5), "("));
		expected.add(new Token(TName.CLOSE_ROUND, new Pos(8, 6), ")"));
		expected.add(new Token(TName.OPEN_ANGLE, new Pos(8, 7), "<"));
		expected.add(new Token(TName.CLOSE_ANGLE, new Pos(8, 8), ">"));
		expected.add(new Token(TName.COLON, new Pos(8, 9), ":"));
		expected.add(new Token(TName.SEMICOLON, new Pos(8, 10), ";"));
		expected.add(new Token(TName.COMMA, new Pos(8, 11), ","));
		expected.add(new Token(TName.DOT, new Pos(8, 12), "."));
		expected.add(new Token(TName.ADDITIVE_OP, new Pos(8, 13), "+"));
		expected.add(new Token(TName.ADDITIVE_OP, new Pos(8, 14), "-"));
		expected.add(new Token(TName.MULTIPLICATIVE_OP, new Pos(8, 15), "*"));
		expected.add(new Token(TName.MULTIPLICATIVE_OP, new Pos(8, 16), "/"));
		expected.add(new Token(TName.EQUAL, new Pos(8, 17), "="));
		expected.add(new Token(TName.STRING, new Pos(10, 1), "STRING TEST"));
		expected.add(new Token(TName.STRING, new Pos(11, 1), ""));
		expected.add(new Token(TName.STRING, new Pos(12, 1), "slowo"));
		expected.add(new Token(TName.STRING, new Pos(13, 1), " spacje "));
		expected.add(new Token(TName.STRING, new Pos(14, 1), "znaki specjalne: {}[]()<>:;,.+-*/="));
		expected.add(new Token(TName.STRING, new Pos(15, 1), "MESSAGE"));
		expected.add(new Token(TName.STRING, new Pos(16, 1), "Dwie\nlinijki"));
		expected.add(new Token(TName.STRING, new Pos(18, 1), "Trzy\ndlugie\nlinijki"));
		expected.add(new Token(TName.STRING, new Pos(21, 1), "Escape\\n\\t\\r"));
		expected.add(new Token(TName.IDENTIFIER, new Pos(23, 1), "Errors"));
		expected.add(new Token(TName.EOF, new Pos(26, 1), null));
	}
}
