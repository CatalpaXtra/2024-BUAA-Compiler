package frontend.parser.block.statement;

import frontend.parser.block.BlockItemEle;

public class Stmt implements BlockItemEle {
    private final String name = "<Stmt>";
    private final StmtEle stmtEle;

    public Stmt(StmtEle stmtEle) {
        this.stmtEle = stmtEle;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(stmtEle.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
