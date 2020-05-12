package parser.values;

import parser.types.NewEnum;
import parser.types.Types;

public class EnumVal extends Value {
	NewEnum type;
	String value;

	public EnumVal(NewEnum type, String identifier, String value) {
		super(identifier, Types.ENUM);
		this.type = type;
		this.value = value;
	}
	
	public String toString(int indents) {
		StringBuilder string = new StringBuilder();
		StringBuilder indent = new StringBuilder();
		
		for (int i = 0; i < indents; i++)
			indent.append(" ");
		
		string.append(indent + "⌞" + type.getIdentifier() + " " + identifier + ": " + value +  "\n");
		return string.toString();
	}
}
