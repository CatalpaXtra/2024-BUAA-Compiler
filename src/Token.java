public class Token {
    public enum Type {
        IDENFR, INTCON, STRCON, CHRCON,
        MAINTK, CONSTTK, BREAKTK, CONTINUETK, IFTK, ELSETK, FORTK, GETINTTK, GETCHARTK, PRINTFTK, RETURNTK,
        INTTK, CHARTK, VOIDTK,
        NOT, AND, OR, ERRA,
        PLUS, MINU, MULT, DIV, MOD, LSS, LEQ, GRE, GEQ, EQL, NEQ, ASSIGN,
        SEMICN, COMMA, LPARENT, RPARENT, LBRACK, RBRACK, LBRACE, RBRACE,
    }

    private final Type type;
    private final String content;

    public Token(Type type, String content) {
        this.type = type;
        this.content = content;
    }

    public Type getType() {
        return this.type;
    }

    public void printToken() {
        switch (type) {
            case ERRA:
                System.out.println(content + " a");
                break;
            default:
                System.out.println(type + " " + content);
                break;
        }
    }
}
