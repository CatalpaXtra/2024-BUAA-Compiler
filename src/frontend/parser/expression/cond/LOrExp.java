package frontend.parser.expression.cond;

import frontend.lexer.Token;

import java.util.ArrayList;

public class LOrExp extends BaseExp {
    public LOrExp(ArrayList<LAndExp> lowerExps, ArrayList<Token> operators) {
        super("<LOrExp>", lowerExps, operators);
    }
}
