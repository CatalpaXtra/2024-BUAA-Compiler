package frontend.parser.expression.primary;

import frontend.lexer.Token;
import frontend.parser.expression.Exp;

public class ExpInParent implements PrimaryEle {
    private final Token lParent;
    private final Exp exp;
    private final Token rParent;

    public ExpInParent(Token lParent, Exp exp, Token rParent) {
        this.lParent = lParent;
        this.exp = exp;
        this.rParent = rParent;
    }

    public Exp getExp() {
        return exp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lParent.toString());
        sb.append(exp.toString());
        sb.append(rParent.toString());
        return sb.toString();
    }
}
