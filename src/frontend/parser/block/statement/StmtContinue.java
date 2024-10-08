package frontend.parser.block.statement;

import frontend.lexer.Token;

public class StmtContinue implements StmtEle {
    private final Token continue1;
    private final Token semicolon;

    public StmtContinue(Token continue1, Token semicolon) {
        this.continue1 = continue1;
        this.semicolon = semicolon;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(continue1.toString());
        sb.append(semicolon.toString());
        return sb.toString();
    }
}
