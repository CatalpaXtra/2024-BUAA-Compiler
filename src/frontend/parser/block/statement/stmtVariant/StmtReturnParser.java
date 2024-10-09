package frontend.parser.block.statement.stmtVariant;

import frontend.Error;
import frontend.ErrorHandler;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.ParserErrors;
import frontend.parser.expression.Exp;
import frontend.parser.expression.ExpParser;

public class StmtReturnParser {
    private final TokenIterator iterator;
    private Token return1;
    private Exp exp;
    private Token semicolon;

    public StmtReturnParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public StmtReturn parseStmtReturn() {
        return1 = iterator.getNextToken();
        Token token = iterator.getNextToken();
        iterator.traceBack(1);
        exp = null;
        if (token.getType().equals(Token.Type.RBRACE)) {
            /* prevent out of bound */
            Error error = new Error(Error.Type.i, ";", return1.getLine());
            ParserErrors.addError(error);
            semicolon = null;
        } else {
            if (!token.getType().equals(Token.Type.SEMICN)) {
                ExpParser expParser = new ExpParser(iterator);
                exp = expParser.parseExp();
            }
            ErrorHandler errorHandler = new ErrorHandler(iterator);
            semicolon = errorHandler.handleErrorI();
        }
        StmtReturn stmtReturn = new StmtReturn(return1, exp, semicolon);
        return stmtReturn;
    }
}
