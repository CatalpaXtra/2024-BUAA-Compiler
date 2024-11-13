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
import midend.llvm.global.*;
import midend.llvm.Module;
import midend.llvm.Support;
import midend.llvm.symbol.*;

import java.util.ArrayList;

public class GlobalDecl {
    public static void visitGlobalDecl(Decl decl, SymbolTable symbolTable) {
        DeclEle declEle = decl.getDeclEle();
        if (declEle instanceof ConstDecl) {
            GlobalDecl.visitGlobalConstDecl((ConstDecl) declEle, symbolTable);
        } else {
            GlobalDecl.visitGlobalVarDecl((VarDecl) declEle, symbolTable);
        }
    }

    private static void visitGlobalConstDecl(ConstDecl constDecl, SymbolTable symbolTable) {
        String type = constDecl.getBType().identifyType();
        ArrayList<ConstDef> constDefs = constDecl.getConstDefs();
        for (ConstDef constDef : constDefs) {
            visitGlobalConstDef(constDef, type, symbolTable);
        }
    }

    private static void visitGlobalVarDecl(VarDecl varDecl, SymbolTable symbolTable) {
        String type = varDecl.getBType().identifyType();
        ArrayList<VarDef> varDefs = varDecl.getVarDefs();
        for (VarDef varDef : varDefs) {
            visitGlobalVarDef(varDef, type, symbolTable);
        }
    }

    private static void visitGlobalConstDef(ConstDef constDef, String type, SymbolTable symbolTable) {
        String symbolType = "Const" + type;
        String irType = Support.varTransfer(type);
        String name = constDef.getIdent().getIdenfr();
        Constant constant = null;
        int size = -1;
        if (constDef.isArray()) {
            symbolType += "Array";
            size = ConstValue.visitConstExp(constDef.getConstExp(), symbolTable);

            ConstInitValEle constInitValEle = constDef.getConstInitVal().getConstInitValEle();
            if (constInitValEle instanceof ConstExpSet) {
                ArrayList<Integer> initVal = ConstValue.visitConstExpSet((ConstExpSet) constInitValEle, symbolTable);
                constant = new IrArray(irType, initVal, size);
            } else if (constInitValEle instanceof StringConst) {
                String initVal = ((StringConst) constInitValEle).getToken().getContent();
                constant = new IrString(initVal, size);
            }
        } else {
            int initVal = ConstValue.visitConstExp((ConstExp) constDef.getConstInitVal().getConstInitValEle(), symbolTable);
            constant = new IrVar(initVal);
        }
        GlobalVal globalVal = new GlobalVal(name, symbolType, constant, size);
        Module.addGlobalVal(globalVal);
        symbolTable.addSymbol(globalVal);
    }

    private static void visitGlobalVarDef(VarDef varDef, String type, SymbolTable symbolTable) {
        String symbolType = type;
        String irType = Support.varTransfer(type);
        String name = varDef.getIdent().getIdenfr();
        Constant constant = null;
        int size = -1;
        if (varDef.isArray()) {
            symbolType += "Array";
            size = ConstValue.visitConstExp(varDef.getConstExp(), symbolTable);
            if (varDef.hasInitValue()) {
                InitValEle initValEle = varDef.getInitVal().getInitValEle();
                if (initValEle instanceof ExpSet) {
                    ArrayList<Integer> initVal = ConstValue.visitExpSet((ExpSet) initValEle, symbolTable);
                    constant = new IrArray(irType, initVal, size);
                } else if (initValEle instanceof StringConst) {
                    String initVal = ((StringConst) initValEle).getToken().getContent();
                    constant = new IrString(initVal, size);
                }
            }
        } else {
            int initVal = 0;
            if (varDef.hasInitValue()) {
                initVal = ConstValue.visitExp((Exp) varDef.getInitVal().getInitValEle(), symbolTable);
            }
            constant = new IrVar(initVal);
        }
        GlobalVal globalVal = new GlobalVal(name, symbolType, constant, size);
        Module.addGlobalVal(globalVal);
        symbolTable.addSymbol(globalVal);
    }
}
