package parser.arithmetics;

import java.util.ArrayList;

public class Term {

	ArrayList<Factor> factors;
	ArrayList<Character> operators;
	
	public Term(ArrayList<Factor> factors, ArrayList<Character> operators) {
		this.factors = factors;		
		this.operators = operators;
	}
}
