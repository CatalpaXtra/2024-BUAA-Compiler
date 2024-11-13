package midend.llvm.decl;

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
import midend.llvm.Support;
import midend.llvm.symbol.*;

import java.util.ArrayList;

public class GlobalDecl {
    private static Module module;

    public static void setGlobalDecl(Module md) {
        module = md;
    }

    public static void visitGlobalDecl(Decl decl, SymbolTable symbolTable) {
        DeclEle declEle = decl.getDeclEle();
        if (declEle instanceof ConstDecl) {
            GlobalDecl.visitGlobalConstDecl((ConstDecl) declEle, symbolTable);
        } else {
            GlobalDecl.visitGlobalVarDecl((VarDecl) declEle, symbolTable);
        }
    }

    public static void visitGlobalConstDecl(ConstDecl constDecl, SymbolTable symbolTable) {
        String type = constDecl.getBType().identifyType();
        ArrayList<ConstDef> constDefs = constDecl.getConstDefs();
        for (ConstDef constDef : constDefs) {
            visitGlobalConstDef(constDef, type, symbolTable);
        }
    }

    public static void visitGlobalVarDecl(VarDecl varDecl, SymbolTable symbolTable) {
        String type = varDecl.getBType().identifyType();
        ArrayList<VarDef> varDefs = varDecl.getVarDefs();
        for (VarDef varDef : varDefs) {
            visitGlobalVarDef(varDef, type, symbolTable);
        }
    }

    private static void visitGlobalConstDef(ConstDef constDef, String type, SymbolTable symbolTable) {
        String symbolType = "Const" + type;
        String llvmType = Support.varTransfer(type);
        String name = constDef.getIdent().getIdenfr();
        if (constDef.isArray()) {
            symbolType += "Array";
            int size = ConstValue.visitConstExp(constDef.getConstExp(), symbolTable);
            ConstInitValEle constInitValEle = constDef.getConstInitVal().getConstInitValEle();

            if (constInitValEle instanceof ConstExpSet) {
                ArrayList<Integer> initVal = ConstValue.visitConstExpSet((ConstExpSet) constInitValEle, symbolTable);
                module.addGlobalVar("@" + name + " = dso_local global [" + size + " x " + llvmType + "] " + initGlobalIntArray(size, initVal, llvmType));
                SymbolCon symbolCon = new SymbolCon(symbolType, name, "@" + name, initVal, size);
                symbolTable.addSymbol(symbolCon);
            } else if (constInitValEle instanceof StringConst) {
                String initVal = ((StringConst) constInitValEle).getToken().getContent();
                module.addGlobalVar("@" + name + " = dso_local global [" + size + " x i8] c" + initGlobalCharArray(size, initVal));
                SymbolCon symbolCon = new SymbolCon(symbolType, name, "@" + name, initVal, size);
                symbolTable.addSymbol(symbolCon);
            }
        } else {
            int initVal = ConstValue.visitConstExp((ConstExp) constDef.getConstInitVal().getConstInitValEle(), symbolTable);
            module.addGlobalVar("@" + name + " = dso_local global " + llvmType + " " + initVal);
            SymbolCon symbolCon = new SymbolCon(symbolType, name, "@" + name, initVal);
            symbolTable.addSymbol(symbolCon);
        }
    }

    private static void visitGlobalVarDef(VarDef varDef, String type, SymbolTable symbolTable) {
        String symbolType = type;
        String name = varDef.getIdent().getIdenfr();
        if (varDef.isArray()) {
            symbolType += "Array";
            int size = ConstValue.visitConstExp(varDef.getConstExp(), symbolTable);
            if (varDef.hasInitValue()) {
                InitValEle initValEle = varDef.getInitVal().getInitValEle();
                if (initValEle instanceof ExpSet) {
                    ArrayList<Integer> initVal = ConstValue.visitExpSet((ExpSet) initValEle, symbolTable);
                    String llvmType = Support.varTransfer(type);
                    module.addGlobalVar("@" + name + " = dso_local global [" + size + " x " + llvmType + "] " + initGlobalIntArray(size, initVal, llvmType));
                    SymbolVar symbolVar = new SymbolVar(symbolType, name, "@" + name, initVal, size);
                    symbolTable.addSymbol(symbolVar);
                } else if (initValEle instanceof StringConst) {
                    String initVal = ((StringConst) initValEle).getToken().getContent();
                    module.addGlobalVar("@" + name + " = dso_local global [" + size + " x i8] c" + initGlobalCharArray(size, initVal));
                    SymbolVar symbolVar = new SymbolVar(symbolType, name, "@" + name, initVal, size);
                    symbolTable.addSymbol(symbolVar);
                }
            } else {
                if (symbolType.contains("Int")) {
                    module.addGlobalVar("@" + name + " = dso_local global [" + size + " x i32] zeroinitializer");
                    SymbolVar symbolVar = new SymbolVar(symbolType, name, "@" + name, new ArrayList<>(), size);
                    symbolTable.addSymbol(symbolVar);
                } else {
                    module.addGlobalVar("@" + name + " = dso_local global [" + size + " x i8] zeroinitializer");
                    SymbolVar symbolVar = new SymbolVar(symbolType, name, "@" + name, "", size);
                    symbolTable.addSymbol(symbolVar);
                }
            }
        } else {
            int initVal = 0;
            if (varDef.hasInitValue()) {
                initVal = ConstValue.visitExp((Exp) varDef.getInitVal().getInitValEle(), symbolTable);
            }
            String llvmType = Support.varTransfer(type);
            module.addGlobalVar("@" + name + " = dso_local global " + llvmType + " " + initVal);
            SymbolVar symbolVar = new SymbolVar(symbolType, name, "@" + name, initVal);
            symbolTable.addSymbol(symbolVar);
        }
    }

    private static String initGlobalIntArray(int arraySize, ArrayList<Integer> initVal, String llvmType) {
        boolean zeroinitializer = true;
        String arrayFormat = "[";
        for (Integer val : initVal) {
            if (val != 0) {
                zeroinitializer = false;
            }
            arrayFormat += llvmType + " " + val + ", ";
        }
        if (zeroinitializer) {
            return "zeroinitializer";
        }
        for (int i = initVal.size(); i < arraySize; i++) {
            arrayFormat += llvmType + " 0, ";
        }
        return arrayFormat.substring(0, arrayFormat.length() - 2) + "]";
    }

    public static String initGlobalCharArray(int arraySize, String initVal) {
        initVal = initVal.substring(1, initVal.length() - 1);
        String arrayFormat = "\"";
        int len = 0;
        for (int i = 0; i < initVal.length(); i++) {
            if (initVal.charAt(i) == '\\' && i + 1 < initVal.length()) {
                char next = initVal.charAt(i + 1);
                if (next == '0') {
                    arrayFormat += "\\00";
                } else if (next == 'n') {
                    arrayFormat += "\\0A";
                }
                i++;
            } else {
                arrayFormat += initVal.charAt(i);
            }
            len++;
        }
        for (int i = len; i < arraySize; i++) {
            arrayFormat += "\\00";
        }
        return arrayFormat + "\"";
    }
}
