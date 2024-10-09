package frontend.parser.function;

import frontend.lexer.Token;
import frontend.parser.block.Block;
import frontend.parser.function.params.FuncFParams;
import frontend.parser.terminal.Ident;

public class FuncDef {
    private final String name = "<FuncDef>";
    private final FuncType funcType;
    private final Ident ident;
    private final Token lParent;
    private final FuncFParams funcFParams;
    private final Token rParent;
    private final Block block;

    public FuncDef(FuncType funcType, Ident ident, Token lParent, FuncFParams funcFParams, Token rParent, Block block) {
        this.funcType = funcType;
        this.ident = ident;
        this.lParent = lParent;
        this.funcFParams = funcFParams;
        this.rParent = rParent;
        this.block = block;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(funcType.toString());
        sb.append(ident.toString());
        sb.append(lParent.toString());
        if (funcFParams != null) {
            sb.append(funcFParams.toString());
        }
        sb.append(rParent.toString());
        sb.append(block.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
