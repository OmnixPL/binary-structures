package parser.arithmetics;

import java.util.ArrayList;

public class Expression {
	ArrayList<Term> terms;
	ArrayList<Character> operators;
	
	public Expression(ArrayList<Term> terms, ArrayList<Character> operators) {
		this.terms = terms;		
		this.operators = operators;
	}

	public String toString(int indents) {
		StringBuilder string = new StringBuilder();
		StringBuilder indent = new StringBuilder();
		
		for (int i = 0; i < indents; i++)
			indent.append(" ");
		
		int i;
		for(i = 0; i < terms.size() - 1; i++) {
			string.append(indent + "⌞\n" + terms.get(i).toString(indents + 2));
			string.append(indent + "⌞" + operators.get(i) + "\n");
		}

		string.append(indent + "⌞\n" + terms.get(i).toString(indents + 2));

		return string.toString();
	}
}
