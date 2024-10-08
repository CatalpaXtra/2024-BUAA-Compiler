package frontend.parser.function;

import frontend.lexer.Token;

public class FuncType {
    private final String name = "<FuncType>";
    private final Token token;

    public FuncType(Token token) {
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
