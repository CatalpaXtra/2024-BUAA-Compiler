package frontend.parser.expression.unary;

import frontend.lexer.Token;
import frontend.parser.terminal.Ident;

public class UnaryFunc implements UnaryEle {
    private final Ident ident;
    private final Token lParent;
    private final FuncRParams funcRParams;
    private final Token rParent;

    public UnaryFunc(Ident ident, Token lParent, FuncRParams funcRParams, Token rParent) {
        this.ident = ident;
        this.lParent = lParent;
        this.funcRParams = funcRParams;
        this.rParent = rParent;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident.toString());
        sb.append(lParent.toString());
        if (funcRParams != null) {
            sb.append(funcRParams.toString());
        }
        sb.append(rParent.toString());
        return sb.toString();
    }
}
