package frontend.parser.function;

import frontend.lexer.Token;

public class FuncType {
    private final String name = "<FuncType>";
    private final Token token;

    public FuncType(Token token) {
        this.token = token;
    }

    public String identifyFuncType() {
        if (token.getType().equals(Token.Type.INTTK)) {
            return "Int";
        } else if (token.getType().equals(Token.Type.CHARTK)) {
            return "Char";
        }
        return "Void";
    }

    public Token getToken() {
        return token;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(token.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
