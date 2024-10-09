package frontend.parser.block.statement;

import frontend.ErrorHandler;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.expression.Exp;
import frontend.parser.expression.ExpParser;
import frontend.parser.terminal.StringConst;
import frontend.parser.terminal.StringConstParser;

import java.util.ArrayList;

public class StmtPrintParser {
    private final TokenIterator iterator;
    private Token printf;
    private Token lParent;
    private StringConst stringConst;
    private ArrayList<Token> commas;
    private ArrayList<Exp> exps;
    private Token rParent;
    private Token semicolon;

    public StmtPrintParser(TokenIterator iterator) {
        this.iterator = iterator;
        this.commas = new ArrayList<>();
        this.exps = new ArrayList<>();
    }

    public StmtPrint parseStmtPrint() {
        printf = iterator.getNextToken();
        lParent = iterator.getNextToken();
        StringConstParser stringConstParser = new StringConstParser(iterator);
        stringConst = stringConstParser.parseStringConst();

        Token token = iterator.getNextToken();
        while (token.getType().equals(Token.Type.COMMA)) {
            commas.add(token);
            ExpParser expParser = new ExpParser(iterator);
            exps.add(expParser.parseExp());
            token = iterator.getNextToken();
        }
        iterator.traceBack(1);
        ErrorHandler errorHandler = new ErrorHandler(iterator);
        rParent = errorHandler.handleErrorJ();
        semicolon = errorHandler.handleErrorI();
        StmtPrint stmtPrint = new StmtPrint(printf, lParent, stringConst, commas, exps, rParent, semicolon);
        return stmtPrint;
    }
}
