package frontend.parser.declaration.constDecl;

import frontend.lexer.Token;
import frontend.parser.declaration.BType;
import frontend.parser.declaration.DeclEle;

import java.util.ArrayList;

public class ConstDecl implements DeclEle {
    private final String name = "<ConstDecl>";
    private final Token constTk;
    private final BType btype;
    private final ArrayList<ConstDef> constDefs;
    private final ArrayList<Token> commas;
    private final Token semicolon;

    public ConstDecl(Token constTk, BType btype, ArrayList<ConstDef> constDefs, ArrayList<Token> commas, Token semicolon) {
        this.constTk = constTk;
        this.btype = btype;
        this.constDefs = constDefs;
        this.commas = commas;
        this.semicolon = semicolon;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(constTk.toString());
        sb.append(btype.toString());
        sb.append(constDefs.get(0).toString());
        for (int i = 0; i < commas.size(); i++) {
            sb.append(commas.get(i).toString());
            sb.append(constDefs.get(i + 1).toString());
        }
        sb.append(semicolon.toString());
        sb.append(name + "\n");
        return sb.toString();
    }
}
