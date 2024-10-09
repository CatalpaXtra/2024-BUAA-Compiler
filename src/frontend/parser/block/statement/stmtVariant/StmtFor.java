package frontend.parser.block.statement.stmtVariant;

import frontend.lexer.Token;
import frontend.parser.block.statement.Stmt;
import frontend.parser.block.statement.StmtEle;
import frontend.parser.block.statement.stmtVariant.ForStmt;
import frontend.parser.expression.cond.Cond;

public class StmtFor implements StmtEle {
    private final Token for1;
    private final Token lParent;
    private final ForStmt forStmt1;
    private final Token semicolon1;
    private final Cond cond;
    private final Token semicolon2;
    private final ForStmt forStmt2;
    private final Token rParent;
    private final Stmt stmt;

    public StmtFor(Token for1, Token lParent, ForStmt forStmt1, Token semicolon1,
                   Cond cond, Token semicolon2, ForStmt forStmt2, Token rParent, Stmt stmt) {
        this.for1 = for1;
        this.lParent = lParent;
        this.forStmt1 = forStmt1;
        this.semicolon1 = semicolon1;
        this.cond = cond;
        this.semicolon2 = semicolon2;
        this.forStmt2 = forStmt2;
        this.rParent = rParent;
        this.stmt = stmt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(for1.toString());
        sb.append(lParent.toString());
        if (forStmt1 != null) {
            sb.append(forStmt1.toString());
        }
        sb.append(semicolon1.toString());
        if (cond != null) {
            sb.append(cond.toString());
        }
        sb.append(semicolon2.toString());
        if (forStmt2 != null) {
            sb.append(forStmt2.toString());
        }
        sb.append(rParent.toString());
        sb.append(stmt.toString());
        return sb.toString();
    }
}
