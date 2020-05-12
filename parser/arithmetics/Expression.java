package parser.arithmetics;

import java.util.ArrayList;

public class Expression {
	ArrayList<Term> terms;
	ArrayList<Character> operators;
	
	public Expression(ArrayList<Term> terms, ArrayList<Character> operators) {
		this.terms = terms;		
		this.operators = operators;
	}

}
