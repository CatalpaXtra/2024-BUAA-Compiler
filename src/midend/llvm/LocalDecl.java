package midend.llvm;

import frontend.lexer.Token;
import frontend.parser.declaration.Decl;
import frontend.parser.declaration.DeclEle;
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

    public static void setLocalDecl(SymbolTable symbolTable, Module md) {
        globalSymbolTable = symbolTable;
        module = md;
    }

    public static void visitDecl(Decl decl, SymbolTable symbolTable) {
        DeclEle declEle = decl.getDeclEle();
        if (declEle instanceof ConstDecl) {
            visitConstDecl((ConstDecl) declEle, symbolTable);
        } else {
            visitVarDecl((VarDecl) declEle, symbolTable);
        }
    }

    private static void visitConstDecl(ConstDecl constDecl, SymbolTable symbolTable) {
        String type = constDecl.getBType().identifyType();
        ArrayList<ConstDef> constDefs = constDecl.getConstDefs();
        for (ConstDef constDef : constDefs) {
            visitConstDef(constDef, type, symbolTable);
        }
    }

    private static void visitVarDecl(VarDecl varDecl, SymbolTable symbolTable) {
        String type = varDecl.getBType().identifyType();
        ArrayList<VarDef> varDefs = varDecl.getVarDefs();
        for (VarDef varDef : varDefs) {
            visitVarDef(varDef, type, symbolTable);
        }
    }

    private static void assignIntArray(RetValue memoryReg, int size, ArrayList<Integer> initVal) {
        RetValue lastReg = memoryReg;
        for (int i = 0; i < initVal.size(); i++) {
            RetValue thisReg = new RetValue(Register.allocReg(), 1);
            if (i == 0) {
                module.addCode(thisReg.irOut() + " = getelementptr inbounds [" + size + " x i32], [" + size + " x i32]* " + lastReg.irOut() +", i32 0, i32 0");
            } else {
                module.addCode(thisReg.irOut() + " = getelementptr inbounds i32, i32* " + lastReg.irOut() + ", i32 1");
            }
            module.addCode("store i32 " + initVal.get(i) + ", i32* " + thisReg.irOut());
            lastReg = thisReg;
        }
    }

    private static void assignCharArray(RetValue memoryReg, int size, String initVal) {
        initVal = initVal.substring(1, initVal.length() - 1) + "\0";
        RetValue lastReg = memoryReg;
        for (int i = 0; i < initVal.length(); i++) {
            RetValue thisReg = new RetValue(Register.allocReg(), 1);
            if (i == 0) {
                module.addCode(thisReg.irOut() + " = getelementptr inbounds [" + size + " x i8], [" + size + " x i8]* " + lastReg.irOut() +", i8 0, i8 0");
            } else {
                module.addCode(thisReg.irOut() + " = getelementptr inbounds i8, i8* " + lastReg.irOut() + ", i8 1");
            }
            int value = initVal.charAt(i);
            module.addCode("store i8 " + value + ", i8* " + thisReg.irOut());
            lastReg = thisReg;
        }
    }

    private static void visitConstDef(ConstDef constDef, String type, SymbolTable symbolTable) {
        String symbolType = "Const" + type;
        String llvmType = Support.varTransfer(type);
        String name = constDef.getIdent().getIdenfr();
        int line = constDef.getIdent().getLine();
        if (constDef.isArray()) {
            symbolType += "Array";
            int size = GlobalDecl.visitGlobalConstExp(constDef.getConstExp(), symbolTable);
            RetValue memoryReg = new RetValue(Register.allocReg(), 1);
            module.addCode(memoryReg.irOut() + " = alloca [" + size + " x " + llvmType + "]");

            ConstInitValEle constInitValEle = constDef.getConstInitVal().getConstInitValEle();
            if (constInitValEle instanceof ConstExpSet) {
                ArrayList<Integer> initVal = GlobalDecl.visitGlobalConstExpSet((ConstExpSet) constInitValEle, symbolTable);
                assignIntArray(memoryReg, size, initVal);
                SymbolCon symbolCon = new SymbolCon(symbolType, name, line, "%" + memoryReg, initVal, size);
                symbolTable.addSymbol(symbolCon);
            } else if (constInitValEle instanceof StringConst) {
                String initVal = ((StringConst) constInitValEle).getToken().getContent();
                assignCharArray(memoryReg, size, initVal);
                SymbolCon symbolCon = new SymbolCon(symbolType, name, line, "%" + memoryReg, initVal, size);
                symbolTable.addSymbol(symbolCon);
            }
        } else {
            RetValue memoryReg = new RetValue(Register.allocReg(), 1);
            module.addCode(memoryReg.irOut() + " = alloca " + llvmType);
            int initVal = GlobalDecl.visitGlobalConstExp((ConstExp) constDef.getConstInitVal().getConstInitValEle(), symbolTable);
            module.addCode("store " + llvmType + " " + initVal + ", " + llvmType + "* %" + memoryReg);
            SymbolCon symbolCon = new SymbolCon(symbolType, name, line, memoryReg.irOut(), initVal);
            symbolTable.addSymbol(symbolCon);
        }
    }

    private static void assignVarIntArray(RetValue memoryReg, int size, ArrayList<RetValue> initVal) {
        RetValue lastReg = memoryReg;
        for (int i = 0; i < initVal.size(); i++) {
            RetValue thisReg = new RetValue(Register.allocReg(), 1);
            if (i == 0) {
                module.addCode(thisReg.irOut() + " = getelementptr inbounds [" + size + " x i32], [" + size + " x i32]* " + lastReg.irOut() +", i32 0, i32 0");
            } else {
                module.addCode(thisReg.irOut() + " = getelementptr inbounds i32, i32* " + lastReg.irOut() + ", i32 1");
            }
            module.addCode("store i32 " + initVal.get(i).irOut() + ", i32* " + thisReg.irOut());
            lastReg = thisReg;
        }
    }

    private static void visitVarDef(VarDef varDef, String type, SymbolTable symbolTable) {
        String symbolType = type;
        String llvmType = Support.varTransfer(type);
        String name = varDef.getIdent().getIdenfr();
        int line = varDef.getIdent().getLine();
        if (varDef.isArray()) {
            symbolType += "Array";
            int size = GlobalDecl.visitGlobalConstExp(varDef.getConstExp(), symbolTable);
            RetValue memoryReg = new RetValue(Register.allocReg(), 1);
            module.addCode(memoryReg.irOut() + " = alloca [" + size + " x " + llvmType + "]");

            if (varDef.hasInitValue()) {
                InitValEle initValEle = varDef.getInitVal().getInitValEle();
                if (initValEle instanceof ExpSet) {
                    ArrayList<RetValue> initVal = visitExpSet((ExpSet) initValEle, symbolTable);
                    assignVarIntArray(memoryReg, size, initVal);
                    SymbolVar symbolVar = new SymbolVar(symbolType, name, line, memoryReg.irOut(), new ArrayList<>(), size);
                    symbolTable.addSymbol(symbolVar);
                } else if (initValEle instanceof StringConst) {
                    String initVal = ((StringConst) initValEle).getToken().getContent();
                    assignCharArray(memoryReg, size, initVal);
                    SymbolVar symbolVar = new SymbolVar(symbolType, name, line, memoryReg.irOut(), initVal, size);
                    symbolTable.addSymbol(symbolVar);
                }
            }
        } else {
            RetValue memoryReg = new RetValue(Register.allocReg(), 1);
            module.addCode(memoryReg.irOut() + " = alloca " + llvmType);
            if (varDef.hasInitValue()) {
                RetValue result = visitExp((Exp) varDef.getInitVal().getInitValEle(), symbolTable);
                module.addCode("store " + llvmType + " " + result.irOut() + ", " + llvmType + "* " + memoryReg.irOut());
            }
            SymbolVar symbolVar = new SymbolVar(symbolType, name, line, memoryReg.irOut());
            symbolTable.addSymbol(symbolVar);
        }
    }

    private static ArrayList<RetValue> visitExpSet(ExpSet expSet, SymbolTable symbolTable) {
        ArrayList<Exp> exps = expSet.getExps();
        ArrayList<RetValue> results = new ArrayList<>();
        for (Exp exp : exps) {
            RetValue result = visitExp(exp, symbolTable);
            results.add(result);
        }
        return results;
    }

    public static RetValue visitExp(Exp exp, SymbolTable symbolTable) {
        return visitAddExp(exp.getAddExp(), symbolTable);
    }

    public static RetValue visitAddExp(AddExp addExp, SymbolTable symbolTable) {
        ArrayList<MulExp> mulExps = addExp.getLowerExps();
        RetValue result = visitMulExp(mulExps.get(0), symbolTable);
        ArrayList<Token> operators = addExp.getOperators();
        for (int i = 1; i < mulExps.size(); i++) {
            RetValue left = result;
            RetValue right = visitMulExp(mulExps.get(i), symbolTable);
            result = new RetValue(Register.allocReg(), 1);
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
            result = new RetValue(Register.allocReg(), 1);
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
        return new RetValue(0, 0);
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
                // TODO
                // if result is array

                String llvmType = Support.varTransfer(fParams.get(i).getSymbolType());
                passRParam += llvmType + " " + result.irOut() + ", ";
            }
        }
        passRParam = passRParam.length() > 2 ? passRParam.substring(0, passRParam.length() - 2) : passRParam;

        RetValue result = new RetValue(Register.allocReg(), 1);
        if (symbolFunc.getSymbolType().contains("Void")) {
            Register.cancelReg();
            module.addCode("call void @" + funcName + "(" + passRParam + ")");
        } else {
            String llvmType = Support.varTransfer(symbolFunc.getSymbolType());
            module.addCode(result.irOut() + " = call " + llvmType + " @" + funcName + "(" + passRParam + ")");
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
                return new RetValue(-retValue.getValue(), 0);
            } else {
                RetValue result = new RetValue(Register.allocReg(), 1);
                module.addCode(result.irOut() + " = sub i32 0, " + retValue.irOut());
                return result;
            }
        }
        return new RetValue(0, 0);
    }

    private static RetValue visitPrimaryExp(PrimaryExp primaryExp, SymbolTable symbolTable) {
        PrimaryEle primaryEle = primaryExp.getPrimaryEle();
        if (primaryEle instanceof ExpInParent) {
            return visitExp(((ExpInParent) primaryEle).getExp(), symbolTable);
        } else if (primaryEle instanceof LVal) {
            return visitLVal((LVal) primaryEle, symbolTable);
        } else if (primaryEle instanceof frontend.parser.expression.primary.Number) {
            return new RetValue(((frontend.parser.expression.primary.Number) primaryEle).getIntConst().getVal(), 0);
        } else if (primaryEle instanceof frontend.parser.expression.primary.Character) {
            return new RetValue(((Character) primaryEle).getCharConst().getVal(), 0);
        }
        return new RetValue(0, 0);
    }

    public static RetValue visitLVal(LVal lVal, SymbolTable symbolTable) {
        Ident ident = lVal.getIdent();
        Symbol symbol = symbolTable.getSymbol(ident.getIdenfr());
        String memory = symbol.getMemory();
        String llvmType = Support.varTransfer(symbol.getSymbolType());
        if (lVal.isArray()) {
            int size = symbol.getArraySize();
            RetValue loc = visitExp(lVal.getExp(), symbolTable);
            RetValue temp = new RetValue(Register.allocReg(), 1);
            module.addCode(temp.irOut() + " = getelementptr inbounds [" + size + " x i32], [" + size + " x i32]* " + memory + ", i32 0, i32 " + loc.irOut());
            RetValue result = new RetValue(Register.allocReg(), 1);
            module.addCode(result.irOut() + " = load i32, i32* " + temp.irOut());
            return result;
        } else {
            RetValue result = new RetValue(Register.allocReg(), 1);
            module.addCode(result.irOut() + " = load i32, i32* " + memory);
            return result;
        }
    }
}
