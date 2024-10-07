package frontend.parser.expression.unary;

public class UnaryOpExp implements UnaryEle {
    private final UnaryOp unaryOp;
    private final UnaryExp unaryExp;

    public UnaryOpExp(UnaryOp unaryOp, UnaryExp unaryExp) {
        this.unaryOp = unaryOp;
        this.unaryExp = unaryExp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(unaryOp.toString());
        sb.append(unaryExp.toString());
        return sb.toString();
    }
}
