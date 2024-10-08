package frontend.parser.expression.cond;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.expression.add.AddExp;
import frontend.parser.expression.add.AddExpParser;

import java.util.ArrayList;

public class RelExpParser {
    private final TokenIterator iterator;
    private ArrayList<AddExp> lowerExps;
    private ArrayList<Token> operators;

    public RelExpParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public RelExp parseRelExp() {
        lowerExps = new ArrayList<>();
        operators = new ArrayList<>();

        AddExpParser addExpParser = new AddExpParser(iterator);
        lowerExps.add(addExpParser.parseAddExp());
        Token token = iterator.getNextToken();
        while (token.getType().equals(Token.Type.LSS) || token.getType().equals(Token.Type.LEQ) ||
                token.getType().equals(Token.Type.GRE) || token.getType().equals(Token.Type.GEQ)) {
            operators.add(token);
            lowerExps.add(addExpParser.parseAddExp());
            token = iterator.getNextToken();
        }
        iterator.traceBack(1);
        RelExp relExp = new RelExp(lowerExps, operators);
        return relExp;
    }
}
