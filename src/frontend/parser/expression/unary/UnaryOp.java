package frontend.parser.expression.unary;

import frontend.lexer.Token;

public class UnaryOp {
    private final String name = "<UnaryOp>";
    private final Token token;

    public UnaryOp(Token token) {
        this.token = token;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(token.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
