package frontend.parser.terminal;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

public class StringConstParser {
    private final TokenIterator iterator;

    public StringConstParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public StringConst parseStringConst() {
        Token token = iterator.getNextToken();
        if (!token.getType().equals(Token.Type.STRCON)) {
            System.out.println("EXPECT STRCON HERE");
        }
        StringConst stringConst = new StringConst(token);
        return stringConst;
    }
}
