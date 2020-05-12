package parser.values;

import parser.types.NewString;
import parser.types.Types;

public class StringVal extends Value {
	NewString type;
	String value;
	
	public StringVal(NewString type, String identifier, String value) {
		super(identifier, Types.STRING);
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
