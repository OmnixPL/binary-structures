package encoder;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import lexer.Lexer;
import parser.MsgTree;
import parser.Parser;
import parser.types.*;
import parser.values.*;
import reader.CodeReader;

public class Encoder {
	MsgTree tree;
	int typesNo;
	int bitsForType;
	ArrayList<Byte> encoded = new ArrayList<Byte>();
	
	public static void main(String[] args) {
		Encoder encoder = new Encoder();
		encoder.encode(args[0]);
	}

//	public static void main(String[] args) {
//	}
	
	ArrayList<Byte> encode(String filename) {
		ArrayList<Byte> result = new ArrayList<Byte>();
		ArrayList<Byte> types = new ArrayList<Byte>();
		ArrayList<Byte> values = new ArrayList<Byte>();
		
		buildTree(filename);
		
		// TODO throw?
		if (tree == null)
			return null;

		typesNo = tree.getTypes().size();
		bitsForType = calculateBitsNo(typesNo); // index 0 is reserved for array
												// but size returns number of elements, not highest index, so it evens out
		
		appendByte(result, typesNo);
		types = encodeTypes(tree.getTypes());
		values = encodeValues(tree.getValues());
		
		result.addAll(types);
		result.addAll(values);
		return result;
	}
	
	private ArrayList<Byte> encodeTypes(ArrayList<Type> types) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		
		for (Type type : types) {
			if (type.getType() == Types.INTEGER)
				list.addAll(encodeInteger(type));
			else if (type.getType() == Types.DOUBLE)
				list.addAll(encodeDouble(type));
			else if (type.getType() == Types.ENUM)
				list.addAll(encodeEnum(type));
			else if (type.getType() == Types.STRING)
				list.addAll(encodeString(type));
			else if (type.getType() == Types.STRUCT)
				list.addAll(encodeStruct(type));
			else if (type.getType() == Types.CHOICE)
				list.addAll(encodeChoice(type));
		}
		
