package lexer;
public enum Tokens {
	MESSAGE, TYPES, VALUES, 						// headers
	IDENTIFIER, INT, DOUBLE, STRING,		// names + values
	OPEN_CURLY, CLOSE_CURLY, OPEN_ANGLE, CLOSE_ANGLE, OPEN_SQUARE, CLOSE_SQUARE, OPEN_ROUND, CLOSE_ROUND, // brackets
	ADDITIVE_OP, MULTIPLICATIVE_OP,					// for simple arithmetic in array size
	DOT, COMMA, SEMICOLON, COLON, EQUAL,
	EOF
}