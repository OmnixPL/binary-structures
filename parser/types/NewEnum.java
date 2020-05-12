package parser.types;

import java.util.ArrayList;

public class NewEnum extends Type {
	private ArrayList<String> values;
	
	public NewEnum(String identifier, ArrayList<String> values) {
		super(Types.ENUM, identifier);
		this.values = values;
	}

	public ArrayList<String> getValues() {
		return values;
	}
	
	public String toString(int indents) {
		StringBuilder string = new StringBuilder();
		StringBuilder indent = new StringBuilder();
		
		for (int i = 0; i < indents; i++)
			indent.append(" ");
		
		string.append(indent + "⌞" + getType() + ": " + getIdentifier() +  "\n");
		
		indent.append(" ");
		indent.append(" ");
		
		for (String value : values)
			string.append(indent + "⌞" + value + "\n");
		
		return string.toString();
	}
}
