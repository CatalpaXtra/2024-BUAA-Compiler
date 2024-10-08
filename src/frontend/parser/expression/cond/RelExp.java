package frontend.parser.expression.cond;

import frontend.lexer.Token;
import frontend.parser.expression.add.AddExp;

import java.util.ArrayList;

public class RelExp extends BaseExp {
    public RelExp(ArrayList<AddExp> lowerExps, ArrayList<Token> operators) {
        super("<RelExp>", lowerExps, operators);
    }
}
