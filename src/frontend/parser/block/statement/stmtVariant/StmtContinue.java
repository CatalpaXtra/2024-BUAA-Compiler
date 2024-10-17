package frontend.parser.block.statement.stmtVariant;

import frontend.lexer.Token;
import frontend.parser.block.statement.StmtEle;

public class StmtContinue implements StmtEle {
    private final Token continue1;
    private final Token semicolon;

    public StmtContinue(Token continue1, Token semicolon) {
        this.continue1 = continue1;
        this.semicolon = semicolon;
    }

    public int getLineNum() {
        return continue1.getLine();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(continue1.toString());
        sb.append(semicolon.toString());
        return sb.toString();
    }
}
