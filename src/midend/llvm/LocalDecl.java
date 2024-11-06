package midend.llvm;

import frontend.lexer.Token;
import frontend.parser.block.statement.stmtVariant.StmtAssign;
import frontend.parser.block.statement.stmtVariant.StmtGetChar;
import frontend.parser.block.statement.stmtVariant.StmtGetInt;
import frontend.parser.declaration.constDecl.ConstDecl;
import frontend.parser.declaration.constDecl.ConstDef;
import frontend.parser.declaration.constDecl.constInitVal.ConstExpSet;
import frontend.parser.declaration.constDecl.constInitVal.ConstInitValEle;
import frontend.parser.declaration.varDecl.VarDecl;
import frontend.parser.declaration.varDecl.VarDef;
import frontend.parser.declaration.varDecl.initVal.ExpSet;
import frontend.parser.declaration.varDecl.initVal.InitValEle;
import frontend.parser.expression.ConstExp;
import frontend.parser.expression.Exp;
import frontend.parser.expression.add.AddExp;
import frontend.parser.expression.add.MulExp;
import frontend.parser.expression.primary.Character;
import frontend.parser.expression.primary.ExpInParent;
import frontend.parser.expression.primary.LVal;
import frontend.parser.expression.primary.PrimaryEle;
import frontend.parser.expression.primary.PrimaryExp;
import frontend.parser.expression.unary.*;
import frontend.parser.terminal.Ident;
import frontend.parser.terminal.StringConst;
import midend.symbol.*;

import java.util.ArrayList;

public class LocalDecl {
    private static SymbolTable globalSymbolTable;
    private static Module module;
    private static int virtualReg;

    public static void setLocalDecl(SymbolTable symbolTable, Module md) {
        globalSymbolTable = symbolTable;
        module = md;
    }

    public static void resetReg(int initReg) {
        virtualReg = initReg;
    }

    public static int allocReg() {
        return virtualReg++;
    }

    public static void visitConstDecl(ConstDecl constDecl, SymbolTable symbolTable, int scope) {
        String type = constDecl.getBType().identifyType();
        ArrayList<ConstDef> constDefs = constDecl.getConstDefs();
        for (ConstDef constDef : constDefs) {
            visitConstDef(constDef, type, symbolTable, scope);
        }
    }

    public static void visitVarDecl(VarDecl varDecl, SymbolTable symbolTable, int scope) {
        String type = varDecl.getBType().identifyType();
        ArrayList<VarDef> varDefs = varDecl.getVarDefs();
        for (VarDef varDef : varDefs) {
            visitVarDef(varDef, type, symbolTable, scope);
        }
    }

    private static void visitConstDef(ConstDef constDef, String type, SymbolTable symbolTable, int scope) {
        String symbolType = "Const" + type;
        String name = constDef.getIdent().getIdenfr();
        int line = constDef.getIdent().getLine();
        if (constDef.isArray()) {
            symbolType += "Array";
            int size = GlobalDecl.visitGlobalConstExp(constDef.getConstExp(), symbolTable);
            ConstInitValEle constInitValEle = constDef.getConstInitVal().getConstInitValEle();

            // TODO
            if (constInitValEle instanceof ConstExpSet) {
                ArrayList<Integer> initVal = GlobalDecl.visitGlobalConstExpSet((ConstExpSet) constInitValEle, symbolTable);
            } else if (constInitValEle instanceof StringConst) {
                String initVal = ((StringConst) constInitValEle).getToken().getContent();
            }
        } else {
            int memoryReg = virtualReg++;
            module.addCode("%" + memoryReg + " = alloca i32");
            int initVal = GlobalDecl.visitGlobalConstExp((ConstExp) constDef.getConstInitVal().getConstInitValEle(), symbolTable);
            module.addCode("store i32 " + initVal + ", i32* %" + memoryReg);
            SymbolCon symbolCon = new SymbolCon(symbolType, name, line, scope, "%" + memoryReg, initVal);
            symbolTable.addSymbol(symbolCon);
        }
    }

