package midend.llvm.visit;

import frontend.lexer.Token;
import frontend.parser.declaration.varDecl.initVal.ExpSet;
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
import midend.llvm.Register;
import midend.llvm.RetValue;
import midend.llvm.Support;
import midend.llvm.function.Function;
import midend.llvm.function.IrBlock;
import midend.llvm.function.Param;
import midend.llvm.symbol.Symbol;
import midend.llvm.symbol.SymbolTable;

import java.util.ArrayList;

public class VarValue {
    private static SymbolTable globalSymbolTable;
    private static IrBlock irBlock;

    public static void setVarValue(SymbolTable globalSymbolTable) {
        VarValue.globalSymbolTable = globalSymbolTable;
    }

    public static void setVarValueIrBlock(IrBlock irBlock) {
        VarValue.irBlock = irBlock;
    }

    public static ArrayList<RetValue> visitExpSet(ExpSet expSet, SymbolTable symbolTable) {
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
                irBlock.addInstrBinary(result, left, right, "add");
            } else if (op.getType().equals(Token.Type.MINU)) {
                irBlock.addInstrBinary(result, left, right, "sub");
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
                irBlock.addInstrBinary(result, left, right, "mul");
            } else if (op.getType().equals(Token.Type.DIV)) {
                irBlock.addInstrBinary(result, left, right, "sdiv");
            } else if (op.getType().equals(Token.Type.MOD)) {
                irBlock.addInstrBinary(result, left, right, "srem");
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
        Function function = (Function) globalSymbolTable.getSymbol(funcName);
        FuncRParams funcRParams = unaryFunc.getFuncRParams();
        String passRParam = "";
        if (funcRParams != null) {
            ArrayList<Exp> funcExps = funcRParams.getExps();
            ArrayList<Param> params = function.getParams();
            int len = funcExps.size();
            for (int i = 0; i < len; i++) {
                RetValue result = visitExp(funcExps.get(i), symbolTable);
                String irType = params.get(i).getIrType();
                if (irType.equals("i8")) {
                    RetValue value = result;
                    result = new RetValue(Register.allocReg(), 1);
                    irBlock.addInstrTrunc(result, "i32", value, "i8");
                }
                passRParam += irType + " " + result.irOut() + ", ";
            }
        }
        passRParam = passRParam.length() > 2 ? passRParam.substring(0, passRParam.length() - 2) : passRParam;

        if (function.getIrType().contains("void")) {
            irBlock.addInstrCall(null, "void", funcName, passRParam);
            return null;
        } else {
            RetValue result = new RetValue(Register.allocReg(), 1);
            String irType = function.getIrType();
            irBlock.addInstrCall(result, irType, funcName, passRParam);
            if (function.isChar()) {
                RetValue value = result;
                result = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrZext(result, "i8", value, "i32");
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
                RetValue zero = new RetValue(0, 0);
                irBlock.addInstrBinary(result, zero, retValue, "sub");
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
                RetValue zero = new RetValue(0, 0);
                irBlock.addInstrIcmp(result, "eq", retValue, zero);
                RetValue value = result;
                result = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrZext(result, "i1", value, "i32");
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
        String irType = Support.varTransfer(symbol.getSymbolType());
        if (lVal.isArray()) {
            RetValue loc = visitExp(lVal.getExp(), symbolTable);
            RetValue result;
            if (symbol.isPointer()) {
                RetValue temp1 = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrLoad(temp1, irType + "*", memory);
                RetValue temp2 = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrGetelementptr(temp2, -1, irType, temp1.irOut(), loc.irOut());
                result = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrLoad(result, irType, temp2.irOut());
            } else {
                RetValue temp1 = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrGetelementptr(temp1, symbol.getArraySize(), irType, memory, loc.irOut());
                result = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrLoad(result, irType, temp1.irOut());
            }

            if (symbol.isChar()) {
                RetValue value = result;
                result = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrZext(result, "i8", value, "i32");
            }
            return result;
        } else {
            if (symbol.isArray()) {
                /* Exist In Call Func While Pass Param */
                /* int c[10]; a = func(c); */
                int size = symbol.getArraySize();
                RetValue result = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrGetelementptr(result, size, irType, memory, "0");
                return result;
            } else if (symbol.isPointer()) {
                /* Exist In Call Func While Pass Param */
                RetValue result = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrLoad(result, irType+"*", memory);
                return result;
            } else {
                RetValue result = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrLoad(result, irType, memory);
                if (symbol.isChar()) {
                    RetValue value = result;
                    result = new RetValue(Register.allocReg(), 1);
                    irBlock.addInstrZext(result, "i8", value, "i32");
                }
                return result;
            }
        }
    }
}
