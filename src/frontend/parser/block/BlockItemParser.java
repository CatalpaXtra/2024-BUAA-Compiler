package frontend.parser.block;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.block.statement.StmtParser;
import frontend.parser.declaration.DeclParser;

public class BlockItemParser {
    private final TokenIterator iterator;
    private BlockItemEle blockItemEle;

    public BlockItemParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public BlockItem parseBlockItem() {
        Token token = iterator.getNextToken();
        iterator.traceBack(1);
        if (token.getType().equals(Token.Type.CONSTTK) || token.getType().equals(Token.Type.INTTK) || token.getType().equals(Token.Type.CHARTK)) {
            DeclParser declParser = new DeclParser(iterator);
            blockItemEle = declParser.parseDecl();
        } else {
            StmtParser stmtParser = new StmtParser(iterator);
            blockItemEle = stmtParser.parseStmt();
        }
        BlockItem blockItem = new BlockItem(blockItemEle);
        return blockItem;
    }
}
