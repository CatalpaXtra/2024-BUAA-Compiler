package frontend.parser.expression.cond;

import frontend.lexer.Token;

import java.util.ArrayList;

public class BaseExp<T> {
    private final String name;
    private final ArrayList<T> lowerExps;
    private final ArrayList<Token> operators;

    public BaseExp(String name, ArrayList<T> lowerExps, ArrayList<Token> operators) {
        this.name = name;
        this.lowerExps = lowerExps;
        this.operators = operators;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lowerExps.get(0).toString());
        sb.append(name + "\n");
        int len = operators.size();
        for (int i = 0; i < len; i++) {
            sb.append(operators.get(i).toString());
            sb.append(lowerExps.get(i + 1).toString());
            sb.append(name + "\n");
        }
        return sb.toString();
    }
}
