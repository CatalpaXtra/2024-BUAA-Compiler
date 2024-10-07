package frontend.parser.expression.primary;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

public class PrimaryExpParser {
    private final TokenIterator iterator;
    private PrimaryEle primaryEle;

    public PrimaryExpParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public PrimaryExp parsePrimaryExp() {
        Token token = iterator.getNextToken();
        iterator.traceBack(1);
        if (token.getType().equals(Token.Type.LPARENT)) {
            ExpInParentParser expInParentParser = new ExpInParentParser(iterator);
            primaryEle = expInParentParser.parseExpInParent();
        } else if (token.getType().equals(Token.Type.IDENFR)) {
            LValParser lvalParser = new LValParser(iterator);
            primaryEle = lvalParser.parseLVal();
        } else if (token.getType().equals(Token.Type.INTCON)) {
            NumberParser numberParser = new NumberParser(iterator);
            primaryEle = numberParser.parseNumber();
        } else if (token.getType().equals(Token.Type.CHRCON)) {
            CharacterParser characterParser = new CharacterParser(iterator);
            primaryEle = characterParser.parseCharacter();
        }
        PrimaryExp primaryExp = new PrimaryExp(primaryEle);
        return primaryExp;
    }
}
