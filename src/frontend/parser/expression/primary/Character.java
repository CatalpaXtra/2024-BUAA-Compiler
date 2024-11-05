package frontend.parser.expression.primary;

import frontend.parser.terminal.CharConst;

public class Character implements PrimaryEle {
    private final String name = "<Character>";
    private final CharConst charConst;

    public Character(CharConst charConst) {
        this.charConst = charConst;
    }

    public CharConst getCharConst() {
        return charConst;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(charConst.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
