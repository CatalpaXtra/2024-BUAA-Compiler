package frontend;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.ParserErrors;

public class ErrorHandler {
    private final TokenIterator iterator;

    public ErrorHandler(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public Token handleErrorI() {
        Token token = iterator.getNextToken();
        if (token.getType().equals(Token.Type.SEMICN)) {
            return token;
        }
        iterator.traceBack(2);
        token = iterator.getNextToken();
        Error error = new Error(Error.Type.i, ";", token.getLine());
        ParserErrors.addError(error);
        return null;
    }

    public Token handleErrorJ() {
        Token token = iterator.getNextToken();
        if (token.getType().equals(Token.Type.RPARENT)) {
            return token;
        }
        iterator.traceBack(2);
        token = iterator.getNextToken();
        Error error = new Error(Error.Type.j, ")", token.getLine());
        ParserErrors.addError(error);
        return null;
    }

    public Token handleErrorK() {
        Token token = iterator.getNextToken();
        if (token.getType().equals(Token.Type.RBRACK)) {
            return token;
        }
        iterator.traceBack(2);
        token = iterator.getNextToken();
        Error error = new Error(Error.Type.k, "]", token.getLine());
        ParserErrors.addError(error);
        return null;
    }
}
