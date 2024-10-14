package frontend.parser.declaration.varDecl;

import frontend.lexer.Token;
import frontend.parser.declaration.BType;
import frontend.parser.declaration.DeclEle;

import java.util.ArrayList;

public class VarDecl implements DeclEle {
    private final String name = "<VarDecl>";
    private final BType btype;
    private final ArrayList<VarDef> varDefs;
    private final ArrayList<Token> commas;
    private final Token semicolon;

    public VarDecl(BType btype, ArrayList<VarDef> varDefs, ArrayList<Token> commas, Token semicolon) {
        this.btype = btype;
        this.varDefs = varDefs;
        this.commas = commas;
        this.semicolon = semicolon;
    }

    public BType getBType() {
        return btype;
    }

    public ArrayList<VarDef> getVarDefs() {
        return varDefs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(btype.toString());
        sb.append(varDefs.get(0).toString());
        for (int i = 0; i < commas.size(); i++) {
            sb.append(commas.get(i).toString());
            sb.append(varDefs.get(i + 1).toString());
        }
        sb.append(semicolon.toString());
        sb.append(name + "\n");
        return sb.toString();
    }

}
