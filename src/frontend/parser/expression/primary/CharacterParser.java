package frontend.parser.expression.primary;

import frontend.lexer.TokenIterator;
import frontend.parser.terminal.CharConst;
import frontend.parser.terminal.CharConstParser;

public class CharacterParser {
    private final TokenIterator iterator;

    public CharacterParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public Character parseCharacter() {
        CharConstParser charConstParser = new CharConstParser(iterator);
        CharConst charConst = charConstParser.parseCharConst();
        Character character = new Character(charConst);
        return character;
    }
}
