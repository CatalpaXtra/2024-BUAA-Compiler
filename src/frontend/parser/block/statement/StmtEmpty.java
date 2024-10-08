package frontend.parser.block.statement;

import frontend.lexer.Token;

public class StmtEmpty implements StmtEle {
    private final Token semicolon;

    public StmtEmpty(Token semicolon) {
        this.semicolon = semicolon;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(semicolon.toString());
        return sb.toString();
    }
}
