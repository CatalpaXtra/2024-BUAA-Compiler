package frontend.parser.terminal;

import frontend.lexer.Token;

public class IntConst {
    private final Token token;

    public IntConst(Token token) {
        this.token = token;
    }

    public int getVal() {
        return Integer.parseInt(token.getContent());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(token.toString());
        return sb.toString();
    }
}
