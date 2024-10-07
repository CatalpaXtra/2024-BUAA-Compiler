package frontend.parser.terminal;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

public class IntConstParser {
    private final TokenIterator iterator;

    public IntConstParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public IntConst parseIntConst() {
        Token token = iterator.getNextToken();
        if (!token.getType().equals(Token.Type.INTCON)) {
            System.out.println("EXPECT INTCON HERE");
        }
        IntConst intConst = new IntConst(token);
        return intConst;
    }
}
