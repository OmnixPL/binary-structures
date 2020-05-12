package reader;
import java.io.BufferedReader;
import java.io.IOException;

public class CodeReader {
	private BufferedReader reader;
	private char current;
	private Pos pos;
	
	public CodeReader(BufferedReader reader) throws IOException {
		this.reader = reader;
		current = (char) reader.read();
		pos = new Pos(1, 1);
	}
	
	public char peek() {
		return current;
	}

	public void consume() throws IOException {
		current = (char) reader.read();
		pos.column++;
		
		if(current == 10) {
			pos.line++;
			pos.column = 0;		// reset this to 0, not 1, because it is actually still previous line, NEXT char will have column 1 in new line.
								// but then, you can't really tell that new line has started, so you must set it now.
								// can't even consume this newline here and pretend it never happened because strings need them
		}
	}
	
	public Pos getPos() {
		return pos;
	}
}
