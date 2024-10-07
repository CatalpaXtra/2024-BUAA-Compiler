package frontend.parser.expression.primary;

import frontend.parser.terminal.IntConst;

public class Number implements PrimaryEle {
    private final String name = "<Number>";
    private final IntConst intConst;

    public Number(IntConst intConst) {
        this.intConst = intConst;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(intConst.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
