package frontend.parser.block.statement.stmtVariant;

import frontend.parser.ErrorHandler;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.expression.Exp;
import frontend.parser.expression.ExpParser;

public class StmtExpParser {
    private final TokenIterator iterator;
    private Exp exp;
    private Token semicolon;

    public StmtExpParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public StmtExp parseStmtExp() {
        ExpParser expParser = new ExpParser(iterator);
        exp = expParser.parseExp();
        ErrorHandler errorHandler = new ErrorHandler(iterator);
        semicolon = errorHandler.handleErrorI();
        StmtExp stmtExp = new StmtExp(exp, semicolon);
        return stmtExp;
    }
}