    private static void visitVarDef(VarDef varDef, String type, SymbolTable symbolTable, int scope) {
        String symbolType = type;
        String name = varDef.getIdent().getIdenfr();
        int line = varDef.getIdent().getLine();
        if (varDef.isArray()) {
            symbolType += "Array";
            int size = GlobalDecl.visitGlobalConstExp(varDef.getConstExp(), symbolTable);
            InitValEle initValEle = varDef.getInitVal().getInitValEle();

            // TODO
            if (initValEle instanceof ExpSet) {
                ArrayList<Integer> initVal = visitExpSet((ExpSet) initValEle, symbolTable);
            } else if (initValEle instanceof StringConst) {
                String initVal = ((StringConst) initValEle).getToken().getContent();
            }
        } else {
            int memoryReg = virtualReg++;
            module.addCode("%" + memoryReg + " = alloca i32");
            if (varDef.hasInitValue()) {
                RetValue result = visitExp((Exp) varDef.getInitVal().getInitValEle(), symbolTable);
                module.addCode("store i32 " + result.irOut() + ", i32* %" + memoryReg);
            }
            SymbolVar symbolVar = new SymbolVar(symbolType, name, line, scope, "%" + memoryReg);
            symbolTable.addSymbol(symbolVar);
        }
    }

    private static ArrayList<Integer> visitExpSet(ExpSet expSet, SymbolTable symbolTable) {
        return null;
    }

    public static RetValue visitExp(Exp exp, SymbolTable symbolTable) {
        return visitAddExp(exp.getAddExp(), symbolTable);
    }

    private static RetValue visitAddExp(AddExp addExp, SymbolTable symbolTable) {
        ArrayList<MulExp> mulExps = addExp.getLowerExps();
        RetValue result = visitMulExp(mulExps.get(0), symbolTable);
        ArrayList<Token> operators = addExp.getOperators();
        for (int i = 1; i < mulExps.size(); i++) {
            RetValue left = result;
            RetValue right = visitMulExp(mulExps.get(i), symbolTable);
            result = new RetValue(virtualReg++, false);
            Token op = operators.get(i - 1);
            if (op.getType().equals(Token.Type.PLUS)) {
                module.addCode(result.irOut() + " = add i32 " + left.irOut() + ", " + right.irOut());
            } else if (op.getType().equals(Token.Type.MINU)) {
                module.addCode(result.irOut() + " = sub i32 " + left.irOut() + ", " + right.irOut());
            }
        }
        return result;
    }

    private static RetValue visitMulExp(MulExp mulExp, SymbolTable symbolTable) {
        ArrayList<UnaryExp> unaryExps = mulExp.getLowerExps();
        RetValue result = visitUnaryExp(unaryExps.get(0), symbolTable);
        ArrayList<Token> operators = mulExp.getOperators();
        for (int i = 1; i < unaryExps.size(); i++) {
            RetValue left = result;
            RetValue right = visitUnaryExp(unaryExps.get(i), symbolTable);
            result = new RetValue(virtualReg++, false);
            Token op = operators.get(i - 1);
            if (op.getType().equals(Token.Type.MULT)) {
                module.addCode(result.irOut() + " = mul i32 " + left.irOut() + ", " + right.irOut());
            } else if (op.getType().equals(Token.Type.DIV)) {
                module.addCode(result.irOut() + " = sdiv i32 " + left.irOut() + ", " + right.irOut());
            } else if (op.getType().equals(Token.Type.MOD)) {
                module.addCode(result.irOut() + " = srem i32 " + left.irOut() + ", " + right.irOut());
            }
        }
        return result;
    }

    private static RetValue visitUnaryExp(UnaryExp unaryExp, SymbolTable symbolTable) {
        UnaryEle unaryEle = unaryExp.getUnaryEle();
        if (unaryEle instanceof UnaryFunc) {
            return visitUnaryFunc((UnaryFunc) unaryEle, symbolTable);
        } else if (unaryEle instanceof UnaryOpExp) {
            return visitUnaryOpExp((UnaryOpExp) unaryEle, symbolTable);
        } else if (unaryEle instanceof PrimaryExp) {
            return visitPrimaryExp((PrimaryExp) unaryEle, symbolTable);
        }
        return new RetValue(0, true);
    }

