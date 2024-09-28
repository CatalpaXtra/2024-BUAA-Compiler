package frontend.lexer;

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
    private final int line;

    public Token(Type type, String content, int line) {
        this.type = type;
        this.content = content;
        this.line = line;
    }

    public String toString() {
        return type + " " + content;
    }
}
