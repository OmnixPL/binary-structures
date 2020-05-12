package parser.types;

import java.util.ArrayList;

public class NewStruct extends Type {
	ArrayList<Type> members;
	
	public NewStruct(String identifier, ArrayList<Type> members) {
		super(Types.STRUCT, identifier);
		this.members = members;
	}
	
	public String toString(int indents) {
		StringBuilder string = new StringBuilder();
		StringBuilder indent = new StringBuilder();
		
		for (int i = 0; i < indents; i++)
			indent.append(" ");
		
		string.append(indent + "⌞" + getType() + ": " + getIdentifier() +  "\n");
		
		for (Type member : members)
			string.append(member.toString(indents + 2));
		
		return string.toString();
	}

	public ArrayList<Type> getMembers() {
		return members;
	}
}
