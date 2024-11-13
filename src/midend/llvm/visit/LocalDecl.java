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
import midend.llvm.Module;
import midend.llvm.Register;
import midend.llvm.RetValue;
import midend.llvm.Support;
import midend.llvm.symbol.*;

import java.util.ArrayList;

public class LocalDecl {
    private static Module module;

    public static void setLocalDecl(Module md) {
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

    private static void initLocalIntArray(RetValue memoryReg, int size, ArrayList<Integer> initVal, String llvmType) {
        RetValue lastReg = memoryReg;
        for (int i = 0; i < initVal.size(); i++) {
            RetValue thisReg = new RetValue(Register.allocReg(), 1);
            if (i == 0) {
                module.addInstrGetelementptrArray(thisReg, size, llvmType, lastReg.irOut(), "0");
            } else {
                module.addInstrGetelementptrPointer(thisReg, llvmType, lastReg.irOut(), "1");
            }
            module.addInstrStore(llvmType, ""+initVal.get(i), thisReg.irOut());
            lastReg = thisReg;
        }
        if (llvmType.equals("i8")) {
            for (int i = initVal.size(); i < size; i++) {
                RetValue thisReg = new RetValue(Register.allocReg(), 1);
                module.addInstrGetelementptrPointer(thisReg, "i8", lastReg.irOut(), "1");
                module.addInstrStore("i8", "0", thisReg.irOut());
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
                module.addInstrGetelementptrArray(thisReg, size, "i8", lastReg.irOut(), "0");
            } else {
                module.addInstrGetelementptrPointer(thisReg, "i8", lastReg.irOut(), "1");
            }

            int value = initVal.charAt(i);
            if (initVal.charAt(i) == '\\' && i + 1 < initVal.length()) {
                if (initVal.charAt(i + 1) == 'n') {
                    value = 10;
                    i++;
                }
            }
            module.addInstrStore("i8", ""+value, thisReg.irOut());
            lastReg = thisReg;
            len++;
        }
        for (int i = len; i < size; i++) {
            RetValue thisReg = new RetValue(Register.allocReg(), 1);
            module.addInstrGetelementptrPointer(thisReg, "i8", lastReg.irOut(), "1");
            module.addInstrStore("i8", "0", thisReg.irOut());
            lastReg = thisReg;
        }
    }

    private static void visitConstDef(ConstDef constDef, String type, SymbolTable symbolTable) {
        String symbolType = "Const" + type;
        String llvmType = Support.varTransfer(type);
        String name = constDef.getIdent().getIdenfr();
        if (constDef.isArray()) {
            symbolType += "Array";
            int size = ConstValue.visitConstExp(constDef.getConstExp(), symbolTable);
            RetValue memoryReg = new RetValue(Register.allocReg(), 1);
            module.addInstrAllocaArray(memoryReg, size, llvmType);

            ConstInitValEle constInitValEle = constDef.getConstInitVal().getConstInitValEle();
            if (constInitValEle instanceof ConstExpSet) {
                ArrayList<Integer> initVal = ConstValue.visitConstExpSet((ConstExpSet) constInitValEle, symbolTable);
                initLocalIntArray(memoryReg, size, initVal, llvmType);
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
            module.addInstrAllocaVar(memoryReg, llvmType);
            int initVal = ConstValue.visitConstExp((ConstExp) constDef.getConstInitVal().getConstInitValEle(), symbolTable);
            module.addInstrStore(llvmType, ""+initVal, memoryReg.irOut());
            SymbolCon symbolCon = new SymbolCon(symbolType, name, memoryReg.irOut(), initVal);
            symbolTable.addSymbol(symbolCon);
        }
    }

    private static void initLocalVarIntArray(RetValue memoryReg, int size, ArrayList<RetValue> initVal, String llvmType) {
        RetValue lastReg = memoryReg;
        for (int i = 0; i < initVal.size(); i++) {
            RetValue thisReg = new RetValue(Register.allocReg(), 1);
            if (i == 0) {
                module.addInstrGetelementptrArray(thisReg, size, llvmType, lastReg.irOut(), "0");
            } else {
                module.addInstrGetelementptrPointer(thisReg, llvmType, lastReg.irOut(), "1");
            }
            if (initVal.get(i).isReg() && llvmType.equals("i8")) {
                RetValue temp = new RetValue(Register.allocReg(), 1);
                module.addInstrTrunc(temp, "i32", initVal.get(i), "i8");
                module.addInstrStore(llvmType, temp.irOut(), thisReg.irOut());
            } else {
                module.addInstrStore(llvmType, initVal.get(i).irOut(), thisReg.irOut());
            }
            lastReg = thisReg;
        }
        if (llvmType.equals("i8")) {
            for (int i = initVal.size(); i < size; i++) {
                RetValue thisReg = new RetValue(Register.allocReg(), 1);
                module.addInstrGetelementptrPointer(thisReg, "i8", lastReg.irOut(), "1");
                module.addInstrStore("i8", "0", thisReg.irOut());
                lastReg = thisReg;
            }
        }
    }

    private static void visitVarDef(VarDef varDef, String type, SymbolTable symbolTable) {
        String symbolType = type;
        String llvmType = Support.varTransfer(type);
        String name = varDef.getIdent().getIdenfr();
        if (varDef.isArray()) {
            symbolType += "Array";
            int size = ConstValue.visitConstExp(varDef.getConstExp(), symbolTable);
            RetValue memoryReg = new RetValue(Register.allocReg(), 1);
            module.addInstrAllocaArray(memoryReg, size, llvmType);

            if (varDef.hasInitValue()) {
                InitValEle initValEle = varDef.getInitVal().getInitValEle();
                if (initValEle instanceof ExpSet) {
                    ArrayList<RetValue> initVal = VarValue.visitExpSet((ExpSet) initValEle, symbolTable);
                    initLocalVarIntArray(memoryReg, size, initVal, llvmType);
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
            module.addInstrAllocaVar(memoryReg, llvmType);
            if (varDef.hasInitValue()) {
                RetValue result = VarValue.visitExp((Exp) varDef.getInitVal().getInitValEle(), symbolTable);
                if (result.isReg() && symbolType.contains("Char")) {
                    RetValue value = result;
                    result = new RetValue(Register.allocReg(), 1);
                    module.addInstrTrunc(result, "i32", value, "i8");
                }
                module.addInstrStore(llvmType, result.irOut(), memoryReg.irOut());
            }
            SymbolVar symbolVar = new SymbolVar(symbolType, name, memoryReg.irOut());
            symbolTable.addSymbol(symbolVar);
        }
    }

}
