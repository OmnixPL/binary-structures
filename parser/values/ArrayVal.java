package parser.values;

import java.util.ArrayList;

import parser.arithmetics.Expression;
import parser.types.Type;
import parser.types.Types;

public class ArrayVal extends Value {
	Type type;
	ArrayList<Value> values;
	Expression elementsNo;
	
	public ArrayVal(Type type, String identifier, ArrayList<Value> values, Expression elementsNo) {
		super(identifier, Types.ARRAY);
		this.type = type;
		this.values = values;
		this.elementsNo = elementsNo;
	}
	
	public String toString(int indents) {
		StringBuilder string = new StringBuilder();
		StringBuilder indent = new StringBuilder();
		
		for (int i = 0; i < indents; i++)
			indent.append(" ");
		
		string.append(indent + "⌞" + type.getIdentifier() + " " + getIdentifier() + ": "  +  "\n");
		
		indent.append(" ");
		indent.append(" ");
		
		string.append(indent + "expression:\n" + elementsNo.toString(indents + 2));
		
		for (int i = 0; i < values.size(); i++)
			string.append(indent + "[" + i + "]" + values.get(i).toString(0));
		
		return string.toString();
	}
}
