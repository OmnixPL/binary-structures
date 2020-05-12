package parser.values;

import java.util.ArrayList;

import parser.types.NewStruct;
import parser.types.Types;

public class StructVal extends Value {
	NewStruct type;
	ArrayList<Value> values;

	public StructVal(NewStruct type, String identifier, ArrayList<Value> values) {
		super(identifier, Types.STRUCT);
		this.type = type;
		this.values = values;
	}
	
	public String toString(int indents) {
		StringBuilder string = new StringBuilder();
		StringBuilder indent = new StringBuilder();
		
		for (int i = 0; i < indents; i++)
			indent.append(" ");
		
		string.append(indent + "⌞" + type.getIdentifier() + " " + getIdentifier() + ": "  +  "\n");
		
		for (Value value: values)
			string.append(value.toString(indents + 2));
		
		return string.toString();
	}
}
