package midend.semantic;

import frontend.lexer.Error;
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
import frontend.parser.expression.Exp;
import frontend.parser.expression.add.AddExp;
import frontend.parser.expression.add.MulExp;
import frontend.parser.expression.primary.LVal;
import frontend.parser.expression.primary.PrimaryEle;
import frontend.parser.expression.primary.PrimaryExp;
import frontend.parser.expression.unary.FuncRParams;
import frontend.parser.expression.unary.UnaryEle;
import frontend.parser.expression.unary.UnaryExp;
import frontend.parser.terminal.Ident;
import midend.semantic.symbol.Symbol;
import midend.semantic.symbol.SymbolFunc;
import midend.semantic.symbol.SymbolTable;

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

    public static boolean handleErrorC(Ident ident, SymbolTable symbolTable) {
        if (symbolTable.getSymbol(ident.getIdenfr()) == null) {
            Error error = new Error(Error.Type.c, "c", ident.getLine());
            SemanticErrors.addError(error);
            return true;
        }
        return false;
    }

    public static boolean handleErrorD(FuncRParams funcRParams, Ident funcIdent, SymbolTable symbolTable) {
        int rParamsNum = 0;
        if (funcRParams != null) {
            ArrayList<Exp> funcExps = funcRParams.getExps();
            rParamsNum = funcExps.size();
        }

        SymbolFunc symbolFunc = (SymbolFunc) symbolTable.getSymbol(funcIdent.getIdenfr());
        if (symbolFunc.getSymbolNum() != rParamsNum) {
            Error error = new Error(Error.Type.d, "d", funcIdent.getLine());
            SemanticErrors.addError(error);
            return true;
        }
        return false;
    }

    private static String getExpType(Exp exp, SymbolTable symbolTable) {
        AddExp addExp = exp.getAddExp();
        ArrayList<MulExp> mulExps = addExp.getLowerExps();
        if (mulExps.size() != 1) {
            return "Var";
        }
        ArrayList<UnaryExp> unaryExps = mulExps.get(0).getLowerExps();
        if (unaryExps.size() != 1) {
            return "Var";
        }
        UnaryEle unaryEle = unaryExps.get(0).getUnaryEle();
        if (!(unaryEle instanceof PrimaryExp)) {
            return "Var";
        }
        PrimaryEle primaryEle = ((PrimaryExp) unaryEle).getPrimaryEle();
        if (!(primaryEle instanceof LVal)) {
            return "Var";
        }

        if (((LVal) primaryEle).isVarAsFuncRParam()) {
            return "Var";
        }
        Ident ident = ((LVal) primaryEle).getIdent();
        Symbol symbol = symbolTable.getSymbol(ident.getIdenfr());
        return symbol.getSymbolType().replace("Const", "");
    }

    public static boolean handleErrorE(FuncRParams funcRParams, Ident funcIdent, SymbolTable symbolTable) {
        SymbolFunc symbolFunc = (SymbolFunc) symbolTable.getSymbol(funcIdent.getIdenfr());
        ArrayList<Symbol> symbols = symbolFunc.getSymbols();
        ArrayList<Exp> funcExps = funcRParams == null ? null : funcRParams.getExps();
        for (int i = 0; i < symbols.size(); i++) {
            String symbolType = symbols.get(i).getSymbolType();
            String expType = getExpType(funcExps.get(i), symbolTable);
            if (expType.contains("Array")) {
                if (!expType.equals(symbolType)) {
                    Error error = new Error(Error.Type.e, "e", funcIdent.getLine());
                    SemanticErrors.addError(error);
                    return true;
                }
            } else {
                if (symbolType.contains("Array")) {
                    Error error = new Error(Error.Type.e, "e", funcIdent.getLine());
                    SemanticErrors.addError(error);
                    return true;
                }
            }
        }
        return false;
    }

    public static void handleErrorF(Token.Type type, StmtReturn stmtReturn) {
        if (type.equals(Token.Type.VOIDTK) &&  stmtReturn.existReturnValue()) {
            Error error = new Error(Error.Type.f, "f", stmtReturn.getLineNum());
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

    public static void handleErrorH(LVal lVal, SymbolTable symbolTable) {
        Ident ident = lVal.getIdent();
        Symbol symbol = symbolTable.getSymbol(ident.getIdenfr());
        if (symbol != null && symbol.getSymbolType().contains("Const")) {
            Error error = new Error(Error.Type.h, "h", ident.getLine());
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
            Error error = new Error(Error.Type.l, "l", stmtPrint.getLineNum());
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
