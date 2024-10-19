package frontend.parser.expression.primary;

import frontend.parser.ErrorHandler;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.expression.Exp;
import frontend.parser.expression.ExpParser;

public class ExpInParentParser {
    private final TokenIterator iterator;
    private Token lParent;
    private Exp exp;
    private Token rParent;

    public ExpInParentParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public ExpInParent parseExpInParent() {
        lParent = iterator.getNextToken();
        ExpParser expParser = new ExpParser(iterator);
        exp = expParser.parseExp();
        ErrorHandler errorHandler = new ErrorHandler(iterator);
        rParent = errorHandler.handleErrorJ();
        ExpInParent expInParent = new ExpInParent(lParent, exp, rParent);
        return expInParent;
    }
}
