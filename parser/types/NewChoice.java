package parser.types;

import java.util.ArrayList;

public class NewChoice extends Type {
	NewEnum choice;
	ArrayList<String> values;
	
	public NewChoice(String identifier, NewEnum choosingEnum, ArrayList<String> values) {
		super(Types.CHOICE, identifier);
		this.values = values;
		this.choice = choosingEnum;
	}

	public String toString(int indents) {
		StringBuilder string = new StringBuilder();
		StringBuilder indent = new StringBuilder();
		
		for (int i = 0; i < indents; i++)
			indent.append(" ");
		
		string.append(indent + "⌞" + getType() + ": " + getIdentifier() + " chosen by: " + choice +  "\n");
		
		indent.append(" ");
		indent.append(" ");
		
		for (String value : values)
			string.append(indent + "⌞" + value + "\n");
		
		return string.toString();
	}

	public NewEnum getChoice() {
		return choice;
	}

	public ArrayList<String> getValues() {
		return values;
	}
	
	
}
