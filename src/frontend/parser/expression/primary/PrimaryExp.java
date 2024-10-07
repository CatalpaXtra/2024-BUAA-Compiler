package frontend.parser.expression.primary;

import frontend.parser.expression.unary.UnaryEle;

public class PrimaryExp implements UnaryEle {
    private final String name = "<PrimaryExp>";
    private final PrimaryEle primaryEle;

    public PrimaryExp(PrimaryEle primaryEle) {
        this.primaryEle = primaryEle;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(primaryEle.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
