package midend.llvm.visit;

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
import frontend.parser.terminal.StringConst;
import midend.llvm.Constant;
import midend.llvm.Register;
import midend.llvm.Value;
import midend.llvm.Support;
import midend.llvm.function.IrBlock;
import midend.llvm.global.GlobalBuilder;
import midend.llvm.global.constant.IrArray;
import midend.llvm.global.constant.IrConstant;
import midend.llvm.global.constant.IrString;
import midend.llvm.global.constant.IrVar;
import midend.llvm.instr.IrInstr;
import midend.llvm.instr.IrZext;
import midend.llvm.symbol.*;

import java.util.ArrayList;

public class LocalDecl {
    private static IrBlock irBlock;

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

    private static void initLocalIntArray(Value memoryReg, int size, ArrayList<Integer> initVal, String irType) {
        Value lastReg = memoryReg;
        for (int i = 0; i < initVal.size(); i++) {
            Value thisReg = new Value(Register.allocReg(), "i32");
            if (i == 0) {
                irBlock.addInstrGetelementptr(thisReg, size, irType, lastReg, new Constant(0));
            } else {
                irBlock.addInstrGetelementptr(thisReg, -1, irType, lastReg, new Constant(1));
            }
            Constant value = new Constant(initVal.get(i));
            irBlock.addInstrStore(irType, value, thisReg);
            lastReg = thisReg;
        }
        if (irType.equals("i8")) {
            for (int i = initVal.size(); i < size; i++) {
                Value thisReg = new Value(Register.allocReg(), "i32");
                irBlock.addInstrGetelementptr(thisReg, -1, "i8", lastReg, new Constant(1));
                irBlock.addInstrStore("i8", new Constant(0), thisReg);
                lastReg = thisReg;
            }
        }
    }

    private static void initLocalCharArray(Value memoryReg, int size, String initVal) {
        initVal = initVal.substring(1, initVal.length() - 1);
        Value lastReg = memoryReg;
        int len = 0;
        for (int i = 0; i < initVal.length(); i++) {
            Value thisReg = new Value(Register.allocReg(), "i32");
            if (i == 0) {
                irBlock.addInstrGetelementptr(thisReg, size, "i8", lastReg, new Constant(0));
            } else {
                irBlock.addInstrGetelementptr(thisReg, -1, "i8", lastReg, new Constant(1));
            }

            int value = initVal.charAt(i);
            if (initVal.charAt(i) == '\\' && i + 1 < initVal.length()) {
                if (initVal.charAt(i + 1) == 'n') {
                    value = 10;
                    i++;
                }
            }
            irBlock.addInstrStore("i8", new Constant(value), thisReg);
            lastReg = thisReg;
            len++;
        }
        for (int i = len; i < size; i++) {
            Value thisReg = new Value(Register.allocReg(), "i32");
            irBlock.addInstrGetelementptr(thisReg, -1, "i8", lastReg, new Constant(1));
            irBlock.addInstrStore("i8", new Constant(0), thisReg);
            lastReg = thisReg;
        }
    }

    private static void visitConstDef(ConstDef constDef, String type, SymbolTable symbolTable) {
        String symbolType = "Const" + type;
        String irType = Support.varTransfer(type);
        String name = constDef.getIdent().getIdenfr();
        IrConstant constant = null;
        int size = -1;
        Value memoryReg;
        if (constDef.isArray()) {
            symbolType += "Array";
            size = GlobalBuilder.visitConstExp(constDef.getConstExp(), symbolTable);
            memoryReg = new Value(Register.allocReg(), "i32");
            irBlock.addInstrAlloca(memoryReg, irType, size);

            ConstInitValEle constInitValEle = constDef.getConstInitVal().getConstInitValEle();
            if (constInitValEle instanceof ConstExpSet) {
                ArrayList<Integer> initVal = GlobalBuilder.visitConstExpSet((ConstExpSet) constInitValEle, symbolTable);
                initLocalIntArray(memoryReg, size, initVal, irType);
                constant = new IrArray(irType, initVal, size);
            } else if (constInitValEle instanceof StringConst) {
                String initVal = ((StringConst) constInitValEle).getToken().getContent();
                initLocalCharArray(memoryReg, size, initVal);
                constant = new IrString(initVal, size);
            }
        } else {
            memoryReg = new Value(Register.allocReg(), "i32");
            irBlock.addInstrAlloca(memoryReg, irType, size);
            int initVal = GlobalBuilder.visitConstExp((ConstExp) constDef.getConstInitVal().getConstInitValEle(), symbolTable);
            irBlock.addInstrStore(irType, new Constant(initVal), memoryReg);
            constant = new IrVar(initVal);
        }
        SymbolCon symbolCon = new SymbolCon(symbolType, name, memoryReg, constant, size);
        symbolTable.addSymbol(symbolCon);
    }

