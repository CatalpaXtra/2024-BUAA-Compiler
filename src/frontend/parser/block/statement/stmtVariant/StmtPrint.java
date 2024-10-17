package frontend.parser.block.statement.stmtVariant;

import frontend.lexer.Token;
import frontend.parser.block.statement.StmtEle;
import frontend.parser.expression.Exp;
import frontend.parser.terminal.StringConst;

import java.util.ArrayList;

public class StmtPrint implements StmtEle {
    private final Token printf;
    private final Token lParent;
    private final StringConst stringConst;
    private final ArrayList<Token> commas;
    private final ArrayList<Exp> exps;
    private final Token rParent;
    private final Token semicolon;

    public StmtPrint(Token printf, Token lParent, StringConst stringConst, ArrayList<Token> commas, ArrayList<Exp> exps, Token rParent, Token semicolon) {
        this.printf = printf;
        this.lParent = lParent;
        this.stringConst = stringConst;
        this.commas = commas;
        this.exps = exps;
        this.rParent = rParent;
        this.semicolon = semicolon;
    }

    public int getLineNum() {
        return printf.getLine();
    }

    public StringConst getStringConst() {
        return stringConst;
    }

    public int getExpNum() {
        return exps.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(printf.toString());
        sb.append(lParent.toString());
        sb.append(stringConst.toString());
        int len = commas.size();
        for (int i = 0; i < len; i++) {
            sb.append(commas.get(i).toString());
            sb.append(exps.get(i).toString());
        }
        sb.append(rParent.toString());
        sb.append(semicolon.toString());
        return sb.toString();
    }
}
