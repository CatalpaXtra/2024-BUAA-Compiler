package frontend.parser.block.statement;

import frontend.ErrorHandler;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

public class StmtBreakParser {
    private final TokenIterator iterator;
    private Token break1;
    private Token semicolon;

    public StmtBreakParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public StmtBreak parseStmtBreak() {
        break1 = iterator.getNextToken();
        ErrorHandler errorHandler = new ErrorHandler(iterator);
        semicolon = errorHandler.handleErrorI();
        StmtBreak stmtBreak = new StmtBreak(break1, semicolon);
        return stmtBreak;
    }
}
