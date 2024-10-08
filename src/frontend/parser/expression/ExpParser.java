package frontend.parser.expression;

import frontend.lexer.TokenIterator;
import frontend.parser.expression.add.AddExp;
import frontend.parser.expression.add.AddExpParser;

public class ExpParser {
    private final TokenIterator iterator;
    private AddExp addExp;

    public ExpParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public Exp parseExp() {
        AddExpParser addExpParser = new AddExpParser(iterator);
        addExp = addExpParser.parseAddExp();
        Exp exp = new Exp(addExp);
        return exp;
    }
}
