package frontend.parser.expression.unary;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

public class UnaryOpParser {
    private final TokenIterator iterator;

    public UnaryOpParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public UnaryOp parseUnaryOp() {
        Token first = iterator.getNextToken();
        if (!first.getType().equals(Token.Type.PLUS) && !first.getType().equals(Token.Type.MINU) && !first.getType().equals(Token.Type.NOT)) {
            System.out.println("ERROR : EXPECT PLUS OR MINU OR NOT");
        }
        return new UnaryOp(first);
    }
}
