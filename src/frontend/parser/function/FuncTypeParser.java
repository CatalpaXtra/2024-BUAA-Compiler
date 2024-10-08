package frontend.parser.function;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

public class FuncTypeParser {
    private final TokenIterator iterator;

    public FuncTypeParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public FuncType parseFuncType() {
        Token token = iterator.getNextToken();
        FuncType funcType = new FuncType(token);
        return funcType;
    }
}