    private static void initLocalVarIntArray(Value memoryReg, int size, ArrayList<Value> initVal, String irType) {
        Value lastReg = memoryReg;
        for (int i = 0; i < initVal.size(); i++) {
            Value thisReg = new Value(Register.allocReg(), "i32");
            if (i == 0) {
                irBlock.addInstrGetelementptr(thisReg, size, irType, lastReg, new Constant(0));
            } else {
                irBlock.addInstrGetelementptr(thisReg, -1, irType, lastReg, new Constant(1));
            }
            if (!(initVal.get(i) instanceof Constant) && irType.equals("i8")) {
                Value temp = new Value(Register.allocReg(), "i32");
                irBlock.addInstrTrunc(temp, "i32", initVal.get(i), "i8");
                irBlock.addInstrStore(irType, temp, thisReg);
            } else {
                irBlock.addInstrStore(irType, initVal.get(i), thisReg);
            }
            lastReg = thisReg;
        }
        if (irType.equals("i8")) {
            for (int i = initVal.size(); i < size; i++) {
                Value thisReg = new Value(Register.allocReg(), "i32");
                irBlock.addInstrGetelementptr(thisReg, -1, "i8", lastReg, new Constant(1));
                irBlock.addInstrStore("i8", new Constant(0), thisReg);
                lastReg = thisReg;
            }
        }
    }

    private static void visitVarDef(VarDef varDef, String type, SymbolTable symbolTable) {
        String symbolType = type;
        String irType = Support.varTransfer(type);
        String name = varDef.getIdent().getIdenfr();
        if (varDef.isArray()) {
            symbolType += "Array";
            int size = GlobalBuilder.visitConstExp(varDef.getConstExp(), symbolTable);
            Value memoryReg = new Value(Register.allocReg(), "i32");
            irBlock.addInstrAlloca(memoryReg, irType, size);

            if (varDef.hasInitValue()) {
                InitValEle initValEle = varDef.getInitVal().getInitValEle();
                if (initValEle instanceof ExpSet) {
                    ArrayList<Value> initVal = VarValue.visitExpSet((ExpSet) initValEle, symbolTable);
                    initLocalVarIntArray(memoryReg, size, initVal, irType);
                    SymbolVar symbolVar = new SymbolVar(symbolType, name, memoryReg, new ArrayList<>(), size);
                    symbolTable.addSymbol(symbolVar);
                } else if (initValEle instanceof StringConst) {
                    String initVal = ((StringConst) initValEle).getToken().getContent();
                    initLocalCharArray(memoryReg, size, initVal);
                    SymbolVar symbolVar = new SymbolVar(symbolType, name, memoryReg, initVal, size);
                    symbolTable.addSymbol(symbolVar);
                }
            } else {
                SymbolVar symbolVar = new SymbolVar(symbolType, name, memoryReg, "", size);
                symbolTable.addSymbol(symbolVar);
            }
        } else {
            Value memoryReg = new Value(Register.allocReg(), "i32");
            irBlock.addInstrAlloca(memoryReg, irType, -1);
            if (varDef.hasInitValue()) {
                Value result = VarValue.visitExp((Exp) varDef.getInitVal().getInitValEle(), symbolTable);
                if (!(result instanceof Constant) && irType.equals("i8")) {
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
                irBlock.addInstrStore(irType, result, memoryReg);
            }
            SymbolVar symbolVar = new SymbolVar(symbolType, name, memoryReg);
            symbolTable.addSymbol(symbolVar);
        }
    }

}
