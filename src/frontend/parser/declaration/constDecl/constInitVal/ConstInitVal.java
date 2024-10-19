package frontend.parser.declaration.constDecl.constInitVal;

public class ConstInitVal {
    private final String name = "<ConstInitVal>";
    private final ConstInitValEle constInitValEle;

    public ConstInitVal(ConstInitValEle constInitValEle) {
        this.constInitValEle = constInitValEle;
    }

    public ConstInitValEle getConstInitValEle() {
        return constInitValEle;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(constInitValEle.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
