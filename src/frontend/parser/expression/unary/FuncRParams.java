package frontend.parser.expression.unary;

import frontend.lexer.Token;
import frontend.parser.expression.Exp;

import java.util.ArrayList;

public class FuncRParams {
    private final String name = "<FuncRParams>";
    private final ArrayList<Exp> exps;
    private final ArrayList<Token> commas;

    public FuncRParams(ArrayList<Exp> exps, ArrayList<Token> commas) {
        this.exps = exps;
        this.commas = commas;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(exps.get(0).toString());
        int len = commas.size();
        for (int i = 0; i < len; i++) {
            sb.append(commas.get(i).toString());
            sb.append(exps.get(i + 1).toString());
        }
        sb.append(name + "\n");
        return sb.toString();
    }
}
