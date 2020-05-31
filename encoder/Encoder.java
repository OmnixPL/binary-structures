package encoder;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import lexer.Lexer;
import parser.MsgTree;
import parser.Parser;
import parser.arithmetics.ArrayArithmeticException;
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
		ArrayList<Byte> toCompress = encoder.encode(args[0]);
		ArrayList<Byte> compressed = compressBits(toCompress);
		saveToFile(compressed, args[1]);
	}
	
	ArrayList<Byte> encode(String filename) {
		ArrayList<Byte> result = new ArrayList<Byte>();
		ArrayList<Byte> types = new ArrayList<Byte>();
		ArrayList<Byte> values = new ArrayList<Byte>();
		
		buildTree(filename);
		
		if (tree == null)
			return null;

		typesNo = tree.getTypes().size();
		bitsForType = calculateBitsNo(typesNo); // index 0 is reserved for array
												// but size returns number of elements, not highest index, so it evens out
		
		appendByte(result, typesNo);
		try {
			types = encodeTypes(tree.getTypes());
			values = encodeValues(tree.getValues());			
		}
		catch (EncoderException e) {
			e.printStackTrace();
		}
		
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
	
	private ArrayList<Byte> encodeValues(ArrayList<Value> values) throws EncoderException {
		ArrayList<Byte> list = new ArrayList<Byte>();
		
		for (Value value : values) {
			int i = getTypeIndex(value.getType());
			if (value.getTypeName() == Types.ARRAY)
				appendTypeNo(list, -1);
			else
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
			else if (value.getTypeName() == Types.ARRAY)
				list.addAll(encodeArrayVal(value));
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
	
	private ArrayList<Byte> encodeArrayVal(Value superValue) throws EncoderException {
		ArrayList<Byte> list = new ArrayList<Byte>();
		ArrayVal value = (ArrayVal)superValue;
		
		int size;
		try {
			size = (int) value.getElementsNo().evaluate(tree);
		} catch (ArrayArithmeticException e) {
			e.printStackTrace();
			throw new EncoderException("ArrayArithmeticException caught");
		}
		
		if (size < value.getValues().size())
			throw new EncoderException("Array size is too small for elements provided");
		
		appendByte(list, size);
		
		int i;
		for (i = 0; i < tree.getTypes().size(); i++)
			if (tree.getTypes().get(i).getIdentifier().equalsIgnoreCase(value.getType().getIdentifier()))
				break;
		
		appendTypeNo(list, i);
		
		list.addAll(encodeValuesNoTypeIndex(value.getValues()));
		
		return list;
	}
	
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
			System.out.println("File couldn't be found");
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
	
	public static ArrayList<Byte> compressBits(ArrayList<Byte> list) {
		ArrayList<Byte> output = new ArrayList<Byte>();
		int i;
		int temp;
		for (i = 0; i < list.size() - 8; ) {
			temp = 0;
			for (int inner = 7; inner >= 0; inner--) {
				temp += list.get(i) << inner;
				i++;
			}
			output.add((byte)temp);
		}
		temp = 0;
		int goal = i + 8 - list.size(); 
		for (int inner = 7; inner >= goal; inner--) {
			temp += list.get(i) << inner;
			i++;
		}
		output.add((byte)temp);
		return output;
	}
	
	public static void saveToFile(ArrayList<Byte> list, String fileName) {
	    try {
	    	FileOutputStream outputStream = new FileOutputStream(fileName);
	    	for (Byte byte1 : list)
	    		outputStream.write(byte1);			
	    	outputStream.close();		
		} catch (FileNotFoundException e) {
			System.out.println("Output file could not be created.");
		} catch (IOException e) {	
	    	System.out.println("Error while trying to write to file.");
		}
	}
}