package frontend.parser.block.statement;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

public class StmtContinueParser {
    private final TokenIterator iterator;
    private Token continue1;
    private Token semicolon;

    public StmtContinueParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public StmtContinue parseStmtContinue() {
        continue1 = iterator.getNextToken();
        semicolon = iterator.getNextToken();
        StmtContinue stmtContinue = new StmtContinue(continue1, semicolon);
        return stmtContinue;
    }
}
