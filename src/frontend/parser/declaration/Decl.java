package frontend.parser.declaration;

import frontend.parser.block.BlockItemEle;

public class Decl implements BlockItemEle {
    private final DeclEle declEle;

    public Decl(DeclEle declEle) {
        this.declEle = declEle;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(declEle.toString());
        return sb.toString();
    }
}
