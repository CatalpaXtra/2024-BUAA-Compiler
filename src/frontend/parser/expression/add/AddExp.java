package frontend.parser.expression.add;

import frontend.lexer.Token;

import java.util.ArrayList;

public class AddExp {
    private final String name = "<AddExp>";
    private final ArrayList<MulExp> mulExps;
    private final ArrayList<Token> operators; // '+' '-'

    public AddExp(ArrayList<MulExp> mulExps, ArrayList<Token> operators) {
        this.mulExps = mulExps;
        this.operators = operators;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mulExps.get(0).toString());
        sb.append(name + "\n");
        int len = operators.size();
        for (int i = 0; i < len; i++) {
            sb.append(operators.get(i).toString());
            sb.append(mulExps.get(i + 1).toString());
            sb.append(name + "\n");
        }
        return sb.toString();
    }
}
