package frontend.parser.function.params;

import frontend.lexer.Token;
import frontend.parser.declaration.BType;
import frontend.parser.terminal.Ident;

public class FuncFParam {
    private final String name = "<FuncFParam>";
    private final BType bType;
    private final Ident ident;
    private final Token lBracket;
    private final Token rBracket;

    public FuncFParam(BType bType, Ident ident, Token lBracket, Token rBracket) {
        this.bType = bType;
        this.ident = ident;
        this.lBracket = lBracket;
        this.rBracket = rBracket;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(bType.toString());
        sb.append(ident.toString());
        if (lBracket != null && rBracket != null) {
            sb.append(lBracket.toString());
            sb.append(rBracket.toString());
        }
        sb.append(name + "\n");
        return sb.toString();
    }

}
