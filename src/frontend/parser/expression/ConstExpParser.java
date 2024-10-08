package frontend.parser.expression;

import frontend.lexer.TokenIterator;
import frontend.parser.expression.add.AddExp;
import frontend.parser.expression.add.AddExpParser;

public class ConstExpParser {
    private final TokenIterator iterator;
    private AddExp addExp;

    public ConstExpParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public ConstExp parseConstExp() {
        AddExpParser addExpParser = new AddExpParser(iterator);
        addExp = addExpParser.parseAddExp();
        ConstExp constExp = new ConstExp(addExp);
        return constExp;
    }
}
