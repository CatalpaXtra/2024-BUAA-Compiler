package frontend.parser.terminal;

import frontend.lexer.Token;

public class IntConst {
    private Token token;

    public IntConst(Token token) {
        this.token = token;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(token.toString());
        return sb.toString();
    }
}
