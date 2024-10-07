package frontend.parser.terminal;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

public class CharConstParser {
    private final TokenIterator iterator;

    public CharConstParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public CharConst parseCharConst() {
        Token token = iterator.getNextToken();
        if (!token.getType().equals(Token.Type.CHRCON)) {
            System.out.println("EXPECT INTCON HERE");
        }
        CharConst charConst = new CharConst(token);
        return charConst;
    }
}
