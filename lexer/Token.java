package lexer;

import reader.Pos;

public class Token {
	private Tokens tokenName;
	private String msg;
	private Pos pos;
	
	public Token(Tokens tokenName, Pos pos, String msg) {
		this.tokenName = tokenName;
		this.msg = msg;
		this.pos = pos;
	}
	
	public Tokens getTokenName() {
		return tokenName;
	}

	public String getMsg() {
		return msg;
	}
	
	public Pos getPos() {
		return pos;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Token token = (Token) o;
        if (tokenName != token.tokenName)
        	return false;
        if (!pos.equals(token.pos))
        	return false;
        if (msg == null && token.msg == null)
        	return true;
        if (msg != null && msg.equals(token.msg))
        	return true;
        return false;
    }
}
