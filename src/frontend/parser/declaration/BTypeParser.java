package frontend.parser.declaration;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

public class BTypeParser {
    private final TokenIterator iterator;

    public BTypeParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public BType parseBtype() {
        Token first = iterator.getNextToken();
        if (!first.getType().equals(Token.Type.INTTK) && !first.getType().equals(Token.Type.CHARTK)) {
            System.out.println("ERROR : EXPECT INTTK OR CHARTK");
        }
        return new BType(first);
    }
}
