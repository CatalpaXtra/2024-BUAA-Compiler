package frontend.parser.block.statement;

import frontend.ErrorHandler;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.expression.primary.LVal;
import frontend.parser.expression.primary.LValParser;

public class StmtGetIntParser {
    private final TokenIterator iterator;
    private LVal lVal;
    private Token assign;
    private Token getInt;
    private Token lParent;
    private Token rParent;
    private Token semicolon;

    public StmtGetIntParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public StmtGetInt parseStmtGetInt() {
        LValParser lValParser = new LValParser(iterator);
        lVal = lValParser.parseLVal();
        assign = iterator.getNextToken();
        getInt = iterator.getNextToken();
        lParent = iterator.getNextToken();
        ErrorHandler errorHandler = new ErrorHandler(iterator);
        rParent = errorHandler.handleErrorJ();
        semicolon = errorHandler.handleErrorI();
        StmtGetInt stmtGetInt = new StmtGetInt(lVal, assign, getInt, lParent, rParent, semicolon);
        return stmtGetInt;
    }
}
