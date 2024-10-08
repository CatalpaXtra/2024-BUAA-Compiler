package frontend.parser.expression.cond;

import frontend.lexer.Token;

import java.util.ArrayList;

public class LAndExp extends BaseExp {
    public LAndExp(ArrayList<EqExp> lowerExps, ArrayList<Token> operators) {
        super("<LAndExp>", lowerExps, operators);
    }
}
