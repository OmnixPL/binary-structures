package parser.arithmetics;

import java.util.ArrayList;

import parser.MsgTree;

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
	
	public double evaluate(MsgTree tree) throws ArrayArithmeticException {
		double left = factors.get(0).evaluate(tree);
		double right = 0;
		for (int i = 1; i < factors.size(); i++) {
			right = factors.get(i).evaluate(tree);
			if (operators.get(i - 1).equals('*'))
				left *= right;
			else if (operators.get(i - 1).equals('/')) {
				if (right == 0)
					throw new ArrayArithmeticException("Trying to divide when right factor equals to zero");
				left /= right;
			}
		}
		return left;
	}
}
