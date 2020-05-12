package parser.values;

import parser.types.Types;

public class Value {
	private Types typeName;	// struct, integer etc
	private String identifier;
	
	public Value(String identifier, Types typeName) {
		this.identifier = identifier;
		this.typeName = typeName;
	}
	
	public String toString(int indents) {
		return super.toString();
	}

	public String getIdentifier() {
		return identifier;
	}

	public Types getTypeName() {
		return typeName;
	}
	
	
}
