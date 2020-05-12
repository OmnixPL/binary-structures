package parser;
import java.util.ArrayList;

import parser.types.Type;
import parser.values.Value;

public class MsgTree {
	ArrayList<Type> types;
	ArrayList<Value> values;
	
	public MsgTree() {
		types = new ArrayList<Type>();
		values = new ArrayList<Value>();
	}

}
