package parser.arithmetics;

import java.util.ArrayList;

public class Variable {
	ArrayList<String> identifiers;
	
	public Variable(ArrayList<String> identifiers) {
		this.identifiers = identifiers;
	}
	
	public String toString(int indents) {
		StringBuilder string = new StringBuilder();
		StringBuilder indent = new StringBuilder();
		
		for (int i = 0; i < indents; i++)
			indent.append(" ");
		
		string.append(indent + "⌞");
				
		int i;
		for(i = 0; i < identifiers.size() - 1; i++) {
			string.append(identifiers.get(i) + ".");
		}
		string.append(identifiers.get(i) + "\n");

		return string.toString();
	}
}
