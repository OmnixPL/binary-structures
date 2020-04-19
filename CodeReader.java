import java.io.IOException;

public interface CodeReader {
	public char peek();
	public void consume() throws IOException;
	public Pos getPos();
}
