package frontend.parser.declaration.constDecl.constInitVal;

import frontend.lexer.Token;
import frontend.parser.expression.ConstExp;

import java.util.ArrayList;

public class ConstExpSet implements ConstInitValEle {
    private final Token lBrace;
    private final ArrayList<ConstExp> constExps;
    private final ArrayList<Token> commas;
    private final Token rBrace;

    public ConstExpSet(Token lBrace, ArrayList<ConstExp> constExps, ArrayList<Token> commas, Token rBrace) {
        this.lBrace = lBrace;
        this.constExps = constExps;
        this.commas = commas;
        this.rBrace = rBrace;
    }

    public ArrayList<ConstExp> getConstExps() {
        return constExps;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lBrace.toString());
        if (!constExps.isEmpty()) {
            sb.append(constExps.get(0).toString());
        }
        int len = commas.size();
        for (int i = 0; i < len; i++) {
            sb.append(commas.get(i).toString());
            sb.append(constExps.get(i + 1).toString());
        }
        sb.append(rBrace.toString());
        return sb.toString();
    }
}
