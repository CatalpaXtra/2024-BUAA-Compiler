package frontend.parser.declaration.varDecl;

import frontend.lexer.Token;
import frontend.parser.declaration.varDecl.initVal.InitVal;
import frontend.parser.expression.ConstExp;
import frontend.parser.terminal.Ident;

public class VarDef {
    private final String name = "<VarDef>";
    private final Ident ident;
    private final Token lBracket;
    private final ConstExp constExp;
    private final Token rBracket;
    private final Token eq;
    private final InitVal initVal;

    public VarDef(Ident ident, Token lBracket, ConstExp constExp, Token rBracket, Token eq, InitVal initVal) {
        this.ident = ident;
        this.lBracket = lBracket;
        this.constExp = constExp;
        this.rBracket = rBracket;
        this.eq = eq;
        this.initVal = initVal;
    }

    public Ident getIdent() {
        return ident;
    }

    public boolean isArray() {
        return lBracket != null && constExp != null && rBracket != null;
    }

    public ConstExp getConstExp() {
        return constExp;
    }

    public boolean hasInitValue() {
        return eq != null && initVal != null;
    }

    public InitVal getInitVal() {
        return initVal;
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
        if (eq != null && initVal != null) {
            sb.append(eq.toString());
            sb.append(initVal.toString());
        }
        sb.append(name + "\n");
        return sb.toString();
    }

}
