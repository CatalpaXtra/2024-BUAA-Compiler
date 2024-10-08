package frontend.parser.block.statement;

import frontend.lexer.Token;
import frontend.parser.expression.primary.LVal;

public class StmtGetChar implements StmtEle {
    private final LVal lVal;
    private final Token assign;
    private final Token getChar;
    private final Token lParent;
    private final Token rParent;
    private final Token semicolon;

    public StmtGetChar(LVal lVal, Token assign, Token getChar, Token lParent, Token rParent, Token semicolon) {
        this.lVal = lVal;
        this.assign = assign;
        this.getChar = getChar;
        this.lParent = lParent;
        this.rParent = rParent;
        this.semicolon = semicolon;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lVal.toString());
        sb.append(assign.toString());
        sb.append(getChar.toString());
        sb.append(lParent.toString());
        sb.append(rParent.toString());
        sb.append(semicolon.toString());
        return sb.toString();
    }
}
