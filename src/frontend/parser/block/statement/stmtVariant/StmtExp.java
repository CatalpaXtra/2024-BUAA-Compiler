package frontend.parser.block.statement.stmtVariant;

import frontend.lexer.Token;
import frontend.parser.block.statement.StmtEle;
import frontend.parser.expression.Exp;

public class StmtExp implements StmtEle {
    private final Exp exp;
    private final Token semicolon;

    public StmtExp(Exp exp, Token semicolon) {
        this.exp = exp;
        this.semicolon = semicolon;
    }

    public Exp getExp() {
        return exp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(exp.toString());
        sb.append(semicolon.toString());
        return sb.toString();
    }
}
