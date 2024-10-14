package frontend.parser.declaration.constDecl;

import frontend.lexer.Token;
import frontend.parser.declaration.constDecl.constInitVal.ConstInitVal;
import frontend.parser.expression.ConstExp;
import frontend.parser.terminal.Ident;

public class ConstDef {
    private final String name = "<ConstDef>";
    private final Ident ident;
    private final Token lBracket;
    private final ConstExp constExp;
    private final Token rBracket;
    private final Token eq;
    private final ConstInitVal constInitVal;

    public ConstDef(Ident ident, Token lBracket, ConstExp constExp, Token rBracket, Token eq, ConstInitVal constInitVal) {
        this.ident = ident;
        this.lBracket = lBracket;
        this.constExp = constExp;
        this.rBracket = rBracket;
        this.eq = eq;
        this.constInitVal = constInitVal;
    }

    public Ident getIdent() {
        return ident;
    }

    public boolean isArray() {
        return lBracket != null && constExp != null && rBracket != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident.toString());
        if (lBracket != null && constExp != null && rBracket != null) {
            sb.append(lBracket.toString());
            sb.append(constExp.toString());
            sb.append(rBracket.toString());
        }
        sb.append(eq.toString());
        sb.append(constInitVal.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
