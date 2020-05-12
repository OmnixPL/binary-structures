package parser.values;

import parser.types.NewInteger;
import parser.types.Types;

public class IntegerVal extends Value {
	NewInteger type;
	Integer value;

	public IntegerVal(NewInteger type, String identifier, Integer value) {
		super(identifier, Types.INTEGER);
		this.type = type;
		this.value = value;
	}
	
	public String toString(int indents) {
		StringBuilder string = new StringBuilder();
		StringBuilder indent = new StringBuilder();
		
		for (int i = 0; i < indents; i++)
			indent.append(" ");
		
		string.append(indent + "⌞" + type.getIdentifier() + " " + getIdentifier() + ": " + value +  "\n");
		return string.toString();
	}
}
