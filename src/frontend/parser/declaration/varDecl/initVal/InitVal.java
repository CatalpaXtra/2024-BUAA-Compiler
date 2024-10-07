package frontend.parser.declaration.varDecl.initVal;

public class InitVal {
    private final String name = "<InitVal>";
    private final InitValEle initValEle;

    public InitVal(InitValEle initValEle) {
        this.initValEle = initValEle;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(initValEle.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
