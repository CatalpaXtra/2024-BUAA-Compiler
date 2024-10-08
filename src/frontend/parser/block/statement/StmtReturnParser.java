package frontend.parser.block.statement;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
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
        if (token.getType().equals(Token.Type.SEMICN)) {
            exp = null;
            semicolon = token;
        } else {
            iterator.traceBack(1);
            ExpParser expParser = new ExpParser(iterator);
            exp = expParser.parseExp();
            semicolon = iterator.getNextToken();
        }
        StmtReturn stmtReturn = new StmtReturn(return1, exp, semicolon);
        return stmtReturn;
    }
}
