package frontend.parser.block.statement;

import frontend.lexer.Token;

public class StmtBreak implements StmtEle {
    private final Token break1;
    private final Token semicolon;

    public StmtBreak(Token break1, Token semicolon) {
        this.break1 = break1;
        this.semicolon = semicolon;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(break1.toString());
        sb.append(semicolon.toString());
        return sb.toString();
    }
}
