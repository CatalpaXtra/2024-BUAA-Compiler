package frontend.parser.declaration;

import frontend.lexer.Token;

public class BType {
    private final Token token;

    public BType(Token token) {
        this.token = token;
    }

    public String identifyType() {
        if (token.getType().equals(Token.Type.INTTK)) {
            return "Int";
        }
        return "Char";
    }

    public Token getToken() {
        return token;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(token.toString());
        return sb.toString();
    }
}
