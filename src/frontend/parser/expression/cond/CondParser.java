package frontend.parser.expression.cond;

import frontend.lexer.TokenIterator;

public class CondParser {
    private final TokenIterator iterator;
    private LOrExp lOrExp;

    public CondParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public Cond parseCond() {
        LOrExpParser lOrExpParser = new LOrExpParser(iterator);
        lOrExp = lOrExpParser.parseLOrExp();
        Cond cond = new Cond(lOrExp);
        return cond;
    }
}
