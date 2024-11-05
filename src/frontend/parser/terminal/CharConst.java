package frontend.parser.terminal;

import frontend.lexer.Token;

public class CharConst {
    private final Token token;

    public CharConst(Token token) {
        this.token = token;
    }

    public int getVal() {
        if (token.getContent().length() == 3) {
            return token.getContent().charAt(1);
        } else {
            return token.getContent().charAt(2);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(token.toString());
        return sb.toString();
    }
}
