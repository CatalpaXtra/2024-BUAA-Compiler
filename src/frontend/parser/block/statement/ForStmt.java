package frontend.parser.block.statement;

import frontend.lexer.Token;
import frontend.parser.expression.Exp;
import frontend.parser.expression.primary.LVal;

public class ForStmt {
    private final String name = "<ForStmt>";
    private final LVal lVal;
    private final Token assign;
    private final Exp exp;

    public ForStmt(LVal lVal, Token assign, Exp exp) {
        this.lVal = lVal;
        this.assign = assign;
        this.exp = exp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lVal.toString());
        sb.append(assign.toString());
        sb.append(exp.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
