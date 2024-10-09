package frontend.parser.expression.primary;

import frontend.lexer.TokenIterator;
import frontend.parser.terminal.IntConst;
import frontend.parser.terminal.IntConstParser;

public class NumberParser {
    private final TokenIterator iterator;
    private IntConst intConst;

    public NumberParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public Number parseNumber() {
        IntConstParser intConstParser = new IntConstParser(iterator);
        intConst = intConstParser.parseIntConst();
        Number number = new Number(intConst);
        return number;
    }
}
