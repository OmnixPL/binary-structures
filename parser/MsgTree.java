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
	
	public MsgTree(ArrayList<Type> types, ArrayList<Value> values) {
		this.types = types;
		this.values = values;
	}

	public void printTree() {
		System.out.println("------------- TYPES");
		for (Type type : types)
			System.out.println(type.toString(0));
		

		System.out.println("------------- VALUES");
		for (Value value: values)
			System.out.println(value.toString(0));
	}

	public ArrayList<Type> getTypes() {
		return types;
	}

	public ArrayList<Value> getValues() {
		return values;
	}
}
