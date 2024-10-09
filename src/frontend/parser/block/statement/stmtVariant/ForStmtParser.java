package frontend.parser.block.statement.stmtVariant;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.expression.Exp;
import frontend.parser.expression.ExpParser;
import frontend.parser.expression.primary.LVal;
import frontend.parser.expression.primary.LValParser;

public class ForStmtParser {
    private final TokenIterator iterator;
    private LVal lVal;
    private Token assign;
    private Exp exp;

    public ForStmtParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public ForStmt parseForStmt() {
        LValParser lValParser = new LValParser(iterator);
        lVal = lValParser.parseLVal();
        assign = iterator.getNextToken();
        ExpParser expParser = new ExpParser(iterator);
        exp = expParser.parseExp();
        ForStmt forStmt = new ForStmt(lVal, assign, exp);
        return forStmt;
    }
}
