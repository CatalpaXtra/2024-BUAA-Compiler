package frontend.parser.block.statement.stmtVariant;

import frontend.ErrorHandler;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.block.statement.Stmt;
import frontend.parser.block.statement.StmtParser;
import frontend.parser.expression.cond.Cond;
import frontend.parser.expression.cond.CondParser;

public class StmtIfParser {
    private final TokenIterator iterator;
    private Token if1;
    private Token lParent;
    private Cond cond;
    private Token rParent;
    private Stmt stmt1;
    private Token else1;
    private Stmt stmt2;

    public StmtIfParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public StmtIf parseStmtIf() {
        if1 = iterator.getNextToken();
        lParent = iterator.getNextToken();
        CondParser condParser = new CondParser(iterator);
        cond = condParser.parseCond();
        ErrorHandler errorHandler = new ErrorHandler(iterator);
        rParent = errorHandler.handleErrorJ();
        StmtParser stmtParser = new StmtParser(iterator);
        stmt1 = stmtParser.parseStmt();

        Token token = iterator.getNextToken();
        if (token.getType().equals(Token.Type.ELSETK)) {
            else1 = token;
            stmt2 = stmtParser.parseStmt();
        } else {
            iterator.traceBack(1);
            else1 = null;
            stmt2 = null;
        }
        StmtIf stmtIf = new StmtIf(if1, lParent, cond, rParent, stmt1, else1, stmt2);
        return stmtIf;
    }
}
