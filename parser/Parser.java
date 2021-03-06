package parser;
import java.io.IOException;
import java.util.ArrayList;

import lexer.Lexer;
import lexer.LexerException;
import lexer.Token;
import lexer.Tokens;
import parser.arithmetics.Expression;
import parser.arithmetics.Factor;
import parser.arithmetics.Term;
import parser.arithmetics.Variable;
import parser.types.NewChoice;
import parser.types.NewDouble;
import parser.types.NewEnum;
import parser.types.NewInteger;
import parser.types.NewString;
import parser.types.NewStruct;
import parser.types.Type;
import parser.types.Types;
import parser.values.ArrayVal;
import parser.values.ChoiceVal;
import parser.values.DoubleVal;
import parser.values.EnumVal;
import parser.values.IntegerVal;
import parser.values.StringVal;
import parser.values.StructVal;
import parser.values.Value;

public class Parser {
	private Lexer lexer;
	private MsgTree tree;
	private Token currenToken;
	
	public Parser(Lexer lexer) {
		this.lexer = lexer;
		tree = new MsgTree();
	}
	
	public int buildParseTree() {
		try {
			consumeToken(); // actually initialize
			
			// parse "MESSAGE" header
			if (currenToken.getTokenName() != Tokens.MESSAGE) 
				return -1;
			consumeToken();
			parseTypes();
			parseValues();
		} catch (ParserException e) {
			System.out.println(e);
		}
		tree.printTree();
		return 0;
	}
	
// ----------------------- PARSE TYPES 
	private int parseTypes() throws ParserException {
		Type type;
		if (currenToken.getTokenName() != Tokens.TYPES)
			return -1;
		consumeToken();
		
		while (currenToken.getTokenName() == Tokens.IDENTIFIER) {
			if ( (type = parseType()) != null)
				tree.types.add(type);
			else break;
		}
		
		return 0;
	}
	
	private Type parseType() throws ParserException {
		Type type = null;
		if (currenToken.getMsg().equalsIgnoreCase("Integer"))
			type = parseInteger();
		else if (currenToken.getMsg().equalsIgnoreCase("Double"))
			type = parseDouble();
		else if (currenToken.getMsg().equalsIgnoreCase("Enum"))
			type = parseEnum();
		else if (currenToken.getMsg().equalsIgnoreCase("String"))
			type = parseString();
		else if (currenToken.getMsg().equalsIgnoreCase("Struct"))
			type = parseStruct();
		else if (currenToken.getMsg().equalsIgnoreCase("Choice"))
			type = parseChoice();
		else 
			throwMessageExpected("parse type", "built-in type (Integer, Double, etc.)");
		
		for (Type type2 : tree.types) {
			if (type2.getIdentifier().equalsIgnoreCase(type.getIdentifier()))
				throwMessage("parse type", "Type with the same name already exists");
		}
		
		return type;
	}
	
	private NewInteger parseInteger() throws ParserException {
		consumeToken();

		if (currenToken.getTokenName() != Tokens.OPEN_ANGLE)
			throwMessageExpected("integer type", "<");
		consumeToken();
		
		// parse bits number
		if (currenToken.getTokenName() != Tokens.INT)
			throwMessageExpected("integer type", "integer");
		Integer bits = Integer.parseInt(currenToken.getMsg());
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.CLOSE_ANGLE)
			throwMessageExpected("integer type", ">");
		consumeToken();
		
		String identifier = parseTypeName();
		
