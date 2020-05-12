package parser.types;

public class Type {
	private Types typeName;	// struct, integer etc
	private String identifier;	// kot, pies, ilosc
	
	public Type(Types typeName, String identifier) {
		this.typeName = typeName;
		this.identifier = identifier;
	}

	public Types getType() {
		return typeName;
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public String toString(int indents) {
		return super.toString();
	}
}
