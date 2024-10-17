package frontend.parser.block;

import frontend.lexer.Token;
import frontend.parser.block.statement.StmtEle;

import java.util.ArrayList;

public class Block implements StmtEle {
    private final String name = "<Block>";
    private final Token lBrace;
    private final ArrayList<BlockItem> blockItems;
    private final Token rBrace;

    public Block(Token lBrace, ArrayList<BlockItem> blockItems, Token rBrace) {
        this.lBrace = lBrace;
        this.blockItems = blockItems;
        this.rBrace = rBrace;
    }

    public ArrayList<BlockItem> getBlockItems() {
        return blockItems;
    }

    public Token getRBrace() {
        return rBrace;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lBrace.toString());
        int len = blockItems.size();
        for (int i = 0; i < len; i++) {
            sb.append(blockItems.get(i));
        }
        sb.append(rBrace.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