		return new NewInteger(identifier, bits);
	}
	
	private NewDouble parseDouble() throws ParserException {
		consumeToken();
		String identifier = parseTypeName();
		
		return new NewDouble(identifier);
	}
	
	private NewEnum parseEnum() throws ParserException {
		ArrayList<String> values = new ArrayList<String>();
		
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.OPEN_CURLY)
			throwMessageExpected("enum type", "{");
		consumeToken();
		
		// parse enumerators
		while (true) {
			if (currenToken.getTokenName() != Tokens.STRING)
				throwMessageExpected("enum type", "enumerator");
			values.add(currenToken.getMsg());
			consumeToken();
			
			if (currenToken.getTokenName() != Tokens.COMMA)
				break;
			consumeToken();
		}
		
		if (currenToken.getTokenName() != Tokens.CLOSE_CURLY)
			throwMessageExpected("enum type", "}");
		consumeToken();
		
		String identifier = parseTypeName();
		
		return new NewEnum(identifier, values);
	}
	
	private NewString parseString() throws ParserException {
		consumeToken();
		
		String identifier = parseTypeName();
		
		return new NewString(identifier);
	}
	
	private NewStruct parseStruct() throws ParserException {
		Type type;
		ArrayList<Type> members = new ArrayList<Type>();
		
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.OPEN_CURLY)
			throwMessageExpected("struct type", "{");
		consumeToken();
		
		while (currenToken.getTokenName() != Tokens.CLOSE_CURLY ) {
			type = parseType();
			if (type != null)
				members.add(type);
			else 
				break;
		}
		
		if (currenToken.getTokenName() != Tokens.CLOSE_CURLY)
			throwMessageExpected("struct type", "}");
		consumeToken();

		String identifier = parseTypeName();
		
		return new NewStruct(identifier, members);
	}

	private NewChoice parseChoice() throws ParserException {
		NewEnum newEnum;
		ArrayList<String> values = new ArrayList<String>();
		
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.OPEN_ANGLE)
			throwMessageExpected("choice type", "<");
		consumeToken();
		
		String enumName;
		if (currenToken.getTokenName() != Tokens.IDENTIFIER)
			throwMessageExpected("choice type", "enum name");
		enumName = currenToken.getMsg();
		consumeToken();
		
		if ( (newEnum = findEnumByName(enumName)) == null)
			throwMessage("parse choice", "Name does not identify an enum type");
		
		if (currenToken.getTokenName() != Tokens.CLOSE_ANGLE)
			throwMessageExpected("choice type", ">");
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.OPEN_CURLY)
			throwMessageExpected("choice type", "{");
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.IDENTIFIER)
			throwMessageExpected("choice type", "type name");
		do {
			if ( findTypeByName(currenToken.getMsg()) == null)
				throwMessage("parse choice", "Name does not identify a type");
			
			values.add(currenToken.getMsg());
			consumeToken();
			
			if (currenToken.getTokenName() != Tokens.SEMICOLON)
				throwMessageExpected("choice type", ";");
			consumeToken();
			
		} while (currenToken.getTokenName() == Tokens.IDENTIFIER);
		
		if (currenToken.getTokenName() != Tokens.CLOSE_CURLY)
			throwMessageExpected("choice type", "}");
		consumeToken();
		
		String identifier = parseTypeName();
		
		if (values.size() != newEnum.getValues().size())
			throwMessageExpected("choice type", "the same number of types as enumerators in enum");
		
		return new NewChoice(identifier, newEnum, values);
	}
	
	private Type findTypeByName(String typeName) {
		for (Type type : tree.types) {
			if (type.getIdentifier().equals(typeName))
				return type;
		}
		return null;
	}
	
	private NewEnum findEnumByName(String typeName) {
		for (Type type : tree.types) {
			if (type.getType() == Types.ENUM) {
				if (type.getIdentifier().equals(typeName))
					return (NewEnum) type;
			}
		}
		return null;
	}
	
	// parse "name;" part of any type declaration
	private String parseTypeName() throws ParserException {
		if (currenToken.getTokenName() != Tokens.IDENTIFIER)
			throwMessageExpected("type name", "identifier");
		String identifier = currenToken.getMsg();
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.SEMICOLON)
			throwMessageExpected("type name", ";");
		consumeToken();
		
		return identifier;
	}

	
