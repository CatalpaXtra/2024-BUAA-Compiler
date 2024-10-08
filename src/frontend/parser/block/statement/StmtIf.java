package frontend.parser.block.statement;

import frontend.lexer.Token;
import frontend.parser.expression.cond.Cond;

public class StmtIf implements StmtEle {
    private final Token if1;
    private final Token lParent;
    private final Cond cond;
    private final Token rParent;
    private final Stmt stmt1;
    private final Token else1;
    private final Stmt stmt2;

    public StmtIf(Token if1, Token lParent, Cond cond, Token rParent, Stmt stmt1, Token else1, Stmt stmt2) {
        this.if1 = if1;
        this.lParent = lParent;
        this.cond = cond;
        this.rParent = rParent;
        this.stmt1 = stmt1;
        this.else1 = else1;
        this.stmt2 = stmt2;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(if1.toString());
        sb.append(lParent.toString());
        sb.append(cond.toString());
        sb.append(rParent.toString());
        sb.append(stmt1.toString());
        if (else1 != null && stmt2 != null) {
            sb.append(else1.toString());
            sb.append(stmt2.toString());
        }
        return sb.toString();
    }
}
