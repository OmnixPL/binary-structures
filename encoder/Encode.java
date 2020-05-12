package encoder;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import lexer.Lexer;
import lexer.LexerException;
import lexer.Tokens;
import parser.Parser;
import lexer.Token;
import reader.CodeReader;
import reader.Pos;

public class Encode {
	public static void main(String[] args) {
		//noParser(args);
		parsered(args);
	}
	
	@SuppressWarnings("unused")
	private static void parsered(String[] args) {
		CodeReader cr;
		Lexer lexer;
		Parser parser;
		
		try (BufferedReader bf = new BufferedReader(new FileReader(args[0]))) {
			cr = new CodeReader(bf);
			lexer = new Lexer(cr);
			parser = new Parser(lexer);
			
			parser.buildParseTree();			
		}
		catch (FileNotFoundException e) {
		    e.printStackTrace();
		}
		catch (IOException e) {
		    e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unused")
	private static void noParser(String[] args) {
		CodeReader cr;
		Lexer lexer;
		Token token;
		LinkedList<Token> output = new LinkedList<Token>();
		LinkedList<Token> expected = new LinkedList<Token>();
		boolean success = true;
		
		// parser placeholder
		try (BufferedReader bf = new BufferedReader(new FileReader(args[0]))) {
			cr = new CodeReader(bf);
			lexer = new Lexer(cr);
			
			for (@SuppressWarnings("unused")	int i = 0 ; ; i++) {
				//System.out.println(i);
				try {
					token = lexer.getNextToken();
					output.add(token);
					if (token.getTokenName() == Tokens.EOF) break;
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
		expected.add(new Token(Tokens.MESSAGE, new Pos(1, 1), null));
		expected.add(new Token(Tokens.TYPES, new Pos(2, 1), null));
		expected.add(new Token(Tokens.VALUES, new Pos(3, 1), null));
		expected.add(new Token(Tokens.IDENTIFIER, new Pos(4, 1), "ident"));
		expected.add(new Token(Tokens.IDENTIFIER, new Pos(4, 7), "Identyfikator"));
		expected.add(new Token(Tokens.INT, new Pos(6, 1), "0"));
		expected.add(new Token(Tokens.INT, new Pos(6, 3), "3"));
		expected.add(new Token(Tokens.INT, new Pos(6, 5), "4"));
		expected.add(new Token(Tokens.DOUBLE, new Pos(6, 7), "0.4"));
		expected.add(new Token(Tokens.ADDITIVE_OP, new Pos(6, 11), "-"));
		expected.add(new Token(Tokens.INT, new Pos(6, 12), "3"));
		expected.add(new Token(Tokens.DOUBLE, new Pos(6, 14), "0.02"));
		expected.add(new Token(Tokens.OPEN_CURLY, new Pos(8, 1), "{"));
		expected.add(new Token(Tokens.CLOSE_CURLY, new Pos(8, 2), "}"));
		expected.add(new Token(Tokens.OPEN_SQUARE, new Pos(8, 3), "["));
		expected.add(new Token(Tokens.CLOSE_SQUARE, new Pos(8, 4), "]"));
		expected.add(new Token(Tokens.OPEN_ROUND, new Pos(8, 5), "("));
		expected.add(new Token(Tokens.CLOSE_ROUND, new Pos(8, 6), ")"));
		expected.add(new Token(Tokens.OPEN_ANGLE, new Pos(8, 7), "<"));
		expected.add(new Token(Tokens.CLOSE_ANGLE, new Pos(8, 8), ">"));
		expected.add(new Token(Tokens.COLON, new Pos(8, 9), ":"));
		expected.add(new Token(Tokens.SEMICOLON, new Pos(8, 10), ";"));
		expected.add(new Token(Tokens.COMMA, new Pos(8, 11), ","));
		expected.add(new Token(Tokens.DOT, new Pos(8, 12), "."));
		expected.add(new Token(Tokens.ADDITIVE_OP, new Pos(8, 13), "+"));
		expected.add(new Token(Tokens.ADDITIVE_OP, new Pos(8, 14), "-"));
		expected.add(new Token(Tokens.MULTIPLICATIVE_OP, new Pos(8, 15), "*"));
		expected.add(new Token(Tokens.MULTIPLICATIVE_OP, new Pos(8, 16), "/"));
		expected.add(new Token(Tokens.EQUAL, new Pos(8, 17), "="));
		expected.add(new Token(Tokens.STRING, new Pos(10, 1), "STRING TEST"));
		expected.add(new Token(Tokens.STRING, new Pos(11, 1), ""));
		expected.add(new Token(Tokens.STRING, new Pos(12, 1), "slowo"));
		expected.add(new Token(Tokens.STRING, new Pos(13, 1), " spacje "));
		expected.add(new Token(Tokens.STRING, new Pos(14, 1), "znaki specjalne: {}[]()<>:;,.+-*/="));
		expected.add(new Token(Tokens.STRING, new Pos(15, 1), "MESSAGE"));
		expected.add(new Token(Tokens.STRING, new Pos(16, 1), "Dwie\nlinijki"));
		expected.add(new Token(Tokens.STRING, new Pos(18, 1), "Trzy\ndlugie\nlinijki"));
		expected.add(new Token(Tokens.STRING, new Pos(21, 1), "Escape\\n\\t\\r"));
		expected.add(new Token(Tokens.IDENTIFIER, new Pos(23, 1), "Errors"));
		expected.add(new Token(Tokens.EOF, new Pos(26, 1), null));
	}
}
