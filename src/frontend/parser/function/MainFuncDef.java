package frontend.parser.function;

import frontend.lexer.Token;
import frontend.parser.block.Block;

public class MainFuncDef {
    private final String name = "<MainFuncDef>";
    private final Token intTk;
    private final Token mainTk;
    private final Token lParent;
    private final Token rParent;
    private final Block block;

    public MainFuncDef(Token intTk, Token mainTk, Token lParent, Token rParent, Block block) {
        this.intTk = intTk;
        this.mainTk = mainTk;
        this.lParent = lParent;
        this.rParent = rParent;
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(intTk.toString());
        sb.append(mainTk.toString());
        sb.append(lParent.toString());
        sb.append(rParent.toString());
        sb.append(block.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
