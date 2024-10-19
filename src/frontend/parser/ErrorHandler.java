package frontend.parser;

import frontend.lexer.Error;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

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
        token = new Token(Token.Type.SEMICN, ";", token.getLine());
        return token;
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
        token = new Token(Token.Type.RPARENT, ")", token.getLine());
        return token;
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
        token = new Token(Token.Type.RBRACK, "]", token.getLine());
        return token;
    }
}
