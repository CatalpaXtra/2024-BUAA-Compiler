package frontend.parser.block;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

import java.util.ArrayList;

public class BlockParser {
    private final TokenIterator iterator;
    private Token lBrace;
    private final ArrayList<BlockItem> blockItems;
    private Token rBrace;

    public BlockParser(TokenIterator iterator) {
        this.iterator = iterator;
        this.blockItems = new ArrayList<>();
    }

    public Block parseBlock() {
        lBrace = iterator.getNextToken();
        Token token = iterator.getNextToken();
        while (!token.getType().equals(Token.Type.RBRACE)) {
            iterator.traceBack(1);
            BlockItemParser blockItemParser = new BlockItemParser(iterator);
            blockItems.add(blockItemParser.parseBlockItem());
            token = iterator.getNextToken();
        }
        rBrace = token;
        Block block = new Block(lBrace, blockItems, rBrace);
        return block;
    }
}
