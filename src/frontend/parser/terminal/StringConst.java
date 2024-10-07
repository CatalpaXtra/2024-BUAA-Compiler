package frontend.parser.terminal;

import frontend.lexer.Token;
import frontend.parser.declaration.constDecl.constInitVal.ConstInitValEle;
import frontend.parser.declaration.varDecl.initVal.InitValEle;

public class StringConst implements ConstInitValEle, InitValEle {
    private Token token;

    public StringConst(Token token) {
        this.token = token;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(token.toString());
        return sb.toString();
    }
}
