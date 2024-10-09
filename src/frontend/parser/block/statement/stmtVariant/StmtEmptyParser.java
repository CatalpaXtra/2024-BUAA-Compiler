package frontend.parser.block.statement.stmtVariant;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

public class StmtEmptyParser {
    private final TokenIterator iterator;
    private Token semicolon;

    public StmtEmptyParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public StmtEmpty parseStmtEmpty() {
        semicolon = iterator.getNextToken();
        StmtEmpty stmtEmpty = new StmtEmpty(semicolon);
        return stmtEmpty;
    }
}
