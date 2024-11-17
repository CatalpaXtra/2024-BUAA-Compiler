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
import midend.llvm.Register;
import midend.llvm.RetValue;
import midend.llvm.Support;
import midend.llvm.function.IrBlock;
import midend.llvm.global.GlobalBuilder;
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

    private static void initLocalIntArray(RetValue memoryReg, int size, ArrayList<Integer> initVal, String irType) {
        RetValue lastReg = memoryReg;
        for (int i = 0; i < initVal.size(); i++) {
            RetValue thisReg = new RetValue(Register.allocReg(), 1);
            if (i == 0) {
                irBlock.addInstrGetelementptr(thisReg, size, irType, lastReg.irOut(), "0");
            } else {
                irBlock.addInstrGetelementptr(thisReg, -1, irType, lastReg.irOut(), "1");
            }
            irBlock.addInstrStore(irType, ""+initVal.get(i), thisReg.irOut());
            lastReg = thisReg;
        }
        if (irType.equals("i8")) {
            for (int i = initVal.size(); i < size; i++) {
                RetValue thisReg = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrGetelementptr(thisReg, -1, "i8", lastReg.irOut(), "1");
                irBlock.addInstrStore("i8", "0", thisReg.irOut());
                lastReg = thisReg;
            }
        }
    }

    private static void initLocalCharArray(RetValue memoryReg, int size, String initVal) {
        initVal = initVal.substring(1, initVal.length() - 1);
        RetValue lastReg = memoryReg;
        int len = 0;
        for (int i = 0; i < initVal.length(); i++) {
            RetValue thisReg = new RetValue(Register.allocReg(), 1);
            if (i == 0) {
                irBlock.addInstrGetelementptr(thisReg, size, "i8", lastReg.irOut(), "0");
            } else {
                irBlock.addInstrGetelementptr(thisReg, -1, "i8", lastReg.irOut(), "1");
            }

            int value = initVal.charAt(i);
            if (initVal.charAt(i) == '\\' && i + 1 < initVal.length()) {
                if (initVal.charAt(i + 1) == 'n') {
                    value = 10;
                    i++;
                }
            }
            irBlock.addInstrStore("i8", ""+value, thisReg.irOut());
            lastReg = thisReg;
            len++;
        }
        for (int i = len; i < size; i++) {
            RetValue thisReg = new RetValue(Register.allocReg(), 1);
            irBlock.addInstrGetelementptr(thisReg, -1, "i8", lastReg.irOut(), "1");
            irBlock.addInstrStore("i8", "0", thisReg.irOut());
            lastReg = thisReg;
        }
    }

    private static void visitConstDef(ConstDef constDef, String type, SymbolTable symbolTable) {
        String symbolType = "Const" + type;
        String irType = Support.varTransfer(type);
        String name = constDef.getIdent().getIdenfr();
        if (constDef.isArray()) {
            symbolType += "Array";
            int size = GlobalBuilder.visitConstExp(constDef.getConstExp(), symbolTable);
            RetValue memoryReg = new RetValue(Register.allocReg(), 1);
            irBlock.addInstrAlloca(memoryReg, irType, size);

            ConstInitValEle constInitValEle = constDef.getConstInitVal().getConstInitValEle();
            if (constInitValEle instanceof ConstExpSet) {
                ArrayList<Integer> initVal = GlobalBuilder.visitConstExpSet((ConstExpSet) constInitValEle, symbolTable);
                initLocalIntArray(memoryReg, size, initVal, irType);
                SymbolCon symbolCon = new SymbolCon(symbolType, name, memoryReg.irOut(), initVal, size);
                symbolTable.addSymbol(symbolCon);
            } else if (constInitValEle instanceof StringConst) {
                String initVal = ((StringConst) constInitValEle).getToken().getContent();
                initLocalCharArray(memoryReg, size, initVal);
                SymbolCon symbolCon = new SymbolCon(symbolType, name, memoryReg.irOut(), initVal, size);
                symbolTable.addSymbol(symbolCon);
            }
        } else {
            RetValue memoryReg = new RetValue(Register.allocReg(), 1);
            irBlock.addInstrAlloca(memoryReg, irType, -1);
            int initVal = GlobalBuilder.visitConstExp((ConstExp) constDef.getConstInitVal().getConstInitValEle(), symbolTable);
            irBlock.addInstrStore(irType, ""+initVal, memoryReg.irOut());
            SymbolCon symbolCon = new SymbolCon(symbolType, name, memoryReg.irOut(), initVal);
            symbolTable.addSymbol(symbolCon);
        }
    }

    private static void initLocalVarIntArray(RetValue memoryReg, int size, ArrayList<RetValue> initVal, String irType) {
        RetValue lastReg = memoryReg;
        for (int i = 0; i < initVal.size(); i++) {
            RetValue thisReg = new RetValue(Register.allocReg(), 1);
            if (i == 0) {
                irBlock.addInstrGetelementptr(thisReg, size, irType, lastReg.irOut(), "0");
            } else {
                irBlock.addInstrGetelementptr(thisReg, -1, irType, lastReg.irOut(), "1");
            }
            if (initVal.get(i).isReg() && irType.equals("i8")) {
                RetValue temp = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrTrunc(temp, "i32", initVal.get(i), "i8");
                irBlock.addInstrStore(irType, temp.irOut(), thisReg.irOut());
            } else {
                irBlock.addInstrStore(irType, initVal.get(i).irOut(), thisReg.irOut());
            }
            lastReg = thisReg;
        }
        if (irType.equals("i8")) {
            for (int i = initVal.size(); i < size; i++) {
                RetValue thisReg = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrGetelementptr(thisReg, -1, "i8", lastReg.irOut(), "1");
                irBlock.addInstrStore("i8", "0", thisReg.irOut());
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
            RetValue memoryReg = new RetValue(Register.allocReg(), 1);
            irBlock.addInstrAlloca(memoryReg, irType, size);

            if (varDef.hasInitValue()) {
                InitValEle initValEle = varDef.getInitVal().getInitValEle();
                if (initValEle instanceof ExpSet) {
                    ArrayList<RetValue> initVal = VarValue.visitExpSet((ExpSet) initValEle, symbolTable);
                    initLocalVarIntArray(memoryReg, size, initVal, irType);
                    SymbolVar symbolVar = new SymbolVar(symbolType, name, memoryReg.irOut(), new ArrayList<>(), size);
                    symbolTable.addSymbol(symbolVar);
                } else if (initValEle instanceof StringConst) {
                    String initVal = ((StringConst) initValEle).getToken().getContent();
                    initLocalCharArray(memoryReg, size, initVal);
                    SymbolVar symbolVar = new SymbolVar(symbolType, name, memoryReg.irOut(), initVal, size);
                    symbolTable.addSymbol(symbolVar);
                }
            } else {
                SymbolVar symbolVar = new SymbolVar(symbolType, name, memoryReg.irOut(), "", size);
                symbolTable.addSymbol(symbolVar);
            }
        } else {
            RetValue memoryReg = new RetValue(Register.allocReg(), 1);
            irBlock.addInstrAlloca(memoryReg, irType, -1);
            if (varDef.hasInitValue()) {
                RetValue result = VarValue.visitExp((Exp) varDef.getInitVal().getInitValEle(), symbolTable);
                if (result.isReg() && symbolType.contains("Char")) {
                    RetValue value = result;
                    result = new RetValue(Register.allocReg(), 1);
                    irBlock.addInstrTrunc(result, "i32", value, "i8");
                }
                irBlock.addInstrStore(irType, result.irOut(), memoryReg.irOut());
            }
            SymbolVar symbolVar = new SymbolVar(symbolType, name, memoryReg.irOut());
            symbolTable.addSymbol(symbolVar);
        }
    }

}
