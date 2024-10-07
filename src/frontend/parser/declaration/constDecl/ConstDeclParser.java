package frontend.parser.declaration.constDecl;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.declaration.BType;
import frontend.parser.declaration.BTypeParser;

import java.util.ArrayList;

public class ConstDeclParser {
    private final TokenIterator iterator;
    private Token constTk;
    private BType btype;
    private final ArrayList<ConstDef> constDefs;
    private final ArrayList<Token> commas;
    private Token semicolon;

    public ConstDeclParser(TokenIterator iterator) {
        this.iterator = iterator;
        this.constDefs = new ArrayList<>();
        this.commas = new ArrayList<>();
    }

    public ConstDecl parseConstDecl() {
        constTk = iterator.getNextToken();
        BTypeParser btypeParser = new BTypeParser(iterator);
        btype = btypeParser.parseBtype();
        ConstDefParser constDefParser = new ConstDefParser(iterator);
        constDefs.add(constDefParser.parseConstDef());
        Token token = iterator.getNextToken();
        while (token.getType().equals(Token.Type.COMMA)) {
            commas.add(token);
            constDefs.add(constDefParser.parseConstDef());
            token = iterator.getNextToken();
        }
        semicolon = token;
        ConstDecl constDecl = new ConstDecl(constTk, btype, constDefs, commas, semicolon);
        return constDecl;
    }

}