		return list;
	}
	
	ArrayList<Byte> encodeInteger(Type supertype) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		
		NewInteger type = (NewInteger)supertype;

		appendCode(list, 0,0,1);
		
		int bits = type.getBits();
		appendByte(list, bits);
		
		return list;
	}
	
	ArrayList<Byte> encodeDouble(Type supertype) {
		ArrayList<Byte> list = new ArrayList<Byte>();

		appendCode(list, 0,1,0);
		
		return list;
	}
	
	ArrayList<Byte> encodeEnum(Type supertype) {
		ArrayList<Byte> list = new ArrayList<Byte>();

		NewEnum type = (NewEnum)supertype;

		appendCode(list, 0,1,1);
		
		appendByte(list, type.getValues().size());
		
		for (String value : type.getValues()) {
			appendString(list, value);
		}
		
		return list;
	}
	
	ArrayList<Byte> encodeString(Type supertype) {
		ArrayList<Byte> list = new ArrayList<Byte>();

		appendCode(list, 1,0,0);
		
		return list;
	}
	
	ArrayList<Byte> encodeStruct(Type supertype) {
		ArrayList<Byte> list = new ArrayList<Byte>();

		NewStruct type = (NewStruct)supertype;
		
		appendCode(list, 1,0,1);
		
		list.addAll(encodeTypes(type.getMembers()));
		
		appendCode(list, 1,1,0);
		return list;
	}
	
	ArrayList<Byte> encodeChoice(Type supertype) {
		ArrayList<Byte> list = new ArrayList<Byte>();

		NewChoice type = (NewChoice)supertype;
		
		appendCode(list, 1,1,1);
		 
		int i;
		for (i = 0; i < tree.getTypes().size(); i++) {
			if (tree.getTypes().get(i).equals(type.getChoosingEnum()))
				break;
		}
		
		appendTypeNo(list, i);
		
		for (int j = 0; j < type.getValues().size(); j++) {
			for (i = 0; i < tree.getTypes().size(); i++) {
				if (tree.getTypes().get(i).getIdentifier().equals(type.getValues().get(j)))
					break;
			}
			appendTypeNo(list, i);
		}
		
		return list;
	}
	
	private ArrayList<Byte> encodeValues(ArrayList<Value> values) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		
		for (Value value : values) {
			int i = getTypeIndex(value.getType());
			appendTypeNo(list, i);
			if (value.getTypeName() == Types.INTEGER)
				list.addAll(encodeIntegerVal(value));
			else if (value.getTypeName() == Types.DOUBLE)
				list.addAll(encodeDoubleVal(value));
			else if (value.getTypeName() == Types.ENUM)
				list.addAll(encodeEnumVal(value));
			else if (value.getTypeName() == Types.STRING)
				list.addAll(encodeStringVal(value));
			else if (value.getTypeName() == Types.STRUCT)
				list.addAll(encodeStructVal(value));
			else if (value.getTypeName() == Types.CHOICE)
				list.addAll(encodeChoiceVal(value));
//			else TODO throw something?
		}
		
		return list;
	}
	
	private ArrayList<Byte> encodeValuesNoTypeIndex(ArrayList<Value> values) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		
		for (Value value : values) {
			if (value.getTypeName() == Types.INTEGER)
				list.addAll(encodeIntegerVal(value));
			else if (value.getTypeName() == Types.DOUBLE)
				list.addAll(encodeDoubleVal(value));
			else if (value.getTypeName() == Types.ENUM)
				list.addAll(encodeEnumVal(value));
			else if (value.getTypeName() == Types.STRING)
				list.addAll(encodeStringVal(value));
			else if (value.getTypeName() == Types.STRUCT)
				list.addAll(encodeStructVal(value));
			else if (value.getTypeName() == Types.CHOICE)
				list.addAll(encodeChoiceVal(value));
//			else TODO throw something?
		}
		
		return list;
	}
	
	private ArrayList<Byte> encodeIntegerVal(Value superValue) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		IntegerVal value = (IntegerVal)superValue;
		NewInteger type = (NewInteger)value.getType();
		
		appendBits(list, value.getValue(), type.getBits());
		
		return list;
	}
	
	private ArrayList<Byte> encodeDoubleVal(Value superValue) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		DoubleVal value = (DoubleVal)superValue;
		
		appendLong(list, Double.doubleToRawLongBits(value.getValue()), 64);
		return list;
	}
	
	private ArrayList<Byte> encodeEnumVal(Value superValue) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		EnumVal value = (EnumVal)superValue;
		NewEnum type = (NewEnum)value.getType();
		ArrayList<String> values = type.getValues();
		
		int bits = calculateBitsNo(values.size() - 1);
		
		int index;
		for (index = 0; index < values.size(); index++) {
			if (values.get(index).equals(value.getValue()))
				break;
		}
		
		appendBits(list, index, bits);
		return list;
	}
	
	private ArrayList<Byte> encodeStringVal(Value superValue) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		StringVal value = (StringVal)superValue;
		
		appendString(list, value.getValue());
		return list;
	}
	
	private ArrayList<Byte> encodeStructVal(Value superValue) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		StructVal value = (StructVal)superValue;
		
		list.addAll(encodeValuesNoTypeIndex(value.getValues()));
		
		return list;
	}
	
	private ArrayList<Byte> encodeChoiceVal(Value superValue) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		ChoiceVal value = (ChoiceVal)superValue;
		NewChoice type = (NewChoice)value.getType();
		
		int i;
		for (i = 0; i < type.getValues().size(); i++) {
			if (value.getChosenType().getIdentifier().equalsIgnoreCase(type.getValues().get(i)))
				break;
		}
		int length = calculateBitsNo(type.getValues().size() - 1);
		
		appendBits(list, i, length);
		
		ArrayList<Value> valueInList = new ArrayList<Value>();
		valueInList.add(value.getValue());
		list.addAll(encodeValuesNoTypeIndex(valueInList));
		
		return list;
	}
	
	// TODO jeszcze tablica
	
	private MsgTree buildTree(String filename) {
		CodeReader cr;
		Lexer lexer;
		Parser parser;
		
		try (BufferedReader bf = new BufferedReader(new FileReader(filename))) {
			cr = new CodeReader(bf);
			lexer = new Lexer(cr);
			parser = new Parser(lexer);
			
			parser.buildParseTree();			
			tree = parser.getTree();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private int getTypeIndex(Type type) {
		for (int i = 0; i < tree.getTypes().size(); i++) {
			if (tree.getTypes().get(i).equals(type))
				return i;
		}
		return -1;
	}

	private void appendLong(ArrayList<Byte> list, long value, int length) {
		for (int i = length - 1; i >= 0; i--) {
			list.add((byte)((value >> i) & 0x1));
		}
	}
	
	void appendBits(ArrayList<Byte> list, int value, int length) {
		for (int i = length - 1; i >= 0; i--) {
			list.add((byte)((value >> i) & 0x1));
		}
	}
	
	
	private void appendByte(ArrayList<Byte> list, byte value) {
		appendBits(list, value, 8);
	}
	
	private void appendByte(ArrayList<Byte> list, int value) {
		appendByte(list, (byte)value);
	}
	
	private void appendByte(ArrayList<Byte> list, char value) {
		appendByte(list, (byte)value);
	}
	
	private void appendTypeNo(ArrayList<Byte> list, int value) {
		appendBits(list, value + 1, bitsForType);
	}
	
	private void appendString(ArrayList<Byte> list, String value) {
		for (int i = 0; i < value.length() ; i++) { 
		    appendByte(list, value.charAt(i)); 
		}
		appendByte(list, 0);
	}

	private void appendCode(ArrayList<Byte> list, int one, int two, int three) {
		list.add((byte)one);
		list.add((byte)two);
		list.add((byte)three);
	}
	
	int calculateBitsNo(int value) {
		int count = 0;
		while (value > 0) {
		    count++;
		    value = value >> 1;
		}
		
		return count;
	}
}


















//public static void main(String[] args) {
//		//noParser(args);
//		parsered(args);
//	}
//	
//	@SuppressWarnings("unused")
//	private static void parsered(String[] args) {
//		CodeReader cr;
//		Lexer lexer;
//		Parser parser;
//		
//		try (BufferedReader bf = new BufferedReader(new FileReader(args[0]))) {
//			cr = new CodeReader(bf);
//			lexer = new Lexer(cr);
//			parser = new Parser(lexer);
//			
//			parser.buildParseTree();			
//		}
//		catch (FileNotFoundException e) {
//		    e.printStackTrace();
//		}
//		catch (IOException e) {
//		    e.printStackTrace();
//		}
//		
//	}
//
//	@SuppressWarnings("unused")
//	private static void noParser(String[] args) {
//		CodeReader cr;
//		Lexer lexer;
//		Token token;
//		LinkedList<Token> output = new LinkedList<Token>();
//		LinkedList<Token> expected = new LinkedList<Token>();
//		boolean success = true;
//		
//		// parser placeholder
//		try (BufferedReader bf = new BufferedReader(new FileReader(args[0]))) {
//			cr = new CodeReader(bf);
//			lexer = new Lexer(cr);
//			
//			for (@SuppressWarnings("unused")	int i = 0 ; ; i++) {
//				//System.out.println(i);
//				try {
//					token = lexer.getNextToken();
//					output.add(token);
//					if (token.getTokenName() == Tokens.EOF) break;
//				} catch (LexerException e) {
//					System.out.println(e);
//				}
//			}
//			
//			
//		}
//		catch (FileNotFoundException e) {
//		    e.printStackTrace();
//		}
//		catch (IOException e) {
//		    e.printStackTrace();
//		}
//		
//		// testing
//		if(args[0].equals("Tokens")) {
//			populateExpected(expected);
//			while (!output.isEmpty()) {
//				if (!output.remove().equals(expected.remove())) {
//					System.out.println("Output token doesn't match expected");
//					success = false;
//				}
//			}
//			if(success)
//				System.out.println("All tokens are correct");
//		}
//		System.out.println("Finished");
//	}
//	
//	private static void populateExpected(LinkedList<Token> expected) {
//		expected.add(new Token(Tokens.MESSAGE, new Pos(1, 1), null));
//		expected.add(new Token(Tokens.TYPES, new Pos(2, 1), null));
//		expected.add(new Token(Tokens.VALUES, new Pos(3, 1), null));
//		expected.add(new Token(Tokens.IDENTIFIER, new Pos(4, 1), "ident"));
//		expected.add(new Token(Tokens.IDENTIFIER, new Pos(4, 7), "Identyfikator"));
//		expected.add(new Token(Tokens.INT, new Pos(6, 1), "0"));
//		expected.add(new Token(Tokens.INT, new Pos(6, 3), "3"));
//		expected.add(new Token(Tokens.INT, new Pos(6, 5), "4"));
//		expected.add(new Token(Tokens.DOUBLE, new Pos(6, 7), "0.4"));
//		expected.add(new Token(Tokens.ADDITIVE_OP, new Pos(6, 11), "-"));
//		expected.add(new Token(Tokens.INT, new Pos(6, 12), "3"));
//		expected.add(new Token(Tokens.DOUBLE, new Pos(6, 14), "0.02"));
//		expected.add(new Token(Tokens.OPEN_CURLY, new Pos(8, 1), "{"));
//		expected.add(new Token(Tokens.CLOSE_CURLY, new Pos(8, 2), "}"));
//		expected.add(new Token(Tokens.OPEN_SQUARE, new Pos(8, 3), "["));
//		expected.add(new Token(Tokens.CLOSE_SQUARE, new Pos(8, 4), "]"));
//		expected.add(new Token(Tokens.OPEN_ROUND, new Pos(8, 5), "("));
//		expected.add(new Token(Tokens.CLOSE_ROUND, new Pos(8, 6), ")"));
//		expected.add(new Token(Tokens.OPEN_ANGLE, new Pos(8, 7), "<"));
//		expected.add(new Token(Tokens.CLOSE_ANGLE, new Pos(8, 8), ">"));
//		expected.add(new Token(Tokens.COLON, new Pos(8, 9), ":"));
//		expected.add(new Token(Tokens.SEMICOLON, new Pos(8, 10), ";"));
//		expected.add(new Token(Tokens.COMMA, new Pos(8, 11), ","));
//		expected.add(new Token(Tokens.DOT, new Pos(8, 12), "."));
//		expected.add(new Token(Tokens.ADDITIVE_OP, new Pos(8, 13), "+"));
//		expected.add(new Token(Tokens.ADDITIVE_OP, new Pos(8, 14), "-"));
//		expected.add(new Token(Tokens.MULTIPLICATIVE_OP, new Pos(8, 15), "*"));
//		expected.add(new Token(Tokens.MULTIPLICATIVE_OP, new Pos(8, 16), "/"));
//		expected.add(new Token(Tokens.EQUAL, new Pos(8, 17), "="));
//		expected.add(new Token(Tokens.STRING, new Pos(10, 1), "STRING TEST"));
//		expected.add(new Token(Tokens.STRING, new Pos(11, 1), ""));
//		expected.add(new Token(Tokens.STRING, new Pos(12, 1), "slowo"));
//		expected.add(new Token(Tokens.STRING, new Pos(13, 1), " spacje "));
//		expected.add(new Token(Tokens.STRING, new Pos(14, 1), "znaki specjalne: {}[]()<>:;,.+-*/="));
//		expected.add(new Token(Tokens.STRING, new Pos(15, 1), "MESSAGE"));
//		expected.add(new Token(Tokens.STRING, new Pos(16, 1), "Dwie\nlinijki"));
//		expected.add(new Token(Tokens.STRING, new Pos(18, 1), "Trzy\ndlugie\nlinijki"));
//		expected.add(new Token(Tokens.STRING, new Pos(21, 1), "Escape\\n\\t\\r"));
//		expected.add(new Token(Tokens.IDENTIFIER, new Pos(23, 1), "Errors"));
//		expected.add(new Token(Tokens.EOF, new Pos(26, 1), null));
//	}
//}