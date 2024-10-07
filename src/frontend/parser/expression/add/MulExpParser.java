package frontend.parser.expression.add;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.expression.unary.UnaryExp;
import frontend.parser.expression.unary.UnaryExpParser;

import java.util.ArrayList;

public class MulExpParser {
    private final TokenIterator iterator;
    private ArrayList<UnaryExp> unaryExps;
    private ArrayList<Token> operators; // '*' '/' '%'

    public MulExpParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public MulExp parseMulExp() {
        unaryExps = new ArrayList<>();
        operators = new ArrayList<>();

        UnaryExpParser unaryExpParser = new UnaryExpParser(iterator);
        unaryExps.add(unaryExpParser.parseUnaryExp());
        Token token = iterator.getNextToken();
        while (token.getType().equals(Token.Type.MULT) || token.getType().equals(Token.Type.DIV) || token.getType().equals(Token.Type.MOD)) {
            operators.add(token);
            unaryExps.add(unaryExpParser.parseUnaryExp());
            token = iterator.getNextToken();
        }
        iterator.traceBack(1);
        MulExp mulExp = new MulExp(unaryExps, operators);
        return mulExp;
    }
}
