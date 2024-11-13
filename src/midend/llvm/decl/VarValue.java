package midend.llvm.decl;

import frontend.lexer.Token;
import frontend.parser.expression.Exp;
import frontend.parser.expression.add.AddExp;
import frontend.parser.expression.add.MulExp;
import frontend.parser.expression.primary.Character;
import frontend.parser.expression.primary.ExpInParent;
import frontend.parser.expression.primary.LVal;
import frontend.parser.expression.primary.PrimaryEle;
import frontend.parser.expression.primary.PrimaryExp;
import frontend.parser.expression.unary.*;
import frontend.parser.function.params.FuncFParam;
import frontend.parser.terminal.Ident;
import midend.llvm.Module;
import midend.llvm.Register;
import midend.llvm.RetValue;
import midend.llvm.Support;
import midend.llvm.symbol.Symbol;
import midend.llvm.symbol.SymbolFunc;
import midend.llvm.symbol.SymbolTable;

import java.util.ArrayList;

public class VarValue {
    private static SymbolTable globalSymbolTable;
    private static Module module;

    public static void setVarValue(SymbolTable globalSymbolTable, Module module) {
        VarValue.module = module;
        VarValue.globalSymbolTable = globalSymbolTable;
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
                module.addInstrAdd(result, left, right);
            } else if (op.getType().equals(Token.Type.MINU)) {
                module.addInstrSub(result, left.irOut(), right);
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
                module.addInstrMul(result, left, right);
            } else if (op.getType().equals(Token.Type.DIV)) {
                module.addInstrSdiv(result, left, right);
            } else if (op.getType().equals(Token.Type.MOD)) {
                module.addInstrSrem(result, left, right);
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
            ArrayList<FuncFParam> funcFParams = symbolFunc.getFuncFParams();
            int len = funcExps.size();
            for (int i = 0; i < len; i++) {
                RetValue result = visitExp(funcExps.get(i), symbolTable);
                String llvmType = Support.varTransfer(fParams.get(i).getSymbolType());
                if (funcFParams.get(i).isArray()) {
                    llvmType += "*";
                }
                if (llvmType.equals("i8")) {
                    RetValue value = result;
                    result = new RetValue(Register.allocReg(), 1);
                    module.addInstrTrunc(result, "i32", value, "i8");
                }
                passRParam += llvmType + " " + result.irOut() + ", ";
            }
        }
        passRParam = passRParam.length() > 2 ? passRParam.substring(0, passRParam.length() - 2) : passRParam;

        if (symbolFunc.getSymbolType().contains("Void")) {
            module.addInstrCall(null, "void", funcName, passRParam);
            return null;
        } else {
            RetValue result = new RetValue(Register.allocReg(), 1);
            String llvmType = Support.varTransfer(symbolFunc.getSymbolType());
            module.addInstrCall(result, llvmType, funcName, passRParam);
            if (symbolFunc.isChar()) {
                RetValue value = result;
                result = new RetValue(Register.allocReg(), 1);
                module.addInstrZext(result, "i8", value, "i32");
            }
            return result;
        }
    }

    private static RetValue visitUnaryOpExp(UnaryOpExp unaryOpExp, SymbolTable symbolTable) {
        Token op = unaryOpExp.getUnaryOp().getToken();
        RetValue retValue = visitUnaryExp(unaryOpExp.getUnaryExp(), symbolTable);
        if (op.getType().equals(Token.Type.PLUS)) {
            return retValue;
        } else if (op.getType().equals(Token.Type.MINU)) {
            if (retValue.isDigit()) {
                return new RetValue(-retValue.getValue(), 0);
            } else {
                RetValue result = new RetValue(Register.allocReg(), 1);
                module.addInstrSub(result, "0", retValue);
                return result;
            }
        } else {
            /* Only Exist In Cond */
            if (retValue.isDigit()) {
                if (retValue.irOut().equals("0")) {
                    return new RetValue(1, 0);
                } else {
                    return new RetValue(0, 0);
                }
            } else {
                RetValue result = new RetValue(Register.allocReg(), 1);
                module.addInstrIcmp(result, "eq", retValue, "0");
                RetValue value = result;
                result = new RetValue(Register.allocReg(), 1);
                module.addInstrZext(result, "i1", value, "i32");
                return result;
            }
        }
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

    private static RetValue visitLVal(LVal lVal, SymbolTable symbolTable) {
        Ident ident = lVal.getIdent();
        Symbol symbol = symbolTable.getSymbol(ident.getIdenfr());
        String memory = symbol.getMemory();
        String llvmType = Support.varTransfer(symbol.getSymbolType());
        if (lVal.isArray()) {
            RetValue loc = visitExp(lVal.getExp(), symbolTable);
            RetValue result;
            if (symbol.isPointer()) {
                RetValue temp1 = new RetValue(Register.allocReg(), 1);
                module.addInstrLoad(temp1, llvmType + "*", memory);
                RetValue temp2 = new RetValue(Register.allocReg(), 1);
                module.addInstrGetelementptrPointer(temp2, llvmType, temp1.irOut(), loc.irOut());
                result = new RetValue(Register.allocReg(), 1);
                module.addInstrLoad(result, llvmType, temp2.irOut());
            } else {
                RetValue temp1 = new RetValue(Register.allocReg(), 1);
                module.addInstrGetelementptrArray(temp1, symbol.getArraySize(), llvmType, memory, loc.irOut());
                result = new RetValue(Register.allocReg(), 1);
                module.addInstrLoad(result, llvmType, temp1.irOut());
            }

            if (symbol.isChar()) {
                RetValue value = result;
                result = new RetValue(Register.allocReg(), 1);
                module.addInstrZext(result, "i8", value, "i32");
            }
            return result;
        } else {
            if (symbol.isArray()) {
                /* Exist In Call Func While Pass Param */
                /* int c[10]; a = func(c); */
                int size = symbol.getArraySize();
                RetValue result = new RetValue(Register.allocReg(), 1);
                module.addInstrGetelementptrArray(result, size, llvmType, memory, "0");
                return result;
            } else if (symbol.isPointer()) {
                /* Exist In Call Func While Pass Param */
                RetValue result = new RetValue(Register.allocReg(), 1);
                module.addInstrLoad(result, llvmType+"*", memory);
                return result;
            } else {
                RetValue result = new RetValue(Register.allocReg(), 1);
                module.addInstrLoad(result, llvmType, memory);
                if (symbol.isChar()) {
                    RetValue value = result;
                    result = new RetValue(Register.allocReg(), 1);
                    module.addInstrZext(result, "i8", value, "i32");
                }
                return result;
            }
        }
    }
}
