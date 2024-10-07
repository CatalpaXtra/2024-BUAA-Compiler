package frontend.parser.expression.add;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

import java.util.ArrayList;

public class AddExpParser {
    private final TokenIterator iterator;
    private ArrayList<MulExp> mulExps;
    private ArrayList<Token> operators; // '+' '-'

    public AddExpParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public AddExp parseAddExp() {
        mulExps = new ArrayList<>();
        operators = new ArrayList<>();

        MulExpParser mulExpParser = new MulExpParser(iterator);
        mulExps.add(mulExpParser.parseMulExp());
        Token token = iterator.getNextToken();
        while (token.getType().equals(Token.Type.PLUS) || token.getType().equals(Token.Type.MINU)) {
            operators.add(token);
            mulExps.add(mulExpParser.parseMulExp());
            token = iterator.getNextToken();
        }
        iterator.traceBack(1);
        AddExp addExp = new AddExp(mulExps, operators);
        return addExp;
    }
}
