package frontend.parser.block.statement;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.expression.cond.Cond;
import frontend.parser.expression.cond.CondParser;

public class StmtForParser {
    private final TokenIterator iterator;
    private Token for1;
    private Token lParent;
    private ForStmt forStmt1;
    private Token semicolon1;
    private Cond cond;
    private Token semicolon2;
    private ForStmt forStmt2;
    private Token rParent;
    private Stmt stmt;

    public StmtForParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public StmtFor parseStmtFor() {
        for1 = iterator.getNextToken();
        lParent = iterator.getNextToken();
        Token token = iterator.getNextToken();
        if (token.getType().equals(Token.Type.SEMICN)) {
            forStmt1 = null;
            semicolon1 = token;
        } else {
            iterator.traceBack(1);
            ForStmtParser forStmtParser = new ForStmtParser(iterator);
            forStmt1 = forStmtParser.parseForStmt();
            semicolon1 = iterator.getNextToken();
        }
        token = iterator.getNextToken();
        if (token.getType().equals(Token.Type.SEMICN)) {
            cond = null;
            semicolon2 = token;
        } else {
            iterator.traceBack(1);
            CondParser condParser = new CondParser(iterator);
            cond = condParser.parseCond();
            semicolon2 = iterator.getNextToken();
        }
        token = iterator.getNextToken();
        if (token.getType().equals(Token.Type.RPARENT)) {
            forStmt2 = null;
            rParent = token;
        } else {
            iterator.traceBack(1);
            ForStmtParser forStmtParser = new ForStmtParser(iterator);
            forStmt2 = forStmtParser.parseForStmt();
            rParent = iterator.getNextToken();
        }
        StmtParser stmtParser = new StmtParser(iterator);
        stmt = stmtParser.parseStmt();
        StmtFor stmtFor = new StmtFor(for1, lParent, forStmt1, semicolon1, cond, semicolon2, forStmt2, rParent, stmt);
        return stmtFor;
    }
}