    private static RetValue visitUnaryFunc(UnaryFunc unaryFunc, SymbolTable symbolTable) {
        String funcName = unaryFunc.getIdent().getIdenfr();
        SymbolFunc symbolFunc = (SymbolFunc) globalSymbolTable.getSymbol(funcName);
        ArrayList<Symbol> fParams = symbolFunc.getSymbols();
        FuncRParams funcRParams = unaryFunc.getFuncRParams();
        String passRParam = "";
        if (funcRParams != null) {
            ArrayList<Exp> funcExps = funcRParams.getExps();
            int len = funcExps.size();
            for (int i = 0; i < len; i++) {
                RetValue result = visitExp(funcExps.get(i), symbolTable);
                String type = fParams.get(i).getSymbolType().contains("Int") ? "i32" : "i8";
                passRParam += type + " " + result.irOut() + ", ";
            }
        }
        passRParam = passRParam.length() > 2 ? passRParam.substring(0, passRParam.length() - 2) : passRParam;

        RetValue result = new RetValue(virtualReg++, false);
        if (symbolFunc.getSymbolType().contains("Void")) {
            virtualReg--;
            module.addCode("call void @" + funcName + "(" + passRParam + ")");
        } else if (symbolFunc.getSymbolType().contains("Int")) {
            module.addCode(result.irOut() + " = call i32 @" + funcName + "(" + passRParam + ")");
        } else {
            module.addCode(result.irOut() + " = call i8 @" + funcName + "(" + passRParam + ")");
        }
        return result;
    }

    private static RetValue visitUnaryOpExp(UnaryOpExp unaryOpExp, SymbolTable symbolTable) {
        Token op = unaryOpExp.getUnaryOp().getToken();
        UnaryExp unaryExp = unaryOpExp.getUnaryExp();
        if (op.getType().equals(Token.Type.PLUS)) {
            return visitUnaryExp(unaryExp, symbolTable);
        } else if (op.getType().equals(Token.Type.MINU)) {
            RetValue retValue = visitUnaryExp(unaryExp, symbolTable);
            if (retValue.isDigit()) {
                return new RetValue(-retValue.getValue(), true);
            } else {
                RetValue result = new RetValue(virtualReg++, false);
                module.addCode(result.irOut() + " = sub i32 0, " + retValue.irOut());
                return result;
            }
        }
        return new RetValue(0, true);
    }

    private static RetValue visitPrimaryExp(PrimaryExp primaryExp, SymbolTable symbolTable) {
        PrimaryEle primaryEle = primaryExp.getPrimaryEle();
        if (primaryEle instanceof ExpInParent) {
            return visitExp(((ExpInParent) primaryEle).getExp(), symbolTable);
        } else if (primaryEle instanceof LVal) {
            return visitLVal((LVal) primaryEle, symbolTable);
        } else if (primaryEle instanceof frontend.parser.expression.primary.Number) {
            return new RetValue(((frontend.parser.expression.primary.Number) primaryEle).getIntConst().getVal(), true);
        } else if (primaryEle instanceof frontend.parser.expression.primary.Character) {
            return new RetValue(((Character) primaryEle).getCharConst().getVal(), true);
        }
        return new RetValue(0, true);
    }

    public static RetValue visitLVal(LVal lVal, SymbolTable symbolTable) {
        Ident ident = lVal.getIdent();
        if (lVal.isArray()) {
            Exp exp = lVal.getExp();
            visitExp(exp, symbolTable);
        } else {
            Symbol symbol = symbolTable.getSymbol(ident.getIdenfr());
            String memory = symbol.getMemory();
            RetValue result = new RetValue(virtualReg++, false);
            module.addCode(result.irOut() + " = load i32, i32* " + memory);
            return result;
        }
        return new RetValue(0, true);
    }

    public static void visitStmtAssign(StmtAssign stmtAssign, SymbolTable symbolTable) {
        Ident ident = stmtAssign.getlVal().getIdent();
        Symbol symbol = symbolTable.getSymbol(ident.getIdenfr());
        String memory = symbol.getMemory();
        RetValue result = LocalDecl.visitExp(stmtAssign.getExp(), symbolTable);
        module.addCode("store i32 " + result.irOut() + ", i32* " + memory);
    }

    public static void visitStmtGetInt(StmtGetInt stmtGetInt, SymbolTable symbolTable) {
        int memoryReg = virtualReg++;
        module.addCode("%" + memoryReg + " = call i32 @getint()");
        Ident ident = stmtGetInt.getlVal().getIdent();
        Symbol symbol = symbolTable.getSymbol(ident.getIdenfr());
        String memory = symbol.getMemory();
        module.addCode("store i32 %" + memoryReg + ", i32* " + memory);
    }

    public static void visitStmtGetChar(StmtGetChar stmtGetChar, SymbolTable symbolTable) {
        int memoryReg = virtualReg++;
        module.addCode("%" + memoryReg + " = call i32 @getchar()");
        Ident ident = stmtGetChar.getlVal().getIdent();
        Symbol symbol = symbolTable.getSymbol(ident.getIdenfr());
        String memory = symbol.getMemory();
        module.addCode("store i32 %" + memoryReg + ", i32* " + memory);
    }

}
