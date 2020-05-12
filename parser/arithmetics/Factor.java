package parser.arithmetics;

public class Factor {
	Factors which;
	// TODO this may be faulty if bits are bigger than 32
	Integer number;
	Variable variable;
	Expression expression;
		
	public Factor(Integer number) {
		which = Factors.NUMBER;
		this.number = number;
		variable = null;
		expression = null;
	}
	
	public Factor(Variable variable) {
		which = Factors.VARIABLE;
		number = null;
		this.variable = variable;
		expression = null;
	}
	
	public Factor(Expression expression) {
		which = Factors.EXPRESSION;
		number = null;
		variable = null;
		this.expression = expression;
	}
}
