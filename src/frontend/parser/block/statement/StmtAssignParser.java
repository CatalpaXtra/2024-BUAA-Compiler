package frontend.parser.block.statement;

import frontend.ErrorHandler;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.expression.Exp;
import frontend.parser.expression.ExpParser;
import frontend.parser.expression.primary.LVal;
import frontend.parser.expression.primary.LValParser;

public class StmtAssignParser {
    private final TokenIterator iterator;
    private LVal lVal;
    private Token assign;
    private Exp exp;
    private Token semicolon;

    public StmtAssignParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public StmtAssign parseStmtAssign() {
        LValParser lValParser = new LValParser(iterator);
        lVal = lValParser.parseLVal();
        assign = iterator.getNextToken();
        ExpParser expParser = new ExpParser(iterator);
        exp = expParser.parseExp();
        ErrorHandler errorHandler = new ErrorHandler(iterator);
        semicolon = errorHandler.handleErrorI();
        StmtAssign stmtAssign = new StmtAssign(lVal, assign, exp, semicolon);
        return stmtAssign;
    }
}
