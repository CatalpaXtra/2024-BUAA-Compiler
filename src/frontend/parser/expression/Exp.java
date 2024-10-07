package frontend.parser.expression;

import frontend.parser.expression.add.AddExp;

public class Exp {
    private final String name = "<Exp>";
    private final AddExp addExp;

    public Exp(AddExp addExp) {
        this.addExp = addExp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(addExp.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
