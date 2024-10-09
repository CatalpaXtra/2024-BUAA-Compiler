package frontend.parser.expression.primary;

import frontend.ErrorHandler;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.expression.Exp;
import frontend.parser.expression.ExpParser;
import frontend.parser.terminal.Ident;
import frontend.parser.terminal.IdentParser;

public class LValParser {
    private final TokenIterator iterator;
    private Ident ident;
    private Token lBracket;
    private Exp exp;
    private Token rBracket;

    public LValParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public LVal parseLVal() {
        IdentParser identParser = new IdentParser(iterator);
        ident = identParser.parseIdent();
        Token token = iterator.getNextToken();
        if (token.getType().equals(Token.Type.LBRACK)) {
            lBracket = token;
            ExpParser expParser = new ExpParser(iterator);
            exp = expParser.parseExp();
            ErrorHandler errorHandler = new ErrorHandler(iterator);
            rBracket = errorHandler.handleErrorK();
        } else {
            iterator.traceBack(1);
            lBracket = rBracket = null;
            exp = null;
        }
        LVal lval = new LVal(ident, lBracket, exp, rBracket);
        return lval;
    }
}
