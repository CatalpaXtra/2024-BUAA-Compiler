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
import frontend.parser.terminal.Ident;
import midend.llvm.Constant;
import midend.llvm.Register;
import midend.llvm.Value;
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

    public static ArrayList<Value> visitExpSet(ExpSet expSet, SymbolTable symbolTable) {
        ArrayList<Exp> exps = expSet.getExps();
        ArrayList<Value> results = new ArrayList<>();
        for (Exp exp : exps) {
            Value result = visitExp(exp, symbolTable);
            results.add(result);
        }
        return results;
    }

    public static Value visitExp(Exp exp, SymbolTable symbolTable) {
        return visitAddExp(exp.getAddExp(), symbolTable);
    }

    public static Value visitAddExp(AddExp addExp, SymbolTable symbolTable) {
        ArrayList<MulExp> mulExps = addExp.getLowerExps();
        Value result = visitMulExp(mulExps.get(0), symbolTable);
        ArrayList<Token> operators = addExp.getOperators();
        for (int i = 1; i < mulExps.size(); i++) {
            Value left = result;
            Value right = visitMulExp(mulExps.get(i), symbolTable);
            result = new Value(Register.allocReg(), "i32");
            Token op = operators.get(i - 1);
            if (op.getType().equals(Token.Type.PLUS)) {
                irBlock.addInstrBinary(result, left, right, "add");
            } else if (op.getType().equals(Token.Type.MINU)) {
                irBlock.addInstrBinary(result, left, right, "sub");
            }
        }
        return result;
    }

    private static Value visitMulExp(MulExp mulExp, SymbolTable symbolTable) {
        ArrayList<UnaryExp> unaryExps = mulExp.getLowerExps();
        Value result = visitUnaryExp(unaryExps.get(0), symbolTable);
        ArrayList<Token> operators = mulExp.getOperators();
        for (int i = 1; i < unaryExps.size(); i++) {
            Value left = result;
            Value right = visitUnaryExp(unaryExps.get(i), symbolTable);
            result = new Value(Register.allocReg(), "i32");
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

    private static Value visitUnaryExp(UnaryExp unaryExp, SymbolTable symbolTable) {
        UnaryEle unaryEle = unaryExp.getUnaryEle();
        if (unaryEle instanceof UnaryFunc) {
            return visitUnaryFunc((UnaryFunc) unaryEle, symbolTable);
        } else if (unaryEle instanceof UnaryOpExp) {
            return visitUnaryOpExp((UnaryOpExp) unaryEle, symbolTable);
        } else if (unaryEle instanceof PrimaryExp) {
            return visitPrimaryExp((PrimaryExp) unaryEle, symbolTable);
        }
        return new Constant(0);
    }

    private static Value visitUnaryFunc(UnaryFunc unaryFunc, SymbolTable symbolTable) {
        String funcName = unaryFunc.getIdent().getIdenfr();
        Function function = (Function) globalSymbolTable.getSymbol(funcName);
        FuncRParams funcRParams = unaryFunc.getFuncRParams();
        String passRParam = "";
        if (funcRParams != null) {
            ArrayList<Exp> funcExps = funcRParams.getExps();
            ArrayList<Param> params = function.getParams();
            int len = funcExps.size();
            for (int i = 0; i < len; i++) {
                Value result = visitExp(funcExps.get(i), symbolTable);
                String irType = params.get(i).getIrType();
                if (irType.equals("i8")) {
                    if (Support.spareZext(irBlock.getLastInstr())) {
                        Register.cancelAlloc();
                        Register.cancelAlloc();
                        result = new Value(Register.allocReg(), "i8");
                        irBlock.delLastInstr();
                    } else {
                        Value value = result;
                        result = new Value(Register.allocReg(), "i32");
                        irBlock.addInstrTrunc(result, "i32", value, "i8");
                    }
                }
                passRParam += irType + " " + result.irOut() + ", ";
            }
        }
        passRParam = passRParam.length() > 2 ? passRParam.substring(0, passRParam.length() - 2) : passRParam;

        if (function.getIrType().contains("void")) {
            irBlock.addInstrCall(null, "void", funcName, passRParam);
            return null;
        } else {
            Value result = new Value(Register.allocReg(), "i32");
            String irType = function.getIrType();
            irBlock.addInstrCall(result, irType, funcName, passRParam);
            if (function.isChar()) {
                Value value = result;
                result = new Value(Register.allocReg(), "i32");
                irBlock.addInstrZext(result, "i8", value, "i32");
            }
            return result;
        }
    }

    private static Value visitUnaryOpExp(UnaryOpExp unaryOpExp, SymbolTable symbolTable) {
        Token op = unaryOpExp.getUnaryOp().getToken();
        Value retValue = visitUnaryExp(unaryOpExp.getUnaryExp(), symbolTable);
        if (op.getType().equals(Token.Type.PLUS)) {
            return retValue;
        } else if (op.getType().equals(Token.Type.MINU)) {
            if (retValue instanceof Constant) {
                Constant value = new Constant(-((Constant) retValue).getValue());
                return value;
            } else {
                Value result = new Value(Register.allocReg(), "i32");
                Constant zero = new Constant(0);
                irBlock.addInstrBinary(result, zero, retValue, "sub");
                return result;
            }
        } else {
            /* Only Exist In Cond */
            if (retValue instanceof Constant) {
                if (retValue.irOut().equals("0")) {
                    return new Constant(1);
                } else {
                    return new Constant(0);
                }
            } else {
                Value result = new Value(Register.allocReg(), "i32");
                Constant zero = new Constant(0);
                irBlock.addInstrIcmp(result, "eq", retValue, zero);
                Value value = result;
                result = new Value(Register.allocReg(), "i32");
                irBlock.addInstrZext(result, "i1", value, "i32");
                return result;
            }
        }
    }

    private static Value visitPrimaryExp(PrimaryExp primaryExp, SymbolTable symbolTable) {
        PrimaryEle primaryEle = primaryExp.getPrimaryEle();
        if (primaryEle instanceof ExpInParent) {
            return visitExp(((ExpInParent) primaryEle).getExp(), symbolTable);
        } else if (primaryEle instanceof LVal) {
            return visitLVal((LVal) primaryEle, symbolTable);
        } else if (primaryEle instanceof frontend.parser.expression.primary.Number) {
            return new Constant(((frontend.parser.expression.primary.Number) primaryEle).getIntConst().getVal());
        } else if (primaryEle instanceof frontend.parser.expression.primary.Character) {
            return new Constant(((Character) primaryEle).getCharConst().getVal());
        }
        return new Constant(0);
    }

    private static Value visitLVal(LVal lVal, SymbolTable symbolTable) {
        Ident ident = lVal.getIdent();
        Symbol symbol = symbolTable.getSymbol(ident.getIdenfr());
        Value memory = symbol.getMemory();
        String irType = Support.varTransfer(symbol.getSymbolType());
        if (lVal.isArray()) {
            Value loc = visitExp(lVal.getExp(), symbolTable);
            Value result;
            if (symbol.isPointer()) {
                Value temp1 = new Value(Register.allocReg(), "i32");
                irBlock.addInstrLoad(temp1, irType + "*", memory);
                Value temp2 = new Value(Register.allocReg(), "i32");
                irBlock.addInstrGetelementptr(temp2, -1, irType, temp1, loc);
                result = new Value(Register.allocReg(), "i32");
                irBlock.addInstrLoad(result, irType, temp2);
            } else {
                Value temp1 = new Value(Register.allocReg(), "i32");
                irBlock.addInstrGetelementptr(temp1, symbol.getArraySize(), irType, memory, loc);
                result = new Value(Register.allocReg(), "i32");
                irBlock.addInstrLoad(result, irType, temp1);
            }

            if (symbol.isChar()) {
                Value value = result;
                result = new Value(Register.allocReg(), "i32");
                irBlock.addInstrZext(result, "i8", value, "i32");
            }
            return result;
        } else {
            if (symbol.isArray()) {
                /* Exist In Call Func While Pass Param */
                /* int c[10]; a = func(c); */
                int size = symbol.getArraySize();
                Value result = new Value(Register.allocReg(), "i32");
                irBlock.addInstrGetelementptr(result, size, irType, memory, new Constant(0));
                return result;
            } else if (symbol.isPointer()) {
                /* Exist In Call Func While Pass Param */
                Value result = new Value(Register.allocReg(), "i32");
                irBlock.addInstrLoad(result, irType+"*", memory);
                return result;
            } else {
                Value result = new Value(Register.allocReg(), "i32");
                irBlock.addInstrLoad(result, irType, memory);
                if (symbol.isChar()) {
                    Value value = result;
                    result = new Value(Register.allocReg(), "i32");
                    irBlock.addInstrZext(result, "i8", value, "i32");
                }
                return result;
            }
        }
    }
}
