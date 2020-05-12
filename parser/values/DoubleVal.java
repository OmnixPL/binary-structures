package parser.values;

import parser.types.NewDouble;
import parser.types.Types;

public class DoubleVal extends Value {
	NewDouble type;
	Double value;

	public DoubleVal(NewDouble type, String identifier, Double value) {
		super(identifier, Types.DOUBLE);
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
