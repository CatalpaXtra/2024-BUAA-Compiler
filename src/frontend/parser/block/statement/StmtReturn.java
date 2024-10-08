package frontend.parser.block.statement;

import frontend.lexer.Token;
import frontend.parser.expression.Exp;

public class StmtReturn implements StmtEle {
    private final Token return1;
    private final Exp exp;
    private final Token semicolon;

    public StmtReturn(Token return1, Exp exp, Token semicolon) {
        this.return1 = return1;
        this.exp = exp;
        this.semicolon = semicolon;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(return1.toString());
        if (exp != null) {
            sb.append(exp.toString());
        }
        sb.append(semicolon.toString());
        return sb.toString();
    }
}
