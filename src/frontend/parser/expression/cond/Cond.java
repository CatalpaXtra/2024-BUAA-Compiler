package frontend.parser.expression.cond;

public class Cond {
    private final String name = "<Cond>";
    private final LOrExp lOrExp;

    public Cond(LOrExp lOrExp) {
        this.lOrExp = lOrExp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lOrExp.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
