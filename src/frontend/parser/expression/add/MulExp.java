package frontend.parser.expression.add;

import frontend.lexer.Token;
import frontend.parser.expression.unary.UnaryExp;

import java.util.ArrayList;

public class MulExp {
    private final String name = "<MulExp>";
    private final ArrayList<UnaryExp> unaryExps;
    private final ArrayList<Token> operators; // '+' '-'

    public MulExp(ArrayList<UnaryExp> unaryExps, ArrayList<Token> operators) {
        this.unaryExps = unaryExps;
        this.operators = operators;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(unaryExps.get(0).toString());
        sb.append(name + "\n");
        int len = operators.size();
        for (int i = 0; i < len; i++) {
            sb.append(operators.get(i).toString());
            sb.append(unaryExps.get(i + 1).toString());
            sb.append(name + "\n");
        }
        return sb.toString();
    }
}
