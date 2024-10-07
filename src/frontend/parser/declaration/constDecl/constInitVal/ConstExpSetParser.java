package frontend.parser.declaration.constDecl.constInitVal;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.expression.ConstExp;
import frontend.parser.expression.ConstExpParser;

import java.util.ArrayList;

public class ConstExpSetParser {
    private final TokenIterator iterator;
    private Token lBrace;
    private final ArrayList<ConstExp> constExps;
    private final ArrayList<Token> commas;
    private Token rBrace;

    public ConstExpSetParser(TokenIterator iterator) {
        this.iterator = iterator;
        this.constExps = new ArrayList<>();
        this.commas = new ArrayList<>();
    }

    public ConstExpSet parseConstExpSet() {
        lBrace = iterator.getNextToken();
        Token token = iterator.getNextToken();
        if (token.getType().equals(Token.Type.RBRACE)) {
            rBrace = token;
        } else {
            iterator.traceBack(1);
            ConstExpParser constExpParser = new ConstExpParser(iterator);
            constExps.add(constExpParser.parseConstExp());
            token = iterator.getNextToken();
            while (token.getType().equals(Token.Type.COMMA)) {
                commas.add(token);
                constExps.add(constExpParser.parseConstExp());
                token = iterator.getNextToken();
            }
            rBrace = token;
        }
        ConstExpSet constExpSet = new ConstExpSet(lBrace, constExps, commas, rBrace);
        return constExpSet;
    }
}
