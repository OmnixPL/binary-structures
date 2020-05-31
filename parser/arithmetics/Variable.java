package parser.arithmetics;

import java.util.ArrayList;

import parser.MsgTree;
import parser.types.NewStruct;
import parser.types.Types;
import parser.values.ChoiceVal;
import parser.values.IntegerVal;
import parser.values.StructVal;
import parser.values.Value;

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
	
	public int evaluate(MsgTree tree) throws ArrayArithmeticException {
		// first step: find Value that has same name as first identifier
		int i;
		for (i = 0; i < tree.getValues().size(); i++) {
			if (tree.getValues().get(i).getIdentifier().equalsIgnoreCase(identifiers.get(0)))
				break;
		}
		
		if (i == tree.getValues().size())
			throw new ArrayArithmeticException("Identifier is not followable (struct, choice) or int.");
		
		// now you have it. it can be integer (lovely), struct or choice
		// if its integer, then return it
		// if its struct, then you must:
		// 1. find this struct in types
		// 2. find index of value with second identifier
		// 3. find value in this struct
		// if value found is integer, then return it, if its struct then loop
		// --- if its choice
		// 1. getChosenType
		// 2. if its int - great
		// 3. if its struct - find index of value with third identifier
		
		if (tree.getValues().get(i).getTypeName() == Types.INTEGER) {
			IntegerVal integerVal = (IntegerVal)tree.getValues().get(i);
			return integerVal.getValue();			
		}
		else if (tree.getValues().get(i).getTypeName() == Types.STRUCT) {
			return evaluateStruct(tree.getValues().get(i), 1);
		}
		else if (tree.getValues().get(i).getTypeName() == Types.CHOICE) {
			return evaluateChoice(tree.getValues().get(i), 1);			
		}
		else 
			throw new ArrayArithmeticException("Identifier is not followable (struct, choice) or int.");
	}
	
	private int evaluateStruct(Value superType, int identifiersIndex) throws ArrayArithmeticException {
		StructVal structVal = (StructVal)superType;
		NewStruct newStruct = (NewStruct)structVal.getType();
		int j;
		for (j = 0; j < newStruct.getMembers().size(); j++) {
			if (newStruct.getMembers().get(j).getIdentifier().equalsIgnoreCase(identifiers.get(identifiersIndex)))
				break;
		}
		
		if (j == newStruct.getMembers().size())
			throw new ArrayArithmeticException("Identifier is not followable (struct, choice) or int.");
		
		if (newStruct.getMembers().get(j).getType() == Types.INTEGER) {
			IntegerVal integerVal = (IntegerVal)structVal.getValues().get(j);
			return integerVal.getValue();			
		}
		else if (newStruct.getMembers().get(j).getType() == Types.STRUCT) {
			return evaluateStruct(structVal, identifiersIndex + 1);
		}
		else if (newStruct.getMembers().get(j).getType() == Types.CHOICE)
			return evaluateChoice(structVal, identifiersIndex + 1);
		else 
			throw new ArrayArithmeticException("Identifier is not followable (struct, choice) or int.");
	}
	
	private int evaluateChoice(Value superType, int identifiersIndex) throws ArrayArithmeticException {
		ChoiceVal choiceVal = (ChoiceVal)superType;
		if (choiceVal.getChosenType().getType() == Types.INTEGER) {
			IntegerVal integerVal = (IntegerVal)choiceVal.getValue();
			return integerVal.getValue();
		}
		else if (choiceVal.getChosenType().getType() == Types.STRUCT) {
			return evaluateStruct(choiceVal.getValue(), identifiersIndex + 1);
		}
		else if (choiceVal.getChosenType().getType() == Types.CHOICE)
			return evaluateChoice(choiceVal.getValue(), identifiersIndex + 1);
		else 
			throw new ArrayArithmeticException("Identifier is not followable (struct, choice) or int.");	
	}
}
