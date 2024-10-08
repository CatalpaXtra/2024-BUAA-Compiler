package frontend.parser.block.statement;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

public class StmtBreakParser {
    private final TokenIterator iterator;
    private Token break1;
    private Token semicolon;

    public StmtBreakParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public StmtBreak parseStmtBreak() {
        break1 = iterator.getNextToken();
        semicolon = iterator.getNextToken();
        StmtBreak stmtBreak = new StmtBreak(break1, semicolon);
        return stmtBreak;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(break1.toString());
        sb.append(semicolon.toString());
        return sb.toString();
    }
}
