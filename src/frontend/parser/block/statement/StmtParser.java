package frontend.parser.block.statement;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.block.BlockParser;
import frontend.parser.block.statement.stmtVariant.*;

public class StmtParser {
    private final TokenIterator iterator;
    private StmtEle stmtEle;

    public StmtParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public Stmt parseStmt() {
        Token token = iterator.getNextToken();
        iterator.traceBack(1);
        switch (token.getType()) {
            case IDENFR:        // LVal = Exp/getint()/getchar();
                caseIdenfr();
                break;
            case LPARENT: case INTCON: case CHRCON: case PLUS: case MINU:   // Exp;
                StmtExpParser stmtExpParser = new StmtExpParser(iterator);
                stmtEle = stmtExpParser.parseStmtExp();
                break;
            case SEMICN:        // ;
                StmtEmptyParser stmtEmptyParser = new StmtEmptyParser(iterator);
                stmtEle = stmtEmptyParser.parseStmtEmpty();
                break;
            case LBRACE:        // Block
                BlockParser blockParser = new BlockParser(iterator);
                stmtEle = blockParser.parseBlock();
                break;
            case IFTK:          // if
                StmtIfParser stmtIfParser = new StmtIfParser(iterator);
                stmtEle = stmtIfParser.parseStmtIf();
                break;
            case FORTK:         // for
                StmtForParser stmtForParser = new StmtForParser(iterator);
                stmtEle = stmtForParser.parseStmtFor();
                break;
            case BREAKTK:       // break
                StmtBreakParser stmtBreakParser = new StmtBreakParser(iterator);
                stmtEle = stmtBreakParser.parseStmtBreak();
                break;
            case CONTINUETK:    // continue
                StmtContinueParser stmtContinueParser = new StmtContinueParser(iterator);
                stmtEle = stmtContinueParser.parseStmtContinue();
                break;
            case RETURNTK:      // return
                StmtReturnParser stmtReturnParser = new StmtReturnParser(iterator);
                stmtEle = stmtReturnParser.parseStmtReturn();
                break;
            case PRINTFTK:      // printf
                StmtPrintParser stmtPrintParser = new StmtPrintParser(iterator);
                stmtEle = stmtPrintParser.parseStmtPrint();
                break;
            default:
                System.out.print(token.toString());
                System.out.println(token.getLine());
                System.out.println("STMTPARSER ARRIVE UNEXPECTED BRANCH\n");
        }
        Stmt stmt = new Stmt(stmtEle);
        return stmt;
    }

    private void caseIdenfr() {
        boolean isAssign = false;
        int mode = 0;       // 0:exp 1:getint 2:getchar
        Token token = iterator.getNextToken();
        int cnt = 1;
        while (iterator.hasNext() && !token.getType().equals(Token.Type.SEMICN)) {
            token = iterator.getNextToken();
            cnt += 1;
            if (token.getType().equals(Token.Type.ASSIGN)) {
                isAssign = true;
                token = iterator.getNextToken();
                cnt += 1;
                if (token.getType().equals(Token.Type.GETINTTK)) {
                    mode = 1;
                } else if (token.getType().equals(Token.Type.GETCHARTK)) {
                    mode = 2;
                }
                break;
            }
        }
        iterator.traceBack(cnt);

        if (isAssign) {
            if (mode == 0) {
                StmtAssignParser stmtAssignParser = new StmtAssignParser(iterator);
                stmtEle = stmtAssignParser.parseStmtAssign();
            } else if (mode == 1) {
                StmtGetIntParser stmtGetIntParser = new StmtGetIntParser(iterator);
                stmtEle = stmtGetIntParser.parseStmtGetInt();
            } else if (mode == 2) {
                StmtGetCharParser stmtGetCharParser = new StmtGetCharParser(iterator);
                stmtEle = stmtGetCharParser.parseStmtGetChar();
            }
        } else {
            StmtExpParser stmtExpParser = new StmtExpParser(iterator);
            stmtEle = stmtExpParser.parseStmtExp();
        }
    }

}
