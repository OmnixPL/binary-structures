package parser.values;

import java.util.ArrayList;

import parser.types.Type;
import parser.types.Types;

public class ArrayVal extends Value {
	Type type;
	ArrayList<Value> values;

	public ArrayVal(Type type, String identifier, ArrayList<Value> values) {
		super(identifier, Types.ARRAY);
		this.type = type;
		this.values = values;
	}
	
	public String toString(int indents) {
		StringBuilder string = new StringBuilder();
		StringBuilder indent = new StringBuilder();
		
		for (int i = 0; i < indents; i++)
			indent.append(" ");
		
		string.append(indent + "⌞" + type.getIdentifier() + " " + identifier + ": "  +  "\n");
		
		for (int i = 0; i < values.size(); i++)
			string.append("[" + i + "]" + values.get(i).toString(indents + 2));
		
		return string.toString();
	}
}
