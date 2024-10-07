package frontend.lexer;

import java.util.ArrayList;

public class TokenIterator {
    private final ArrayList<Token> tokens;
    private int loc;
    private final int len;

    public TokenIterator(ArrayList<Token> tokens) {
        this.tokens = tokens;
        loc = 0;
        len = tokens.size();
    }

    public boolean hasNext() {
        return loc < len - 1;
    }

    public Token getNextToken() {
        return tokens.get(loc++);
    }

    public void traceBack(int i) {
        loc -= i;
    }
}
