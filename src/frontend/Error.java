package frontend;

public class Error {
    public enum Type {
        a,  i, j, k,
    }

    private final Error.Type type;
    private final String content;
    private final int line;

    public Error(Error.Type type, String content, int line) {
        this.type = type;
        this.content = content;
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public String toString() {
        return line + " " + type + "\n";
    }
}