// ----------------------- PARSE VALUES
	private int parseValues() throws ParserException {
		Value value;
		
		if (currenToken.getTokenName() != Tokens.VALUES)
			return -1;
		consumeToken();
		
		while (currenToken.getTokenName() == Tokens.IDENTIFIER) {
			if ( (value = parseValue()) != null)
				tree.values.add(value);
			else break;
		}
		return 0;
	}
	
	private Value parseValue() throws ParserException {
		Type type = null;
		Value value = null;
		String typeName;
		String identifier;

		if (currenToken.getTokenName() != Tokens.IDENTIFIER)
			throwMessageExpected("parse value", "type name");
		typeName = currenToken.getMsg();
		consumeToken();
		
		for (Type type2 : tree.types) {
			if (typeName.equalsIgnoreCase(type2.getIdentifier())) {
				type = type2;
				break;
			}
		}
		
		if (type == null)
			throwMessageExpected("parse value", "type name");
		
		if (currenToken.getTokenName() != Tokens.IDENTIFIER)
			throwMessageExpected("parse value", "value name");
		
		identifier = currenToken.getMsg();
		consumeToken();
		
		if ( (value = parseArrayVal(identifier, type)) == null)	// this null is actually usefull - check parseArrayVal
			value = parseSingle(identifier, type);
		
		
		return value;
	}
	
	private ArrayVal parseArrayVal(String identifier, Type type) throws ParserException {
		ArrayList<Value> values = new ArrayList<Value>();
		Value value;
		Expression elementsNo;
		
		if (currenToken.getTokenName() != Tokens.OPEN_SQUARE)
			return null;	// NO ARRAY - NOT AN ERROR
		consumeToken();
		
		elementsNo = parseArithmetic();
		
		if (currenToken.getTokenName() != Tokens.CLOSE_SQUARE)
			throwMessageExpected("parse array", "]");
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.EQUAL)
			throwMessageExpected("parse array", "=");
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.OPEN_CURLY)
			throwMessageExpected("parse array", "{");
		consumeToken();
		
		while (true) {
			value = parseRightHand(identifier, type); 
			if (value == null)
				throwMessage("parse array", "Unknown error while parsing right hand. Should never happen.");
			values.add(value);
			
			if (currenToken.getTokenName() != Tokens.COMMA)
				break;
			consumeToken();
		}
		
		if (currenToken.getTokenName() != Tokens.CLOSE_CURLY)
			throwMessageExpected("parse array", "}");
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.SEMICOLON)
			throwMessageExpected("parse array", ";");
		consumeToken();
		
		return new ArrayVal(type, identifier, values, elementsNo);
	}
	
	// idk if this should return Expression or evaluate it already?
	private Expression parseArithmetic() throws ParserException {
		return parseExpression();
	}
	
	private Expression parseExpression() throws ParserException {
		ArrayList<Character> operators = new ArrayList<Character>();
		ArrayList<Term> terms = new ArrayList<Term>();
		terms.add(parseTerm());
		
		while (currenToken.getTokenName() == Tokens.ADDITIVE_OP) {
			operators.add(currenToken.getMsg().charAt(0));
			consumeToken();
			
			terms.add(parseTerm());
		}
		
		return new Expression(terms, operators);
	}

	private Term parseTerm() throws ParserException {
		ArrayList<Character> operators = new ArrayList<Character>();
		ArrayList<Factor> factors = new ArrayList<Factor>();
		factors.add(parseFactor());
		
		while (currenToken.getTokenName() == Tokens.MULTIPLICATIVE_OP) {
			operators.add(currenToken.getMsg().charAt(0));
			consumeToken();
			
			factors.add(parseFactor());
		}
		
		return new Term(factors, operators);
	}
	
	private Factor parseFactor() throws ParserException {
		Factor factor = null; 
		// compiler doesn't notice that throw... method is used as macro for quick message construction
		// and warns about value being not initialized but it should never be null
		
		if (currenToken.getTokenName() == Tokens.INT) {
			Integer integer = Integer.parseInt(currenToken.getMsg());
			consumeToken();
			factor = new Factor(integer);
		}
		else if (currenToken.getTokenName() == Tokens.IDENTIFIER) {
			Variable variable = parseVariable();
			factor = new Factor(variable);
		}
		else if (currenToken.getTokenName() == Tokens.OPEN_ROUND) {
			consumeToken();
			Expression expression = parseExpression();
			
			if (currenToken.getTokenName() != Tokens.CLOSE_ROUND)
				throwMessageExpected("parse factor", ")");
			consumeToken();
			factor = new Factor(expression);
		}
		else 
			throwMessageExpected("parse factor", "integer, variable or expression");
		
		return factor;
	}
	
	// doesnt check if this ends in int or if the path is correct at all
	private Variable parseVariable() throws ParserException {
		ArrayList<String> identifiers = new ArrayList<String>();
		
		while (true) {
			if (currenToken.getTokenName() != Tokens.IDENTIFIER)
				throwMessageExpected("parse variable", "identifier");
			
			identifiers.add(currenToken.getMsg());
			consumeToken();
			
			if (currenToken.getTokenName() != Tokens.DOT)
				break;
			consumeToken();
		}
		
		return new Variable(identifiers);
	}
	
	private Value parseSingle(String identifier, Type type) throws ParserException {
		if (currenToken.getTokenName() != Tokens.EQUAL)
			throwMessageExpected("parse non-array value", "=");
		consumeToken();
		
		Value value = parseRightHand(identifier, type); 
		
		if (currenToken.getTokenName() != Tokens.SEMICOLON)
			throwMessageExpected("parse non-array value", ";");
		consumeToken();
		return value;
	}

	private Value parseRightHand(String identifier, Type type) throws ParserException {
		Value value = null;	
		// compiler doesn't notice that throw... method is used as macro for quick message construction
		// and warns about value being not initialized but it should never be null
		
		if (type.getType() == Types.INTEGER)
			value = parseIntegerVal(identifier, type);
		else if (type.getType() == Types.DOUBLE)
			value =  parseDoubleVal(identifier, type);
		else if (type.getType() == Types.STRING)
			value =  parseStringVal(identifier, type);
		else if (type.getType() == Types.ENUM)
			value =  parseEnumVal(identifier, type);
		else if (type.getType() == Types.STRUCT)
			value =  parseStructVal(identifier, type);
		else if (type.getType() == Types.CHOICE)
			value =  parseChoiceVal(identifier, type);
		else 
			throwMessageExpected("parse right hand", "some type of value");
		
		return value;
	}
	
	private IntegerVal parseIntegerVal(String identifier, Type type) throws ParserException {
		if (currenToken.getTokenName() != Tokens.INT)
			throwMessageExpected("parse integer value", "integer");
			
		Integer value = Integer.parseInt(currenToken.getMsg());
		
		consumeToken();
		return new IntegerVal((NewInteger)type, identifier, value);
	}
	
	private StringVal parseStringVal(String identifier, Type type) throws ParserException {
		if (currenToken.getTokenName() != Tokens.STRING)
			throwMessageExpected("parse string value", "string");
		
		String value = currenToken.getMsg();
		consumeToken();
		return new StringVal((NewString)type, identifier, value);
	}
	
	private DoubleVal parseDoubleVal(String identifier, Type type) throws ParserException {
		if (currenToken.getTokenName() != Tokens.DOUBLE)
			throwMessageExpected("parse double value", "double");
		
		Double value = Double.parseDouble(currenToken.getMsg());
		consumeToken();
		return new DoubleVal((NewDouble)type, identifier, value);
	}
	
	// checks if string actually exists in enum
	private EnumVal parseEnumVal(String identifier, Type type) throws ParserException {
		NewEnum typeEnum = (NewEnum) type;
		
		if (currenToken.getTokenName() != Tokens.STRING)
			throwMessageExpected("parse enum value", "enumerator");

		String value = currenToken.getMsg();
		consumeToken();
		
		if (!typeEnum.getValues().contains(value))
			throwMessageExpected("parse enum value", "enumerator");
		
		return new EnumVal(typeEnum, identifier, value);
	}
	
	private StructVal parseStructVal(String identifier, Type supertype) throws ParserException {
		NewStruct typeStruct = (NewStruct) supertype;
		ArrayList<Value> values = new ArrayList<Value>();
		Value value;
		
		if (currenToken.getTokenName() != Tokens.OPEN_CURLY)
			throwMessageExpected("parse struct value", "{");
		consumeToken();
		
		for (int i = 0; i < typeStruct.getMembers().size(); i++) {
			Type type = typeStruct.getMembers().get(i);
			value = parseRightHand(type.getIdentifier(), type);
			if (value.getTypeName() != type.getType())
				throwMessageExpected("parse struct value", "same value type as struct member type");
			values.add(value);
			
			if (currenToken.getTokenName() != Tokens.COMMA)
				break;
			consumeToken();
		}
		
		if (values.size() != typeStruct.getMembers().size())
			throwMessageExpected("parse struct value", "the same number of values as types in struct");
		
		if (currenToken.getTokenName() != Tokens.CLOSE_CURLY)
			throwMessageExpected("parse struct value", "}");
		consumeToken();
		
		return new StructVal(typeStruct, identifier, values);
	}

	private ChoiceVal parseChoiceVal(String identifier, Type supertype) throws ParserException {
		NewChoice typeChoice = (NewChoice) supertype;
		
		String choiceString; // this is one of values of ENUM, that decides which type to use
		
		if (currenToken.getTokenName() != Tokens.STRING)
			throwMessageExpected("parse choice value", "enum value");
		choiceString = currenToken.getMsg();
		consumeToken();
		
		// now we look for this enumerator INDEX, because this way we translate it into object (1st enumerator corrseponds to
		// first type in Choice)
		int index = -1;
		for (int i = 0; i < typeChoice.getValues().size(); i++) {
			if (typeChoice.getChoice().getValues().get(i).equalsIgnoreCase(choiceString)) {
				index = i;
				break;
			}
		}
		
		if (index == -1)
			throwMessageExpected("parse choice value", "enum value");
		
		// now that we have proper index, we grab the right TYPE from existing types
		String typeName = typeChoice.getValues().get(index);
		Type type = null;
		for (Type type2: tree.types) {
			if (type2.getIdentifier().equalsIgnoreCase(typeName)) {
				type = type2;
				break;
			}
		}
		
		if (type == null)
			throwMessageExpected("parse choice value", "value of choice at same index as enumerator in enum to be a type name");
		
		// now that we have proper TYPE, we can parse it
		Value value = parseRightHand(type.getIdentifier(), type);
		
		if (value == null)
			throwMessage("parse choice value", "Unknown error while parsing right hand. Should never happen.");
		
		return new ChoiceVal(typeChoice, identifier, typeChoice, value);
	}
	
// ----------------------- OTHER
	private int consumeToken() throws ParserException {		
		try {
			currenToken = lexer.getNextToken();
		} catch (LexerException e) {
			throwMessage("consume token", "Lexer failed");
		} catch (IOException e) {
			return -1;
		}
		
		return 0;
	}
	
	private void throwMessageExpected(String method, String expected) throws ParserException{
		throw new ParserException("Error parsing " + method + ". Expected: " + expected + " at line: " 
				+ currenToken.getPos().getLine() + " column: " + currenToken.getPos().getColumn());		
	}
	
	private void throwMessage(String method, String msg) throws ParserException{
		throw new ParserException("Error in " + method + ". Message: " + msg + ". At line: " 
				+ currenToken.getPos().getLine() + " column: " + currenToken.getPos().getColumn());		
	}
	
	public MsgTree getTree() {
		return tree;
	}

}
