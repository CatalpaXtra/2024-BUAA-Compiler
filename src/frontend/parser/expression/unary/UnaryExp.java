package frontend.parser.expression.unary;

public class UnaryExp {
    private final String name = "<UnaryExp>";
    private final UnaryEle unaryEle;

    public UnaryExp(UnaryEle unaryEle) {
        this.unaryEle = unaryEle;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(unaryEle.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
