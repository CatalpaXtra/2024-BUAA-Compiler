package frontend.parser.terminal;

import frontend.lexer.Token;

public class CharConst {
    private Token token;

    public CharConst(Token token) {
        this.token = token;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(token.toString());
        return sb.toString();
    }
}
