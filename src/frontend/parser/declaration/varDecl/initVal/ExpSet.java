package frontend.parser.declaration.varDecl.initVal;

import frontend.lexer.Token;
import frontend.parser.expression.Exp;

import java.util.ArrayList;

public class ExpSet implements InitValEle{
    private final Token lBrace;
    private final ArrayList<Exp> exps;
    private final ArrayList<Token> commas;
    private final Token rBrace;

    public ExpSet(Token lBrace, ArrayList<Exp> exps, ArrayList<Token> commas, Token rBrace) {
        this.lBrace = lBrace;
        this.exps = exps;
        this.commas = commas;
        this.rBrace = rBrace;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lBrace.toString());
        sb.append(exps.get(0).toString());
        int len = commas.size();
        for (int i = 0; i < len; i++) {
            sb.append(commas.get(i).toString());
            sb.append(exps.get(i + 1).toString());
        }
        sb.append(rBrace.toString());
        return sb.toString();
    }
}
