package frontend.parser.expression.primary;

import frontend.lexer.Token;
import frontend.parser.expression.Exp;
import frontend.parser.terminal.Ident;

public class LVal implements PrimaryEle {
    private final String name = "<LVal>";
    private final Ident ident;
    private final Token lBracket;
    private final Exp exp;
    private final Token rBracket;

    public LVal(Ident ident, Token lBracket, Exp exp, Token rBracket) {
        this.ident = ident;
        this.lBracket = lBracket;
        this.exp = exp;
        this.rBracket = rBracket;
    }

    public Ident getIdent() {
        return ident;
    }

    public Exp getExp() {
        return exp;
    }

    public boolean isArray() {
        return lBracket != null && exp != null && rBracket != null;
    }

    public boolean isVarAsFuncRParam() {
        return lBracket != null && exp != null && rBracket != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident.toString());
        if (lBracket != null && exp != null && rBracket != null) {
            sb.append(lBracket.toString());
            sb.append(exp.toString());
            sb.append(rBracket.toString());
        }
        sb.append(name + "\n");
        return sb.toString();
    }
}
