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

// TODO change all while statements into dowhile loops
// TODO add check if identifier isnt used yet (but can be a keyword like Double ;) )
// TODO check if choice values are actual types
// TODO DODAJ OBOSTRZENIE ZE BITOW W INCIE NIE MOZE BYC WIECEJ NIZ 32 I BEDZIESZ MIAL LATWO XD
// TODO Array jest zrobiony w połowe (lewa strona), ale prawa nie, musiałem przerwać, ponieważ potrzebuję pozostałe typy żeby robić z nich arraye
// TODO zrob test czy tam gdzie sa casty to naprawde jest ten typ
public class Parser {
	private Lexer lexer;
	private MsgTree tree;
	private Token currenToken;
	
	public Parser(Lexer lexer) {
		this.lexer = lexer;
		tree = new MsgTree();
	}
	
	public int buildParseTree() {
		consumeToken(); // actually initialize
		
		// parse "MESSAGE" header
		if (currenToken.getTokenName() != Tokens.MESSAGE) 
			return -1;
		consumeToken();
		
		parseTypes();
		
		parseValues();
		
		return 0;
	}
	
// ----------------------- PARSE TYPES 
	private int parseTypes() {
		Type type;
		if (currenToken.getTokenName() != Tokens.TYPES)
			return -1;
		consumeToken();
		
		// parse built-in type name
		while (currenToken.getTokenName() == Tokens.IDENTIFIER) {
			if ( (type = parseType()) != null)
				tree.types.add(type);
			else break;
		}
		
		System.out.println("--- TYPES FINISHED");
		for (Type type2 : tree.types)
			System.out.println(type2.toString(0));
		return 0;
	}
	
	private Type parseType() {
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
		// TODO add nested ???
		
		return type;
	}
	
	private NewInteger parseInteger() {
		consumeToken();

		if (currenToken.getTokenName() != Tokens.OPEN_ANGLE)
			return null;
		consumeToken();
		
		// parse bits number
		if (currenToken.getTokenName() != Tokens.INT)
			return null;
		Integer bits = Integer.parseInt(currenToken.getMsg());
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.CLOSE_ANGLE)
			return null;
		consumeToken();
		
		String identifier;
		if ( (identifier = parseTypeName()) == null )
			return null;
		
		System.out.println("New Integer " + bits.toString() + " : " + identifier);
		return new NewInteger(identifier, bits);
	}
	
	private NewDouble parseDouble() {
		consumeToken();
		String identifier;
		if ( (identifier = parseTypeName()) == null )
			return null;
		
		System.out.println("New Double: " + identifier);
		return new NewDouble(identifier);
	}
	
	private NewEnum parseEnum() {
		ArrayList<String> values = new ArrayList<String>();
		
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.OPEN_CURLY)
			return null;
		consumeToken();
		
		// parse enumerators
		while (true) {
			if (currenToken.getTokenName() != Tokens.STRING)
				return null;
			values.add(currenToken.getMsg());
			consumeToken();
			
			if (currenToken.getTokenName() != Tokens.COMMA)
				break;
			consumeToken();
		}
		
		if (currenToken.getTokenName() != Tokens.CLOSE_CURLY)
			return null;
		consumeToken();
		
		String identifier;
		if ( (identifier = parseTypeName()) == null )
			return null;
		
		System.out.println("New Enum with " + values.size() + " enumerators: " + identifier);
		return new NewEnum(identifier, values);
	}
	
	private NewString parseString() {
		consumeToken();
		
		String identifier;
		if ( (identifier = parseTypeName()) == null )
			return null;
		
		System.out.println("New String: " + identifier);
		return new NewString(identifier);
	}
	
	private NewStruct parseStruct() {
		Type type;
		ArrayList<Type> members = new ArrayList<Type>();
		
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.OPEN_CURLY)
			return null;
		consumeToken();
		
		while (true) {
			type = parseType();
			if (type != null)
				members.add(type);
			else 
				break;
		}
		
		if (currenToken.getTokenName() != Tokens.CLOSE_CURLY)
			return null;
		consumeToken();
		
		String identifier;
		if ( (identifier = parseTypeName()) == null )
			return null;
		
		System.out.println("New Struct with " + members.size() + " types: " + identifier);
		return new NewStruct(identifier, members);
	}
	
	private NewChoice parseChoice() {
		NewEnum newEnum;
		ArrayList<String> values = new ArrayList<String>();
		
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.OPEN_ANGLE)
			return null;
		consumeToken();
		
		String enumName;
		if (currenToken.getTokenName() != Tokens.IDENTIFIER)
			return null;
		enumName = currenToken.getMsg();
		consumeToken();
		
		if ( (newEnum = findEnumByName(enumName)) == null)
			return null;
		
		if (currenToken.getTokenName() != Tokens.CLOSE_ANGLE)
			return null;
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.OPEN_CURLY)
			return null;
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.IDENTIFIER)
			return null;
		do {
			if ( findTypeByName(currenToken.getMsg()) == null)
				return null;
			
			values.add(currenToken.getMsg());
			consumeToken();
			
			if (currenToken.getTokenName() != Tokens.SEMICOLON)
				return null;
			consumeToken();
			
		} while (currenToken.getTokenName() == Tokens.IDENTIFIER);
		
		if (currenToken.getTokenName() != Tokens.CLOSE_CURLY)
			return null;
		consumeToken();
		
		String identifier;
		if ( (identifier = parseTypeName()) == null )
			return null;
		
		if (values.size() != newEnum.getValues().size())
			return null;
		
		System.out.println("New Choice");
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
	private String parseTypeName() {
		if (currenToken.getTokenName() != Tokens.IDENTIFIER)
			return null;
		String identifier = currenToken.getMsg();
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.SEMICOLON)
			return null;
		consumeToken();
		
		return identifier;
	}

	
