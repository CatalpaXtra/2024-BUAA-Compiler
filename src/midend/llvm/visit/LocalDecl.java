package midend.llvm.visit;

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
import midend.llvm.Constant;
import midend.llvm.Register;
import midend.llvm.Value;
import midend.llvm.Support;
import midend.llvm.function.Function;
import midend.llvm.function.IrBlock;
import midend.llvm.function.Param;
import midend.llvm.global.GlobalBuilder;
import midend.llvm.global.GlobalVal;
import midend.llvm.global.initval.IrArray;
import midend.llvm.global.initval.InitVal;
import midend.llvm.global.initval.IrString;
import midend.llvm.global.initval.IrVar;
import midend.llvm.instr.IrInstr;
import midend.llvm.symbol.*;

import java.util.ArrayList;

public class LocalDecl {
    private static SymbolTable globalSymbolTable;
    private static IrBlock irBlock;

    public static void setLocalDecl(SymbolTable globalSymbolTable) {
        LocalDecl.globalSymbolTable = globalSymbolTable;
    }

    public static void setLocalDeclIrBlock(IrBlock irBlock) {
        LocalDecl.irBlock = irBlock;
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

    private static void initLocalIntArray(IrInstr irAlloca, int size, ArrayList<Integer> initVal, String irType) {
        IrInstr lastInstr = irAlloca;
        for (int i = 0; i < initVal.size(); i++) {
            IrInstr thisInstr;
            if (i == 0) {
                thisInstr = irBlock.addInstrGetelementptr("%"+Register.allocReg(), size, irType, lastInstr, new Constant(0));
            } else {
                thisInstr = irBlock.addInstrGetelementptr("%"+Register.allocReg(), -1, irType, lastInstr, new Constant(1));
            }
            irBlock.addInstrStore(irType, new Constant(initVal.get(i)), thisInstr);
            lastInstr = thisInstr;
        }
        if (irType.equals("i8")) {
            for (int i = initVal.size(); i < size; i++) {
                IrInstr thisInstr = irBlock.addInstrGetelementptr("%"+Register.allocReg(), -1, "i8", lastInstr, new Constant(1));
                irBlock.addInstrStore("i8", new Constant(0), thisInstr);
                lastInstr = thisInstr;
            }
        }
    }

    private static void initLocalCharArray(IrInstr irAlloca, int size, String initVal) {
        initVal = initVal.substring(1, initVal.length() - 1);
        IrInstr lastInstr = irAlloca;
        int len = 0;
        for (int i = 0; i < initVal.length(); i++) {
            IrInstr thisInstr;
            if (i == 0) {
                thisInstr =irBlock.addInstrGetelementptr("%"+Register.allocReg(), size, "i8", lastInstr, new Constant(0));
            } else {
                thisInstr =irBlock.addInstrGetelementptr("%"+Register.allocReg(), -1, "i8", lastInstr, new Constant(1));
            }

            int value = initVal.charAt(i);
            if (initVal.charAt(i) == '\\' && i + 1 < initVal.length()) {
                if (initVal.charAt(i + 1) == 'n') {
                    value = 10;
                    i++;
                }
            }
            irBlock.addInstrStore("i8", new Constant(value), thisInstr);
            lastInstr = thisInstr;
            len++;
        }
        for (int i = len; i < size; i++) {
            IrInstr thisInstr = irBlock.addInstrGetelementptr("%"+Register.allocReg(), -1, "i8", lastInstr, new Constant(1));
            irBlock.addInstrStore("i8", new Constant(0), thisInstr);
            lastInstr = thisInstr;
        }
    }

    private static void visitConstDef(ConstDef constDef, String type, SymbolTable symbolTable) {
        String irType = Support.varTransfer(type);
        String name = constDef.getIdent().getIdenfr();
        InitVal constant = null;
        int size = -1;
        IrInstr irAlloca;
        if (constDef.isArray()) {
            size = GlobalBuilder.visitConstExp(constDef.getConstExp(), symbolTable);
            irAlloca = irBlock.addInstrAlloca("%"+Register.allocReg(), irType, size);

            ConstInitValEle constInitValEle = constDef.getConstInitVal().getConstInitValEle();
            if (constInitValEle instanceof ConstExpSet) {
                ArrayList<Integer> initVal = GlobalBuilder.visitConstExpSet((ConstExpSet) constInitValEle, symbolTable);
                initLocalIntArray(irAlloca, size, initVal, irType);
                constant = new IrArray(irType, initVal, size);
            } else if (constInitValEle instanceof StringConst) {
                String initVal = ((StringConst) constInitValEle).getToken().getContent();
                initLocalCharArray(irAlloca, size, initVal);
                constant = new IrString(initVal, size);
            }
        } else {
            irAlloca = irBlock.addInstrAlloca("%"+Register.allocReg(), irType, size);
            int initVal = GlobalBuilder.visitConstExp((ConstExp) constDef.getConstInitVal().getConstInitValEle(), symbolTable);
            irBlock.addInstrStore(irType, new Constant(initVal), irAlloca);
            constant = new IrVar(initVal);
        }
        SymbolCon symbolCon = new SymbolCon(name, irType, irAlloca, constant, size);
        symbolTable.addSymbol(symbolCon);
    }

    private static void initLocalVarIntArray(IrInstr irAlloca, int size, ArrayList<Value> initVal, String irType) {
        IrInstr lastInstr = irAlloca;
        for (int i = 0; i < initVal.size(); i++) {
            IrInstr thisInstr;
            if (i == 0) {
                thisInstr = irBlock.addInstrGetelementptr("%"+Register.allocReg(), size, irType, lastInstr, new Constant(0));
            } else {
                thisInstr = irBlock.addInstrGetelementptr("%"+Register.allocReg(), -1, irType, lastInstr, new Constant(1));
            }
            if (!(initVal.get(i) instanceof Constant) && irType.equals("i8")) {
                IrInstr temp = irBlock.addInstrTrunc("%"+Register.allocReg(), "i32", initVal.get(i), "i8");
                irBlock.addInstrStore(irType, temp, thisInstr);
            } else {
                irBlock.addInstrStore(irType, initVal.get(i), thisInstr);
            }
            lastInstr = thisInstr;
        }
        if (irType.equals("i8")) {
            for (int i = initVal.size(); i < size; i++) {
                IrInstr thisInstr = irBlock.addInstrGetelementptr("%"+Register.allocReg(), -1, "i8", lastInstr, new Constant(1));
                irBlock.addInstrStore("i8", new Constant(0), thisInstr);
                lastInstr = thisInstr;
            }
        }
    }

    private static void visitVarDef(VarDef varDef, String type, SymbolTable symbolTable) {
        String irType = Support.varTransfer(type);
        String name = varDef.getIdent().getIdenfr();
        InitVal constant = null;
        int size = -1;
        IrInstr irAlloca;
        if (varDef.isArray()) {
            size = GlobalBuilder.visitConstExp(varDef.getConstExp(), symbolTable);
            irAlloca = irBlock.addInstrAlloca("%"+Register.allocReg(), irType, size);

            if (varDef.hasInitValue()) {
                InitValEle initValEle = varDef.getInitVal().getInitValEle();
                if (initValEle instanceof ExpSet) {
                    ArrayList<Value> initVal = visitExpSet((ExpSet) initValEle, symbolTable);
                    initLocalVarIntArray(irAlloca, size, initVal, irType);
                } else if (initValEle instanceof StringConst) {
                    String initVal = ((StringConst) initValEle).getToken().getContent();
                    initLocalCharArray(irAlloca, size, initVal);
                    constant = new IrString(initVal, size);
                }
            }
        } else {
            irAlloca = irBlock.addInstrAlloca("%"+Register.allocReg(), irType, size);
            if (varDef.hasInitValue()) {
                Value result = visitExp((Exp) varDef.getInitVal().getInitValEle(), symbolTable);
                if (!(result instanceof Constant) && irType.equals("i8")) {
                    if (Support.spareZext(irBlock.getLastInstr())) {
                        Register.cancelAlloc();
                        irBlock.delLastInstr();
                        result = irBlock.getLastInstr();
                    } else {
                        result = irBlock.addInstrTrunc("%"+Register.allocReg(), "i32", result, "i8");
                    }
                }
                irBlock.addInstrStore(irType, result, irAlloca);
                if (result instanceof Constant) {
                    constant = new IrVar(((Constant) result).getValue());
                }
            } else {
                constant = new IrVar(0);
            }
        }
        SymbolVar symbolVar = new SymbolVar(name, irType, irAlloca, size, constant);
        symbolTable.addSymbol(symbolVar);
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
            Token op = operators.get(i - 1);
            if (left instanceof Constant && right instanceof Constant) {
                if (op.getType().equals(Token.Type.PLUS)) {
                    result = new Constant(((Constant) left).getValue() + ((Constant) right).getValue());
                } else if (op.getType().equals(Token.Type.MINU)) {
                    result = new Constant(((Constant) left).getValue() - ((Constant) right).getValue());
                }
            } else {
                if (op.getType().equals(Token.Type.PLUS)) {
                    result = irBlock.addInstrBinary("%"+Register.allocReg(), left, right, "add");
                } else if (op.getType().equals(Token.Type.MINU)) {
                    result = irBlock.addInstrBinary("%"+Register.allocReg(), left, right, "sub");
                }
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
            Token op = operators.get(i - 1);
            if (left instanceof Constant && right instanceof Constant) {
                if (op.getType().equals(Token.Type.MULT)) {
                    result = new Constant(((Constant) left).getValue() * ((Constant) right).getValue());
                } else if (op.getType().equals(Token.Type.DIV)) {
                    result = new Constant(((Constant) left).getValue() / ((Constant) right).getValue());
                } else if (op.getType().equals(Token.Type.MOD)) {
                    result = new Constant(((Constant) left).getValue() % ((Constant) right).getValue());
                }
            } else {
                if (op.getType().equals(Token.Type.MULT)) {
                    result = irBlock.addInstrBinary("%"+Register.allocReg(), left, right, "mul");
                } else if (op.getType().equals(Token.Type.DIV)) {
                    result = irBlock.addInstrBinary("%"+Register.allocReg(), left, right, "sdiv");
                } else if (op.getType().equals(Token.Type.MOD)) {
                    result = irBlock.addInstrBinary("%"+Register.allocReg(), left, right, "srem");
                }
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
        ArrayList<Param> params = new ArrayList<>();
        ArrayList<Value> values = new ArrayList<>();
        if (funcRParams != null) {
            ArrayList<Exp> funcExps = funcRParams.getExps();
            params.addAll(function.getParams());
            int len = funcExps.size();
            for (int i = 0; i < len; i++) {
                Value result = visitExp(funcExps.get(i), symbolTable);
                String irType = params.get(i).getIrType();
                if (!(result instanceof Constant) && irType.equals("i8")) {
                    if (Support.spareZext(irBlock.getLastInstr())) {
                        Register.cancelAlloc();
                        irBlock.delLastInstr();
                        result = irBlock.getLastInstr();
                    } else {
                        result = irBlock.addInstrTrunc("%"+Register.allocReg(), "i32", result, "i8");
                    }
                }
                values.add(result);
            }
        }

        if (function.getIrType().contains("void")) {
            irBlock.addInstrCall(null, "void", funcName, params, values, function);
            return null;
        } else {
            String irType = function.getIrType();
            Value result = irBlock.addInstrCall("%"+Register.allocReg(), irType, funcName, params, values, function);
            if (function.isChar()) {
                result = irBlock.addInstrZext("%"+Register.allocReg(), "i8", result, "i32");
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
                return new Constant(-((Constant) retValue).getValue());
            } else {
                Constant zero = new Constant(0);
                return irBlock.addInstrBinary("%"+Register.allocReg(), zero, retValue, "sub");
            }
        } else {
            /* Only Exist In Cond */
            if (retValue instanceof Constant) {
                if (retValue.getName().equals("0")) {
                    return new Constant(1);
                } else {
                    return new Constant(0);
                }
            } else {
                Constant zero = new Constant(0);
                Value result = irBlock.addInstrIcmp("%"+Register.allocReg(), "eq", retValue, zero);
                result = irBlock.addInstrZext("%"+Register.allocReg(), "i1", result, "i32");
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
        Value irAlloca = symbol.getIrAlloca();
        String irType = symbol.getIrType();
        Value result;
        if (lVal.isArray()) {
            Value loc = visitExp(lVal.getExp(), symbolTable);
            if (loc instanceof Constant && (symbol instanceof SymbolCon || (symbol instanceof GlobalVal && ((GlobalVal) symbol).isConst()))) {
                return new Constant(symbol.getValueAtLoc(((Constant) loc).getValue()));
            }
            if (symbol.isPointer()) {
                Value temp1 = irBlock.addInstrLoad("%"+Register.allocReg(), irType + "*", irAlloca);
                Value temp2 = irBlock.addInstrGetelementptr("%"+Register.allocReg(), -1, irType, temp1, loc);
                result = irBlock.addInstrLoad("%"+Register.allocReg(), irType, temp2);
            } else {
                Value temp1 = irBlock.addInstrGetelementptr("%"+Register.allocReg(), symbol.getArraySize(), irType, irAlloca, loc);
                result = irBlock.addInstrLoad("%"+Register.allocReg(), irType, temp1);
            }

            if (symbol.isChar()) {
                result = irBlock.addInstrZext("%"+Register.allocReg(), "i8", result, "i32");
            }
        } else {
            if (symbol.isArray()) {
                /* Exist In Call Func While Pass Param */
                /* int c[10]; a = func(c); */
                int size = symbol.getArraySize();
                result = irBlock.addInstrGetelementptr("%"+Register.allocReg(), size, irType, irAlloca, new Constant(0));
            } else if (symbol.isPointer()) {
                /* Exist In Call Func While Pass Param */
                /* func(int a[]); func(a); */
                result = irBlock.addInstrLoad("%"+Register.allocReg(), irType+"*", irAlloca);
            } else {
                if (symbol instanceof SymbolCon || (symbol instanceof GlobalVal && ((GlobalVal) symbol).isConst())) {
                    return new Constant(symbol.getValue());
                }
                result = irBlock.addInstrLoad("%"+Register.allocReg(), irType, irAlloca);
                if (symbol.isChar()) {
                    result = irBlock.addInstrZext("%"+Register.allocReg(), "i8", result, "i32");
                }
            }
        }
        return result;
    }

}
