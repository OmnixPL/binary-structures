package reader;

public class Pos {
	int line;
	int column;
	
	public Pos(int line, int column) {
		this.line = line;
		this.column = column;
	}
	
	public Pos(Pos pos) {
		this.line = pos.line;
		this.column = pos.column;
	}
	
	public int getLine() {
		return line;
	}
	public int getColumn() {
		return column;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Pos pos = (Pos) o;
		if (line != pos.line)
			return false;
		if (column != pos.column)
			return false;
		return true;
	}
}
