package frontend.parser.expression.cond;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

import java.util.ArrayList;

public class EqExpParser {
    private final TokenIterator iterator;
    private ArrayList<RelExp> lowerExps;
    private ArrayList<Token> operators;

    public EqExpParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public EqExp parseEqExp() {
        lowerExps = new ArrayList<>();
        operators = new ArrayList<>();

        RelExpParser relExpParser = new RelExpParser(iterator);
        lowerExps.add(relExpParser.parseRelExp());
        Token token = iterator.getNextToken();
        while (token.getType().equals(Token.Type.EQL) || token.getType().equals(Token.Type.NEQ)) {
            operators.add(token);
            lowerExps.add(relExpParser.parseRelExp());
            token = iterator.getNextToken();
        }
        iterator.traceBack(1);
        EqExp eqExp = new EqExp(lowerExps, operators);
        return eqExp;
    }
}
