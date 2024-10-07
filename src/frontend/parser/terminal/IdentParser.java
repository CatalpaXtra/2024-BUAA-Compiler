package frontend.parser.terminal;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

public class IdentParser {
    private final TokenIterator iterator;

    public IdentParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public Ident parseIdent() {
        Token token = this.iterator.getNextToken();
        if (!token.getType().equals(Token.Type.IDENFR)) {
            System.out.println("EXPECT IDENFR HERE");
        }
        Ident ident = new Ident(token);
        return ident;
    }
}
