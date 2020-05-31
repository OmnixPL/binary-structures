package parser.values;

import parser.types.NewChoice;
import parser.types.Type;
import parser.types.Types;

public class ChoiceVal extends Value {
	Type chosenType;	// this is the type that was chosen by string in code
	Value value;		// this is value assigned to chosenType

	public ChoiceVal(NewChoice type, String identifier, Type chosenType, Value value) {
		super(identifier, Types.CHOICE);
		this.type = type;	// this is NewChoice, declaration of this value
		this.chosenType = chosenType;
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

	public Type getChosenType() {
		return chosenType;
	}

	public Value getValue() {
		return value;
	}
}
