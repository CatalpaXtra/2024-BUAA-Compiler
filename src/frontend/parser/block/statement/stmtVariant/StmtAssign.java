package frontend.parser.block.statement.stmtVariant;

import frontend.lexer.Token;
import frontend.parser.block.statement.StmtEle;
import frontend.parser.expression.Exp;
import frontend.parser.expression.primary.LVal;

public class StmtAssign implements StmtEle {
    private final LVal lVal;
    private final Token assign;
    private final Exp exp;
    private final Token semicolon;

    public StmtAssign(LVal lVal, Token assign,  Exp exp, Token semicolon) {
        this.lVal = lVal;
        this.assign = assign;
        this.exp = exp;
        this.semicolon = semicolon;
    }

    public LVal getlVal() {
        return lVal;
    }

    public Exp getExp() {
        return exp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lVal.toString());
        sb.append(assign.toString());
        sb.append(exp.toString());
        sb.append(semicolon.toString());
        return sb.toString();
    }
}