// ----------------------- PARSE VALUES
	private int parseValues() {
		Value value;
		
		if (currenToken.getTokenName() != Tokens.VALUES)
			return -1;
		consumeToken();
		
		while (currenToken.getTokenName() == Tokens.IDENTIFIER) {
			if ( (value = parseValue()) != null)
				tree.values.add(value);
			else break;
		}

		System.out.println("--- VALUES FINISHED");
		for (Value value2: tree.values)
			System.out.println(value2.toString(0));
		return 0;
	}
	
	// TODO NIC NIE ZWRACA NA RAZIE
	private Value parseValue() {
		Type type = null;
		Value value = null;
		String typeName;
		String identifier;

		if (currenToken.getTokenName() != Tokens.IDENTIFIER)
			return null;
		typeName = currenToken.getMsg();
		consumeToken();
		
		for (Type type2 : tree.types) {
			if (typeName.equalsIgnoreCase(type2.getIdentifier())) {
				type = type2;
				break;
			}
		}
		
		if (type == null)
			return null;
		
		if (currenToken.getTokenName() != Tokens.IDENTIFIER)
			return null;
		identifier = currenToken.getMsg();
		consumeToken();
		
		if ( (value = parseArrayVal(identifier, type)) == null)
			value = parseSingle(identifier, type);
		
		
		return value;
	}
	
	// TODO NIE ZWRACA NIC NA RAZIE
	private ArrayVal parseArrayVal(String identifier, Type type) {
		ArrayList<Value> values = new ArrayList<Value>();
		Value value;
		if (currenToken.getTokenName() != Tokens.OPEN_SQUARE)
			return null;	// NO ARRAY - NOT AN ERROR
		consumeToken();
		
		// TODO arithmetics are fucking muda
		parseArithmetic();
		
		if (currenToken.getTokenName() != Tokens.CLOSE_SQUARE)
			return null;	// ACTUALLY AN ERROR
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.EQUAL)
			return null;
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.OPEN_CURLY)
			return null;
		consumeToken();
		
		while (true) {
			value = parseRightHand(identifier, type); 
			if (value == null)
				return null;
			values.add(value);
			
			if (currenToken.getTokenName() != Tokens.COMMA)
				break;
			consumeToken();
		}
		
		if (currenToken.getTokenName() != Tokens.CLOSE_CURLY)
			return null;
		consumeToken();
		
		if (currenToken.getTokenName() != Tokens.SEMICOLON)
			return null;
		consumeToken();
		return new ArrayVal(type, identifier, values);
	}
	
	// TODO idk if this should return Expression or evaluate it already?
	private Expression parseArithmetic() {
		return parseExpression();
	}
	
	// TODO NIE MA REAKCJI NA NULLE
	private Expression parseExpression() {
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

	// TODO NIE MA REAKCJI NA NULLE
	private Term parseTerm() {
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
	
	// TODO NIE MA REAKCJI NA NULLE
	private Factor parseFactor() {
		Factor factor;
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
				return null;
			consumeToken();
			factor = new Factor(expression);
		}
		else factor = null;
		
		return factor;
	}
	
	// TODO NIE MA REAKCJI NA NULLE
	// TODO nie sprawdza czy to serio są inty ani czy istnieja 
	private Variable parseVariable() {
		ArrayList<String> identifiers = new ArrayList<String>();
		
		while (true) {
			if (currenToken.getTokenName() != Tokens.IDENTIFIER)
				return null;	
			
			identifiers.add(currenToken.getMsg());
			consumeToken();
			
			if (currenToken.getTokenName() != Tokens.DOT)
				break;
			consumeToken();
		}
		
		return new Variable(identifiers);
	}
	
	private Value parseSingle(String identifier, Type type) {
		if (currenToken.getTokenName() != Tokens.EQUAL)
			return null;
		consumeToken();
		
		Value value = parseRightHand(identifier, type); 
		
		if (currenToken.getTokenName() != Tokens.SEMICOLON)
			return null;
		consumeToken();
		return value;
	}

	private Value parseRightHand(String identifier, Type type) {
		Value value = null;
		
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
		
		return value;
	}
	
	private IntegerVal parseIntegerVal(String identifier, Type type) {
		Integer value = Integer.parseInt(currenToken.getMsg());
		consumeToken();
		return new IntegerVal((NewInteger)type, identifier, value);
	}
	
	private StringVal parseStringVal(String identifier, Type type) {
		String value = currenToken.getMsg();
		consumeToken();
		return new StringVal((NewString)type, identifier, value);
	}
	
	private DoubleVal parseDoubleVal(String identifier, Type type) {
		Double value = Double.parseDouble(currenToken.getMsg());
		consumeToken();
		return new DoubleVal((NewDouble)type, identifier, value);
	}
	
	// checks if string actually exists in enum
	private EnumVal parseEnumVal(String identifier, Type type) {
		NewEnum typeEnum = (NewEnum) type;
		String value = currenToken.getMsg();
		consumeToken();
		
		if (!typeEnum.getValues().contains(value))
			return null;
		
		return new EnumVal(typeEnum, identifier, value);
	}
	
	private StructVal parseStructVal(String identifier, Type supertype) {
		NewStruct typeStruct = (NewStruct) supertype;
		ArrayList<Value> values = new ArrayList<Value>();
		Value value;
		
		if (currenToken.getTokenName() != Tokens.OPEN_CURLY)
			return null;
		consumeToken();
		
		for (int i = 0; i < typeStruct.getMembers().size(); i++) {
			Type type = typeStruct.getMembers().get(i);
			value = parseRightHand(type.getIdentifier(), type);
			if (value.getTypeName() != type.getType())
				return null;
			values.add(value);
			
			if (currenToken.getTokenName() != Tokens.COMMA)
				break;
			consumeToken();
		}
		
		if (values.size() != typeStruct.getMembers().size())
			return null;
		
		if (currenToken.getTokenName() != Tokens.CLOSE_CURLY)
			return null;
		consumeToken();
		
		return new StructVal(typeStruct, identifier, values);
	}

	private ChoiceVal parseChoiceVal(String identifier, Type supertype) {
		NewChoice typeChoice = (NewChoice) supertype;
		
		String choiceString; // this is one of values of ENUM, that decides which type to use
		
		if (currenToken.getTokenName() != Tokens.STRING)
			return null;
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
			return null;
		
		// now that we have proper index, we grab the right TYPE from existing types
		String typeName = typeChoice.getValues().get(index);
		Type type = null;
		for (Type type2: tree.types) {
			if (type2.getIdentifier().equalsIgnoreCase(typeName))
				type = type2;
				break;
		}
		
		if (type == null)
			return null;
		
		// now that we have proper TYPE, we can parse it
		Value value = parseRightHand(type.getIdentifier(), type);
		
		if (value == null)
			return null;
		
		return new ChoiceVal(typeChoice, identifier, typeChoice, value);
	}
	
// ----------------------- OTHER
	private int consumeToken() {		
		try {
			currenToken = lexer.getNextToken();
		} catch (LexerException e) {
			return -1;		// TODO rly bad idea to return null dude
		} catch (IOException e) {
			return -1;
		}
		
		return 0;
	}
}
