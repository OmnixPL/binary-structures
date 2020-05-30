package parser.values;

import parser.types.NewChoice;
import parser.types.Type;
import parser.types.Types;

public class ChoiceVal extends Value {
	Type choice;
	Value value;

	public ChoiceVal(NewChoice type, String identifier, Type choice, Value value) {
		super(identifier, Types.CHOICE);
		this.type = type;
		this.choice = choice;
		this.value = value;
	}
	
	public String toString(int indents) {
		StringBuilder string = new StringBuilder();
		StringBuilder indent = new StringBuilder();
		
		for (int i = 0; i < indents; i++)
			indent.append(" ");
		
		string.append(indent + "⌞" + type.getIdentifier() + " " + getIdentifier() + ": "  +  "\n");
		
		string.append(value.toString(indents + 2));
		
		return string.toString();
	}

	public Type getChoice() {
		return choice;
	}

	public Value getValue() {
		return value;
	}
}
