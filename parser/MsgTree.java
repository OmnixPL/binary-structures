package parser;
import java.util.ArrayList;

import parser.types.Type;
import parser.values.Value;

public class MsgTree {
	ArrayList<Type> types;
	ArrayList<Value> values;
	
	public MsgTree() {
		types = new ArrayList<Type>();
		values = new ArrayList<Value>();
	}

	public void printTree() {
		System.out.println("------------- TYPES");
		for (Type type : types)
			System.out.println(type.toString(0));
		

		System.out.println("------------- VALUES");
		for (Value value: values)
			System.out.println(value.toString(0));
	}
}
