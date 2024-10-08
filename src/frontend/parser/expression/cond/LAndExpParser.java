package frontend.parser.expression.cond;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

import java.util.ArrayList;

public class LAndExpParser {
    private final TokenIterator iterator;
    private ArrayList<EqExp> lowerExps;
    private ArrayList<Token> operators;

    public LAndExpParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public LAndExp parseLAndExp() {
        lowerExps = new ArrayList<>();
        operators = new ArrayList<>();

        EqExpParser eqExpParser = new EqExpParser(iterator);
        lowerExps.add(eqExpParser.parseEqExp());
        Token token = iterator.getNextToken();
        while (token.getType().equals(Token.Type.AND)) {
            operators.add(token);
            lowerExps.add(eqExpParser.parseEqExp());
            token = iterator.getNextToken();
        }
        iterator.traceBack(1);
        LAndExp lAndExp = new LAndExp(lowerExps, operators);
        return lAndExp;
    }
}
