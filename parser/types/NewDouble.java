package parser.types;

public class NewDouble extends Type {
	
	public NewDouble(String identifier) {
		super(Types.DOUBLE, identifier);
	}
	
	public String toString(int indents) {
		StringBuilder string = new StringBuilder();
		StringBuilder indent = new StringBuilder();
		
		for (int i = 0; i < indents; i++)
			indent.append(" ");
		
		string.append(indent + "⌞" + getType() + ": " + getIdentifier() +  "\n");
		
		return string.toString();
	}
}
