package frontend.parser.expression;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

import java.util.ArrayList;

public class FuncRParamsParser {
    private final TokenIterator iterator;
    private final ArrayList<Exp> exps;
    private final ArrayList<Token> commas;

    public FuncRParamsParser(TokenIterator iterator) {
        this.iterator = iterator;
        this.exps = new ArrayList<>();
        this.commas = new ArrayList<>();
    }

    public FuncRParams parseFuncRParams() {
        ExpParser expParser = new ExpParser(iterator);
        exps.add(expParser.parseExp());
        Token token = iterator.getNextToken();
        while (token.getType().equals(Token.Type.COMMA)) {
            commas.add(token);
            exps.add(expParser.parseExp());
            token = iterator.getNextToken();
        }
        iterator.traceBack(1);
        FuncRParams funcRParams = new FuncRParams(exps, commas);
        return funcRParams;
    }
}
