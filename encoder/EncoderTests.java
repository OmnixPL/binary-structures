package encoder;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import parser.MsgTree;
import parser.types.NewChoice;
import parser.types.NewDouble;
import parser.types.NewEnum;
import parser.types.NewInteger;
import parser.types.NewString;
import parser.types.NewStruct;
import parser.types.Type;

class EncoderTests {
	
	@Test
	void testAppendBits() {
		Encoder encoder = new Encoder();
		
		ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
				(byte)1, (byte)1, (byte)0, (byte)1, (byte)0, (byte)1, (byte)0, (byte)1));
		
		ArrayList<Byte> output = new ArrayList<Byte>();
		encoder.appendBits(output, 213, 8);
		assertIterableEquals(expected, output);
	}
	
	@Test
	void testCompressBits() {
		ArrayList<Byte> input = new ArrayList<Byte>(List.of(
				(byte)1, (byte)1, (byte)0, (byte)1, (byte)0, (byte)1, (byte)0, (byte)1,
				(byte)1, (byte)1, (byte)1, (byte)1));
		ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
				(byte)213, (byte)240));
		ArrayList<Byte> output = Encoder.compressBits(input);
		assertIterableEquals(expected, output);
	}
	
	@Nested
	class EncodingTypes {
		@Test
		void testEncodeInteger() {
			Encoder encoder = new Encoder();
			
			ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
					// integer encoded as 001
					(byte)0, (byte)0, (byte)1,
					// 30 bits long
					(byte)0, (byte)0, (byte)0, (byte)1,
					(byte)1, (byte)1, (byte)1, (byte)0));
			
			NewInteger newInteger = new NewInteger("UNIMPORTANT", 30);
			ArrayList<Byte> output = encoder.encodeInteger(newInteger);
			assertIterableEquals(expected, output);
		}
		
		@Test
		void testEncodeDouble() {
			Encoder encoder = new Encoder();
			
			ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
					// double encoded as 010
					(byte)0, (byte)1, (byte)0));
			
			NewDouble newDouble= new NewDouble("UNIMPORTANT");
			ArrayList<Byte> output = encoder.encodeDouble(newDouble);
			assertIterableEquals(expected, output);
		}
		
		@Test
		void testEncodeEnum() {
			Encoder encoder = new Encoder();
			ArrayList<String> values = new ArrayList<String>(List.of("ABC", "x z"));
			
			ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
					// enum encoded as 011
					(byte)0, (byte)1, (byte)1,
					// 2 enumerators
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)0, (byte)1, (byte)0,
					// 1st enumerator: "ABC\0"
					(byte)0, (byte)1, (byte)0, (byte)0,
					(byte)0, (byte)0, (byte)0, (byte)1,
					
					(byte)0, (byte)1, (byte)0, (byte)0,
					(byte)0, (byte)0, (byte)1, (byte)0,
					
					(byte)0, (byte)1, (byte)0, (byte)0,
					(byte)0, (byte)0, (byte)1, (byte)1,
					
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)0, (byte)0, (byte)0,
					// 2nd enumerator: "x z"
					(byte)0, (byte)1, (byte)1, (byte)1,
					(byte)1, (byte)0, (byte)0, (byte)0,
					
					(byte)0, (byte)0, (byte)1, (byte)0,
					(byte)0, (byte)0, (byte)0, (byte)0,
					
					(byte)0, (byte)1, (byte)1, (byte)1,
					(byte)1, (byte)0, (byte)1, (byte)0,
					
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)0, (byte)0, (byte)0));
			
			NewEnum newEnum= new NewEnum("UNIMPORTANT", values);
			ArrayList<Byte> output = encoder.encodeEnum(newEnum);
			assertIterableEquals(expected, output);
		}
		
		@Test
		void testEncodeString() {
			Encoder encoder = new Encoder();
			
			ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
					// string encoded as 100
					(byte)1, (byte)0, (byte)0));
			
			NewString newString = new NewString("UNIMPORTANT");
			ArrayList<Byte> output = encoder.encodeString(newString);
			assertIterableEquals(expected, output);
		}
		
		@Test
		void testEncodeStruct() {
			Encoder encoder = new Encoder();
			ArrayList<Type> types = new ArrayList<Type>();
			
			ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
					// struct beginning encoded as 101
					(byte)1, (byte)0, (byte)1,
					
					// other types are tested separately
					
					// struct ending encoded as 110
					(byte)1, (byte)1, (byte)0));
			
			
			NewStruct newStruct = new NewStruct("UNIMPORTANT", types);
			ArrayList<Byte> output = encoder.encodeStruct(newStruct);
			assertIterableEquals(expected, output);
		}
		
		@Test
		void testEncodeChoice() {
			Encoder encoder = new Encoder();
			
			// preparing types for tree
			ArrayList<Type> types;
			ArrayList<String> enumValues = new ArrayList<String>(List.of("A", "B"));
			ArrayList<String> names = new ArrayList<String>(List.of("TYPEB", "TYPEA"));
			NewEnum enum1 = new NewEnum("UNIMPORTANT", enumValues);
			NewString type1 = new NewString("TYPEA"); 
			NewInteger type2 = new NewInteger("TYPEB", 12);
			NewChoice newChoice = new NewChoice("UNIMPORTANT", enum1, names);
			types = new ArrayList<Type>(List.of(enum1, type1, type2, newChoice));
			encoder.tree = new MsgTree(types, null);
			encoder.typesNo = encoder.tree.getTypes().size();
			encoder.bitsForType = encoder.calculateBitsNo(encoder.typesNo + 1); // +1 because index 0 is reserved for array
			
			ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
					// choice encoded as 111
					(byte)1, (byte)1, (byte)1,
					
					// indexes are written in 3 bits, because 4 types and array
					// enum index (1st because 0th is for array)
					(byte)0, (byte)0, (byte)1,
					
					// first type in newChoice (assuming its type2)
					(byte)0, (byte)1, (byte)1,
					// second type in newChoice (assuming its type1)
					(byte)0, (byte)1, (byte)0));
			
			
			ArrayList<Byte> output = encoder.encodeChoice(newChoice);
			assertIterableEquals(expected, output);
		}
	}

	@Nested
	class EncodingValues {
		@Test
		void testEncodeIntegerVal() {
			Encoder encoder = new Encoder();
			
			ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
					// number of types: 1
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)0, (byte)0, (byte)1,
					
					// integer liczby
					(byte)0, (byte)0, (byte)1,
					// 3 bits long
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)0, (byte)1, (byte)1,
					
					// VALUES
					// type liczby is first
					(byte)1,
						// value is 5
						(byte)1, (byte)0, (byte)1					
					));
			ArrayList<Byte> output = encoder.encode("tests/eIntVal");
			assertIterableEquals(expected, output);
		}
		
		@Test
		void testEncodeDoubleVal() {
			Encoder encoder = new Encoder();
			
			ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
					// number of types: 1
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)0, (byte)0, (byte)1,
					
					// double ulamek
					(byte)0, (byte)1, (byte)0,
					
					// VALUES
					// type ulamek is first
					(byte)1,
						// then value (0.25) in 64 bits
						(byte)0, (byte)0, (byte)1, (byte)1,
						(byte)1, (byte)1, (byte)1, (byte)1,
						
						(byte)1, (byte)1, (byte)0, (byte)1,
						(byte)0, (byte)0, (byte)0, (byte)0,
						
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)0, (byte)0, (byte)0,
						
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)0, (byte)0, (byte)0,
						
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)0, (byte)0, (byte)0,
						
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)0, (byte)0, (byte)0,
						
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)0, (byte)0, (byte)0,
						
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)0, (byte)0, (byte)0
					));
			ArrayList<Byte> output = encoder.encode("tests/eDoubleVal");
			assertIterableEquals(expected, output);
		}
		
		@Test
		void testEncodeEnumVal() {
			Encoder encoder = new Encoder();
			
			ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
					// number of types: 1
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)0, (byte)0, (byte)1,
					
					// enum typ
					(byte)0, (byte)1, (byte)1,
						// 3 enumerators
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)0, (byte)1, (byte)1,
						
						// "po"
						(byte)0, (byte)1, (byte)1, (byte)1,
						(byte)0, (byte)0, (byte)0, (byte)0,
						
						(byte)0, (byte)1, (byte)1, (byte)0,
						(byte)1, (byte)1, (byte)1, (byte)1,
						
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)0, (byte)0, (byte)0,

						// "pop"
						(byte)0, (byte)1, (byte)1, (byte)1,
						(byte)0, (byte)0, (byte)0, (byte)0,
						
						(byte)0, (byte)1, (byte)1, (byte)0,
						(byte)1, (byte)1, (byte)1, (byte)1,
						
						(byte)0, (byte)1, (byte)1, (byte)1,
						(byte)0, (byte)0, (byte)0, (byte)0,
						
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)0, (byte)0, (byte)0,
						
						// "popp"
						(byte)0, (byte)1, (byte)1, (byte)1,
						(byte)0, (byte)0, (byte)0, (byte)0,
						
						(byte)0, (byte)1, (byte)1, (byte)0,
						(byte)1, (byte)1, (byte)1, (byte)1,
						
						(byte)0, (byte)1, (byte)1, (byte)1,
						(byte)0, (byte)0, (byte)0, (byte)0,
						
						(byte)0, (byte)1, (byte)1, (byte)1,
						(byte)0, (byte)0, (byte)0, (byte)0,
						
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)0, (byte)0, (byte)0,
					
					// VALUES
					// type typ is first
					(byte)1,
						// 3rd enumerator (2nd index)
						(byte)1, (byte)0
					));
			ArrayList<Byte> output = encoder.encode("tests/eEnumVal");
			assertIterableEquals(expected, output);
		}
		
		@Test
		void testEncodeStringVal() {
			Encoder encoder = new Encoder();
			
			ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
					// number of types: 1
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)0, (byte)0, (byte)1,
					
					// string tekst
					(byte)1, (byte)0, (byte)0,
					
					// VALUES
					// type typ is first
					(byte)1,
						// value is "ASCII horror"
						(byte)0, (byte)1, (byte)0, (byte)0, 
						(byte)0, (byte)0, (byte)0, (byte)1, 
						(byte)0, (byte)1, (byte)0, (byte)1, 
						(byte)0, (byte)0, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)0, (byte)0, 
						(byte)0, (byte)0, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)0, (byte)0, 
						(byte)1, (byte)0, (byte)0, (byte)1, 
						(byte)0, (byte)1, (byte)0, (byte)0, 
						(byte)1, (byte)0, (byte)0, (byte)1, 
						(byte)0, (byte)0, (byte)1, (byte)0, 
						(byte)0, (byte)0, (byte)0, (byte)0, 
						(byte)0, (byte)1, (byte)1, (byte)0, 
						(byte)1, (byte)0, (byte)0, (byte)0, 
						(byte)0, (byte)1, (byte)1, (byte)0, 
						(byte)1, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)0, (byte)1, (byte)0, 
						(byte)0, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)0, (byte)1, (byte)0, 
						(byte)0, (byte)1, (byte)1, (byte)0, 
						(byte)1, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)0, (byte)1, (byte)0,
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)0, (byte)0, (byte)0
					));
			ArrayList<Byte> output = encoder.encode("tests/eStringVal");
			assertIterableEquals(expected, output);
		}
		
		@Test
		void testEncodeChoiceVal() {
			Encoder encoder = new Encoder();
			
			ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
					// number of types: 4
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)1, (byte)0, (byte)0,
					
					// enum typ 
					(byte)0, (byte)1, (byte)1,					// indexes 8-10
						// 2 enumerators
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)0, (byte)1, (byte)0,
						// "TYP A"
						(byte)0, (byte)1, (byte)0, (byte)1, 
						(byte)0, (byte)1, (byte)0, (byte)0, 
						(byte)0, (byte)1, (byte)0, (byte)1, 	// - 30
						(byte)1, (byte)0, (byte)0, (byte)1, 
						(byte)0, (byte)1, (byte)0, (byte)1, 
						(byte)0, (byte)0, (byte)0, (byte)0, 
						(byte)0, (byte)0, (byte)1, (byte)0, 
						(byte)0, (byte)0, (byte)0, (byte)0, 	// - 50
						(byte)0, (byte)1, (byte)0, (byte)0, 
						(byte)0, (byte)0, (byte)0, (byte)1, 
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)0, (byte)0, (byte)0,		// - 66
						// "TYP STRING"
						(byte)0, (byte)1, (byte)0, (byte)1, 	// - 70
						(byte)0, (byte)1, (byte)0, (byte)0, 
						(byte)0, (byte)1, (byte)0, (byte)1, 	
						(byte)1, (byte)0, (byte)0, (byte)1, 
						(byte)0, (byte)1, (byte)0, (byte)1, 
						(byte)0, (byte)0, (byte)0, (byte)0, 	// - 90
						(byte)0, (byte)0, (byte)1, (byte)0, 
						(byte)0, (byte)0, (byte)0, (byte)0, 
						(byte)0, (byte)1, (byte)0, (byte)1, 
						(byte)0, (byte)0, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)0, (byte)1, 	// - 110
						(byte)0, (byte)1, (byte)0, (byte)0, 
						(byte)0, (byte)1, (byte)0, (byte)1, 
						(byte)0, (byte)0, (byte)1, (byte)0, 
						(byte)0, (byte)1, (byte)0, (byte)0, 
						(byte)1, (byte)0, (byte)0, (byte)1, 	// - 130
						(byte)0, (byte)1, (byte)0, (byte)0, 
						(byte)1, (byte)1, (byte)1, (byte)0, 
						(byte)0, (byte)1, (byte)0, (byte)0, 
						(byte)0, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)0, (byte)0, (byte)0,		// - 150
						(byte)0, (byte)0, (byte)0, (byte)0,		// - 154
					// double ulamek
					(byte)0, (byte)1, (byte)0,
					// string tekst
					(byte)1, (byte)0, (byte)0,
					// choice choiceNaZewnatrz
					(byte)1, (byte)1, (byte)1,
						// used enum:
						(byte)0, (byte)0, (byte)1,
						// types pointed to:
						(byte)0, (byte)1, (byte)0,
						(byte)0, (byte)1, (byte)1,	// - 172
					
					// VALUES
					// choiceNaZewnatrz index 4
					(byte)1, (byte)0, (byte)0,	// - 175
						// used type: second (index 1)
						(byte)1,
						// value: here it is string "wystarczy"
						(byte)0, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)1, 
						(byte)1, (byte)0, (byte)0, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)0, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)0, (byte)0, 
						(byte)0, (byte)1, (byte)1, (byte)0, 
						(byte)0, (byte)0, (byte)0, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)0, (byte)1, (byte)0, 
						(byte)0, (byte)1, (byte)1, (byte)0, 
						(byte)0, (byte)0, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)1, 
						(byte)1, (byte)0, (byte)1, (byte)0, 
						(byte)0, (byte)1, (byte)1, (byte)1, 
						(byte)1, (byte)0, (byte)0, (byte)1, 
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)0, (byte)0, (byte)0
					));
			ArrayList<Byte> output = encoder.encode("tests/eChoiceVal");
			assertIterableEquals(expected, output);
		}
	
		@Test
		void testEncodeStructVal() {
			Encoder encoder = new Encoder();
			
			ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
					// number of types: 1
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)0, (byte)0, (byte)1,
					
					// struct begin
					(byte)1, (byte)0, (byte)1,
					
					// enum rodzaj 
					(byte)0, (byte)1, (byte)1,					
						// 4 enumerators
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)1, (byte)0, (byte)0,
						// "kotek"
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)0, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)1, (byte)0, (byte)1, (byte)0, (byte)0, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)0, (byte)1, (byte)0, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)0, (byte)1, (byte)1, 
						(byte)0, (byte)0, (byte)0, (byte)0,	(byte)0, (byte)0, (byte)0, (byte)0,	
						// "mis"
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)1, (byte)0, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)0, (byte)0, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)1, (byte)0, (byte)0, (byte)1, (byte)1, 
						(byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0,
						// "lalka"
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)1, (byte)0, (byte)0, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)0, (byte)0, (byte)0, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)1, (byte)0, (byte)0, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)0, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)0, (byte)0, (byte)0, (byte)1,
						(byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0,
						// "samochodziK"
						(byte)0, (byte)1, (byte)1, (byte)1, (byte)0, (byte)0, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)0, (byte)0, (byte)0, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)1, (byte)0, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)0, (byte)0, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)0, (byte)0, (byte)0, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)0, (byte)1, (byte)0, (byte)0, 
						(byte)0, (byte)1, (byte)1, (byte)1, (byte)1, (byte)0, (byte)1, (byte)0, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)0, (byte)0, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)0, (byte)1, (byte)1, 
						(byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0,
					// string firma
					(byte)1, (byte)0, (byte)0,
					// integer
					(byte)0, (byte)0, (byte)1,
						// on 8 bits
						(byte)0, (byte)0, (byte)0, (byte)0, (byte)1, (byte)0, (byte)0, (byte)0,
					// double pap
					(byte)0, (byte)1, (byte)0,
					// struct end
					(byte)1, (byte)1, (byte)0,
					
					// VALUES
					// zabawka index 1
					(byte)1,
						// enum value 2 (index 1)
						(byte)0, (byte)1,
						// string "Testowansko"
						(byte)0, (byte)1, (byte)0, (byte)1, (byte)0, (byte)1, (byte)0, (byte)0, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)0, (byte)1, (byte)0, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)1, (byte)0, (byte)0, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)1, (byte)0, (byte)1, (byte)0, (byte)0, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)1, (byte)0, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)0, (byte)0, (byte)0, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)1, (byte)1, (byte)0, 
						(byte)0, (byte)1, (byte)1, (byte)1, (byte)0, (byte)0, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)0, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0,
						// value 45 on 8 bits
						(byte)0, (byte)0, (byte)1, (byte)0, (byte)1, (byte)1, (byte)0, (byte)1,
						// 6.99 as double
						(byte)0, (byte)1, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, 
						(byte)0, (byte)0, (byte)0, (byte)1, (byte)1, (byte)0, (byte)1, (byte)1, 
						(byte)1, (byte)1, (byte)1, (byte)1, (byte)0, (byte)1, (byte)0, (byte)1, 
						(byte)1, (byte)1, (byte)0, (byte)0, (byte)0, (byte)0, (byte)1, (byte)0, 
						(byte)1, (byte)0, (byte)0, (byte)0, (byte)1, (byte)1, (byte)1, (byte)1, 
						(byte)0, (byte)1, (byte)0, (byte)1, (byte)1, (byte)1, (byte)0, (byte)0, 
						(byte)0, (byte)0, (byte)1, (byte)0, (byte)1, (byte)0, (byte)0, (byte)0, 
						(byte)1, (byte)1, (byte)1, (byte)1, (byte)0, (byte)1, (byte)1, (byte)0
					));
			ArrayList<Byte> output = encoder.encode("tests/eStructVal");
			assertIterableEquals(expected, output);
		}

		@Test
		void testEncodeArrayValSimple() {
			Encoder encoder = new Encoder();
			
			ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
					// number of types: 1
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)0, (byte)0, (byte)1,
					
					// int liczby
					(byte)0, (byte)0, (byte)1,
						// on 4 bits
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)1, (byte)0, (byte)0,
					
					// VALUES
					// type ARRAY
					(byte)0,
					// 5 elements
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)1, (byte)0, (byte)1,
					// type liczby is first
					(byte)1,
						// then values {1, 5, 7, 3, 2} in 4 bits
						(byte)0, (byte)0, (byte)0, (byte)1,
						
						(byte)0, (byte)1, (byte)0, (byte)1,
						
						(byte)0, (byte)1, (byte)1, (byte)1,
						
						(byte)0, (byte)0, (byte)1, (byte)1,
						
						(byte)0, (byte)0, (byte)1, (byte)0
					));
			ArrayList<Byte> output = encoder.encode("tests/eArrayValSimple");
			assertIterableEquals(expected, output);
		}
		
		@Test
		void testEncodeArrayValArithmetics() {
			Encoder encoder = new Encoder();
			
			ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
					// number of types: 2
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)0, (byte)1, (byte)0,
					
					// int liczby
					(byte)0, (byte)0, (byte)1,
						// on 4 bits
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)1, (byte)0, (byte)0,
					
					// struct begin
					(byte)1, (byte)0, (byte)1,
						// int sztuk
						(byte)0, (byte)0, (byte)1,
							// on 8 bits
							(byte)0, (byte)0, (byte)0, (byte)0,
							(byte)1, (byte)0, (byte)0, (byte)0,
					// struct end
					(byte)1, (byte)1, (byte)0,
						
					// VALUES
					// type pap is second
					(byte)1, (byte)0,
						// sztuk is 8
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)1, (byte)0, (byte)0, (byte)0,
					// type liczby is first
					(byte)0, (byte)1,
						// then value 3 in 4 bits
						(byte)0, (byte)0, (byte)1, (byte)1,
					// type ARRAY
					(byte)0, (byte)0,
					// 5 elements
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)1, (byte)0, (byte)1,
					// type liczby is first
					(byte)0, (byte)1,
						// then values {1, 5, 7, 3, 2} in 4 bits
						(byte)0, (byte)0, (byte)0, (byte)1,
						
						(byte)0, (byte)1, (byte)0, (byte)1,
						
						(byte)0, (byte)1, (byte)1, (byte)1,
						
						(byte)0, (byte)0, (byte)1, (byte)1,
						
						(byte)0, (byte)0, (byte)1, (byte)0
					));
			ArrayList<Byte> output = encoder.encode("tests/eArrayValArithmetics");
			assertIterableEquals(expected, output);
		}
		
		@Test
		void testEncodeArrayValMultiply() {
			Encoder encoder = new Encoder();
			
			ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
					// number of types: 2
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)0, (byte)1, (byte)0,
					
					// int liczby
					(byte)0, (byte)0, (byte)1,
						// on 4 bits
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)1, (byte)0, (byte)0,
					
					// struct begin
					(byte)1, (byte)0, (byte)1,
						// int sztuk
						(byte)0, (byte)0, (byte)1,
							// on 8 bits
							(byte)0, (byte)0, (byte)0, (byte)0,
							(byte)1, (byte)0, (byte)0, (byte)0,
					// struct end
					(byte)1, (byte)1, (byte)0,
						
					// VALUES
					// type pap is second
					(byte)1, (byte)0,
						// sztuk is 8
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)1, (byte)0, (byte)0, (byte)0,
					// type liczby is first
					(byte)0, (byte)1,
						// then value 2 in 4 bits
						(byte)0, (byte)0, (byte)1, (byte)0,
					// type ARRAY
					(byte)0, (byte)0,
					// 4 elements
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)1, (byte)0, (byte)0,
					// type liczby is first
					(byte)0, (byte)1,
						// then values {1, 5, 7, 3, 2} in 4 bits
						(byte)0, (byte)0, (byte)0, (byte)1,
						
						(byte)0, (byte)1, (byte)0, (byte)1,
						
						(byte)0, (byte)1, (byte)1, (byte)1,
						
						(byte)0, (byte)0, (byte)1, (byte)1
					));
			ArrayList<Byte> output = encoder.encode("tests/eArrayValMultiply");
			assertIterableEquals(expected, output);
		}
		
		@Test
		void testEncodeArrayValDivide() {
			Encoder encoder = new Encoder();
			
			ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
					// number of types: 2
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)0, (byte)1, (byte)0,
					
					// int liczby
					(byte)0, (byte)0, (byte)1,
						// on 4 bits
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)1, (byte)0, (byte)0,
					
					// struct begin
					(byte)1, (byte)0, (byte)1,
						// int sztuk
						(byte)0, (byte)0, (byte)1,
							// on 8 bits
							(byte)0, (byte)0, (byte)0, (byte)0,
							(byte)1, (byte)0, (byte)0, (byte)0,
					// struct end
					(byte)1, (byte)1, (byte)0,
						
					// VALUES
					// type pap is second
					(byte)1, (byte)0,
						// sztuk is 2
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)0, (byte)1, (byte)0,
					// type liczby is first
					(byte)0, (byte)1,
						// then value 2 in 4 bits
						(byte)0, (byte)0, (byte)1, (byte)0,
					// type ARRAY
					(byte)0, (byte)0,
					// 4 elements
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)1, (byte)0, (byte)0,
					// type liczby is first
					(byte)0, (byte)1,
						// then values {1, 5, 7, 3, 2} in 4 bits
						(byte)0, (byte)0, (byte)0, (byte)1,
						
						(byte)0, (byte)1, (byte)0, (byte)1,
						
						(byte)0, (byte)1, (byte)1, (byte)1,
						
						(byte)0, (byte)0, (byte)1, (byte)1
					));
			ArrayList<Byte> output = encoder.encode("tests/eArrayValDivide");
			assertIterableEquals(expected, output);
		}
		
		@Test
		void testEncodeArrayValNestedExpression() {
			Encoder encoder = new Encoder();
			
			ArrayList<Byte> expected = new ArrayList<Byte>(List.of(
					// number of types: 2
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)0, (byte)1, (byte)0,
					
					// int liczby
					(byte)0, (byte)0, (byte)1,
						// on 4 bits
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)1, (byte)0, (byte)0,
					
					// struct begin
					(byte)1, (byte)0, (byte)1,
						// int sztuk
						(byte)0, (byte)0, (byte)1,
							// on 8 bits
							(byte)0, (byte)0, (byte)0, (byte)0,
							(byte)1, (byte)0, (byte)0, (byte)0,
					// struct end
					(byte)1, (byte)1, (byte)0,
						
					// VALUES
					// type pap is second
					(byte)1, (byte)0,
						// sztuk is 1
						(byte)0, (byte)0, (byte)0, (byte)0,
						(byte)0, (byte)0, (byte)0, (byte)1,
					// type liczby is first
					(byte)0, (byte)1,
						// then value 1 in 4 bits
						(byte)0, (byte)0, (byte)0, (byte)1,
					// type ARRAY
					(byte)0, (byte)0,
					// 4 elements
					(byte)0, (byte)0, (byte)0, (byte)0,
					(byte)0, (byte)1, (byte)0, (byte)0,
					// type liczby is first
					(byte)0, (byte)1,
						// then values {1, 5, 7, 3, 2} in 4 bits
						(byte)0, (byte)0, (byte)0, (byte)1,
						
						(byte)0, (byte)1, (byte)0, (byte)1,
						
						(byte)0, (byte)1, (byte)1, (byte)1,
						
						(byte)0, (byte)0, (byte)1, (byte)1
					));
			ArrayList<Byte> output = encoder.encode("tests/eArrayValNestedExpression");
			assertIterableEquals(expected, output);
		}
	}
	
	

}
