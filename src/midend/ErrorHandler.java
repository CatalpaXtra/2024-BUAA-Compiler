package midend;

import frontend.Error;
import frontend.lexer.Token;
import frontend.parser.block.BlockItem;
import frontend.parser.block.BlockItemEle;
import frontend.parser.block.statement.Stmt;
import frontend.parser.block.statement.StmtEle;
import frontend.parser.block.statement.stmtVariant.StmtBreak;
import frontend.parser.block.statement.stmtVariant.StmtContinue;
import frontend.parser.block.statement.stmtVariant.StmtPrint;
import frontend.parser.block.statement.stmtVariant.StmtReturn;
import frontend.parser.declaration.Decl;
import midend.symbol.Symbol;

import java.util.ArrayList;
import java.util.HashMap;

public class ErrorHandler {
    public static boolean handleErrorB(Symbol symbol, HashMap<String, Symbol> symbols) {
        if (symbols.containsKey(symbol.getName())) {
            Error error = new Error(Error.Type.b, "b", symbol.getLine());
            SemanticErrors.addError(error);
            return true;
        }
        return false;
    }

    public static void handleErrorF(Token.Type type, StmtEle stmtEle) {
        if (type.equals(Token.Type.VOIDTK) && (stmtEle instanceof StmtReturn) &&
                ((StmtReturn) stmtEle).existReturnValue()) {
            Error error = new Error(Error.Type.f, "f", ((StmtReturn) stmtEle).getLineNum());
            SemanticErrors.addError(error);
        }
    }

    public static void handleErrorG(Token.Type type, ArrayList<BlockItem> blockItems, int line) {
        if (type.equals(Token.Type.VOIDTK)) {
            return;
        }
        if (blockItems.isEmpty()) {
            Error error = new Error(Error.Type.g, "g", line);
            SemanticErrors.addError(error);
            return;
        }

        BlockItem lastItem = blockItems.get(blockItems.size() - 1);
        BlockItemEle blockItemEle = lastItem.getBlockItemEle();
        if (blockItemEle instanceof Decl || !(((Stmt) blockItemEle).getStmtEle() instanceof StmtReturn)) {
            Error error = new Error(Error.Type.g, "g", line);
            SemanticErrors.addError(error);
        }
    }

    public static void handleErrorL(StmtPrint stmtPrint) {
        String s = stmtPrint.getStringConst().getToken().getContent();
        int expNum = stmtPrint.getExpNum(), symNum = 0;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (s.charAt(i) == '%' && (i + 1) < len) {
                if (s.charAt(i + 1) == 'd' || s.charAt(i + 1) == 'c') {
                    symNum++;
                    i++;
                }
            }
        }
        if (symNum != expNum) {
            Error error = new Error(Error.Type.i, "i", stmtPrint.getLineNum());
            SemanticErrors.addError(error);
        }
    }

    public static void handleErrorM(boolean isInFor, StmtEle stmtEle) {
        if (!isInFor && stmtEle instanceof StmtBreak) {
            Error error = new Error(Error.Type.m, "m", ((StmtBreak) stmtEle).getLineNum());
            SemanticErrors.addError(error);
        } else if (!isInFor && stmtEle instanceof StmtContinue) {
            Error error = new Error(Error.Type.m, "m", ((StmtContinue) stmtEle).getLineNum());
            SemanticErrors.addError(error);
        }
    }
}
