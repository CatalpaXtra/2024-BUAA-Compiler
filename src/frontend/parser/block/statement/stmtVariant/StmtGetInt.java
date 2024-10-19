package frontend.parser.block.statement.stmtVariant;

import frontend.lexer.Token;
import frontend.parser.block.statement.StmtEle;
import frontend.parser.expression.primary.LVal;

public class StmtGetInt implements StmtEle {
    private final LVal lVal;
    private final Token assign;
    private final Token getInt;
    private final Token lParent;
    private final Token rParent;
    private final Token semicolon;

    public StmtGetInt(LVal lVal, Token assign, Token getInt, Token lParent, Token rParent, Token semicolon) {
        this.lVal = lVal;
        this.assign = assign;
        this.getInt = getInt;
        this.lParent = lParent;
        this.rParent = rParent;
        this.semicolon = semicolon;
    }

    public LVal getlVal() {
        return lVal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lVal.toString());
        sb.append(assign.toString());
        sb.append(getInt.toString());
        sb.append(lParent.toString());
        sb.append(rParent.toString());
        sb.append(semicolon.toString());
        return sb.toString();
    }
}
