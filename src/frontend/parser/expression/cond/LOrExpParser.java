package frontend.parser.expression.cond;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

import java.util.ArrayList;

public class LOrExpParser {
    private final TokenIterator iterator;
    private ArrayList<LAndExp> lowerExps;
    private ArrayList<Token> operators;

    public LOrExpParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public LOrExp parseLOrExp() {
        lowerExps = new ArrayList<>();
        operators = new ArrayList<>();

        LAndExpParser lAndExpParser = new LAndExpParser(iterator);
        lowerExps.add(lAndExpParser.parseLAndExp());
        Token token = iterator.getNextToken();
        while (token.getType().equals(Token.Type.OR)) {
            operators.add(token);
            lowerExps.add(lAndExpParser.parseLAndExp());
            token = iterator.getNextToken();
        }
        iterator.traceBack(1);
        LOrExp lOrExp = new LOrExp(lowerExps, operators);
        return lOrExp;
    }
}
