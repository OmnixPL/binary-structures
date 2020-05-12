package parser.arithmetics;

import java.util.ArrayList;

public class Term {

	ArrayList<Factor> factors;
	ArrayList<Character> operators;
	
	public Term(ArrayList<Factor> factors, ArrayList<Character> operators) {
		this.factors = factors;		
		this.operators = operators;
	}
	
	public String toString(int indents) {
		StringBuilder string = new StringBuilder();
		StringBuilder indent = new StringBuilder();
		
		for (int i = 0; i < indents; i++)
			indent.append(" ");
		
		int i;
		for(i = 0; i < factors.size() - 1; i++) {
			string.append(indent + "⌞\n" + factors.get(i).toString(indents + 2));
			string.append(indent + "⌞" + operators.get(i) + "\n");
		}

		string.append(indent + "⌞\n" + factors.get(i).toString(indents + 2));

		return string.toString();
	}
}
