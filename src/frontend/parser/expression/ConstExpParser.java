package frontend.parser.expression;

import frontend.lexer.TokenIterator;
import frontend.parser.expression.add.AddExp;
import frontend.parser.expression.add.AddExpParser;

public class ConstExpParser {
    private final TokenIterator iterator;

    public ConstExpParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public ConstExp parseConstExp() {
        AddExpParser addExpParser = new AddExpParser(iterator);
        AddExp addExp = addExpParser.parseAddExp();
        ConstExp constExp = new ConstExp(addExp);
        return constExp;
    }
}
