package frontend.parser.expression.add;

import frontend.lexer.Token;
import frontend.parser.expression.cond.BaseExp;
import frontend.parser.expression.unary.UnaryExp;

import java.util.ArrayList;

public class MulExp extends BaseExp {
    public MulExp(ArrayList<UnaryExp> lowerExps, ArrayList<Token> operators) { super("<MulExp>", lowerExps, operators); }
}
