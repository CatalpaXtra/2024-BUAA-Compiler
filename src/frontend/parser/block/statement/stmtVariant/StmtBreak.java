package frontend.parser.block.statement.stmtVariant;

import frontend.lexer.Token;
import frontend.parser.block.statement.StmtEle;

public class StmtBreak implements StmtEle {
    private final Token break1;
    private final Token semicolon;

    public StmtBreak(Token break1, Token semicolon) {
        this.break1 = break1;
        this.semicolon = semicolon;
    }

    public int getLineNum() {
        return break1.getLine();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(break1.toString());
        sb.append(semicolon.toString());
        return sb.toString();
    }
}
