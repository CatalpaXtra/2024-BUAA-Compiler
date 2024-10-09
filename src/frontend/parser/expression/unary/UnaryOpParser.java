package frontend.parser.expression.unary;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

public class UnaryOpParser {
    private final TokenIterator iterator;

    public UnaryOpParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public UnaryOp parseUnaryOp() {
        Token token = iterator.getNextToken();
        UnaryOp unaryOp = new UnaryOp(token);
        return unaryOp;
    }
}
