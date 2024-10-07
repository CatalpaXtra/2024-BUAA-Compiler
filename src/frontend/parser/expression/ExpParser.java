package frontend.parser.expression;

import frontend.lexer.TokenIterator;
import frontend.parser.expression.add.AddExp;
import frontend.parser.expression.add.AddExpParser;

public class ExpParser {
    private final TokenIterator iterator;

    public ExpParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public Exp parseExp() {
        AddExpParser addExpParser = new AddExpParser(this.iterator);
        AddExp addExp = addExpParser.parseAddExp();
        Exp exp = new Exp(addExp);
        return exp;
    }
}
