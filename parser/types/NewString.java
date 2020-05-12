package parser.types;

public class NewString extends Type {
	
	public NewString(String identifier) {
		super(Types.STRING, identifier);
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
