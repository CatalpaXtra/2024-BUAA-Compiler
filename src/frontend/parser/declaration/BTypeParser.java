package frontend.parser.declaration;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

public class BTypeParser {
    private final TokenIterator iterator;

    public BTypeParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public BType parseBtype() {
        Token token = iterator.getNextToken();
        BType bType = new BType(token);
        return bType;
    }
}
