package parser.types;

public class NewInteger extends Type {
	int bits;
	
	public NewInteger(String identifier, int bits) {
		super(Types.INTEGER, identifier);
		this.bits = bits;
	}
	
	public String toString(int indents) {
		StringBuilder string = new StringBuilder();
		StringBuilder indent = new StringBuilder();
		
		for (int i = 0; i < indents; i++)
			indent.append(" ");
		
		string.append(indent + "⌞" + getType() + bits + ": " + getIdentifier() +  "\n");
		
		return string.toString();
	}
}
