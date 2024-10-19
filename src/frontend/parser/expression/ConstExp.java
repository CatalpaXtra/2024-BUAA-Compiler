package frontend.parser.expression;

import frontend.parser.declaration.constDecl.constInitVal.ConstInitValEle;
import frontend.parser.expression.add.AddExp;

public class ConstExp implements ConstInitValEle {
    private final String name = "<ConstExp>";
    private final AddExp addExp;

    public ConstExp(AddExp addExp) {
        this.addExp = addExp;
    }

    public AddExp getAddExp() {
        return addExp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(addExp.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
