package frontend.parser.block.statement;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.expression.primary.LVal;
import frontend.parser.expression.primary.LValParser;

public class StmtGetCharParser {
    private final TokenIterator iterator;
    private LVal lVal;
    private Token assign;
    private Token getChar;
    private Token lParent;
    private Token rParent;
    private Token semicolon;

    public StmtGetCharParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public StmtGetChar parseStmtGetChar() {
        LValParser lValParser = new LValParser(iterator);
        lVal = lValParser.parseLVal();
        assign = iterator.getNextToken();
        getChar = iterator.getNextToken();
        lParent = iterator.getNextToken();
        rParent = iterator.getNextToken();
        semicolon = iterator.getNextToken();
        StmtGetChar stmtGetChar = new StmtGetChar(lVal, assign, getChar, lParent, rParent, semicolon);
        return stmtGetChar;
    }
}
