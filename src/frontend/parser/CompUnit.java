package frontend.parser;

import frontend.parser.declaration.Decl;
import frontend.parser.function.FuncDef;

import java.util.ArrayList;

public class CompUnit {
    private final String name = "<CompUnit>";
    private final ArrayList<Decl> decls;
    private final ArrayList<FuncDef> funcDefs;

    public CompUnit(ArrayList<Decl> decls, ArrayList<FuncDef> funcDefs) {
        this.decls = decls;
        this.funcDefs = funcDefs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Decl decl : decls) {
            sb.append(decl.toString());
        }
        for (FuncDef funcDef : funcDefs) {
            sb.append(funcDef.toString());
        }
        sb.append(name + "\n");
        return sb.toString();
    }
}
