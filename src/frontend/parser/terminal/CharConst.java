package frontend.parser.terminal;

import frontend.lexer.Token;

public class CharConst {
    private final Token token;

    public CharConst(Token token) {
        this.token = token;
    }

    public int getVal() {
        String ch = token.getContent().substring(1, token.getContent().length() - 1);
        if (ch.length() == 1) {
            return ch.charAt(0);
        }
        switch (ch){
            case "\\a":
                return 7;
            case "\\b":
                return 8;
            case "\\t":
                return 9;
            case "\\n":
                return 10;
            case "\\v":
                return 11;
            case "\\f":
                return 12;
            case "\\\"":
                return 34;
            case "\\'":
                return 39;
            case "\\\\":
                return 92;
            case "\\0":
                return 0;
        }
        return ch.charAt(1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(token.toString());
        return sb.toString();
    }
}
