package frontend.parser.terminal;

import frontend.lexer.Token;

public class Ident {
    private final Token token;

    public Ident(Token token) {
        this.token = token;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(token.toString());
        return sb.toString();
    }
}
