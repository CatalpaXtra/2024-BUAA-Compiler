package frontend.parser.expression.cond;

import frontend.lexer.Token;

import java.util.ArrayList;

public class EqExp extends BaseExp {
    public EqExp(ArrayList<RelExp> lowerExps, ArrayList<Token> operators) {
        super("<EqExp>", lowerExps, operators);
    }
}
