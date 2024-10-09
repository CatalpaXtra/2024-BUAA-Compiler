package frontend.parser.expression.add;

import frontend.lexer.Token;
import frontend.parser.expression.cond.BaseExp;

import java.util.ArrayList;

public class AddExp extends BaseExp {
    public AddExp(ArrayList<MulExp> lowerExps, ArrayList<Token> operators) {
        super("<AddExp>", lowerExps, operators);
    }
}
