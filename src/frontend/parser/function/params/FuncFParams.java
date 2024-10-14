package frontend.parser.function.params;

import frontend.lexer.Token;

import java.util.ArrayList;

public class FuncFParams {
    private final String name = "<FuncFParams>";
    private final ArrayList<FuncFParam> funcFParamList;
    private final ArrayList<Token> commas;

    public FuncFParams(ArrayList<FuncFParam> funcFParamList, ArrayList<Token> commas) {
        this.funcFParamList = funcFParamList;
        this.commas = commas;
    }

    public ArrayList<FuncFParam> getFuncFParamList() {
        return funcFParamList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(funcFParamList.get(0).toString());
        int len = commas.size();
        for (int i = 0; i < len; i++) {
            sb.append(commas.get(i).toString());
            sb.append(funcFParamList.get(i + 1).toString());
        }
        sb.append(name + "\n");
        return sb.toString();
    }
}
