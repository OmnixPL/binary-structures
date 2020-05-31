package parser.types;

import java.util.ArrayList;

public class NewChoice extends Type {
	NewEnum choosingEnum;
	ArrayList<String> values;
	
	public NewChoice(String identifier, NewEnum choosingEnum, ArrayList<String> values) {
		super(Types.CHOICE, identifier);
		this.values = values;
		this.choosingEnum = choosingEnum;
	}

	public String toString(int indents) {
		StringBuilder string = new StringBuilder();
		StringBuilder indent = new StringBuilder();
		
		for (int i = 0; i < indents; i++)
			indent.append(" ");
		
		string.append(indent + "⌞" + getType() + ": " + getIdentifier() + " chosen by: " + choosingEnum.getIdentifier() +  "\n");
		
		indent.append(" ");
		indent.append(" ");
		
		for (String value : values)
			string.append(indent + "⌞" + value + "\n");
		
		return string.toString();
	}

	public NewEnum getChoosingEnum() {
		return choosingEnum;
	}

	public ArrayList<String> getValues() {
		return values;
	}
	
	
}
