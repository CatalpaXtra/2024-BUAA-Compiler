package midend.llvm;

import frontend.lexer.Token;
import frontend.parser.CompUnit;
import frontend.parser.declaration.Decl;
import frontend.parser.function.FuncDef;
import frontend.parser.function.MainFuncDef;
import frontend.parser.function.params.FuncFParam;
import midend.llvm.function.Function;
import midend.llvm.function.IrBlock;
import midend.llvm.function.Param;
import midend.llvm.visit.*;
import midend.llvm.symbol.*;

import java.util.ArrayList;

public class Builder {
    private final ArrayList<Decl> decls;
    private final ArrayList<FuncDef> funcDefs;
    private final MainFuncDef mainFuncDef;
    private final SymbolTable globalSymbolTable;

    public Builder(CompUnit compUnit) {
        this.decls = compUnit.getDecls();
        this.funcDefs = compUnit.getFuncDefs();
        this.mainFuncDef = compUnit.getMainFuncDef();
        this.globalSymbolTable = new SymbolTable();
    }

    public void build() {
        for (Decl decl : decls) {
            GlobalDecl.visitGlobalDecl(decl, globalSymbolTable);
        }

        VarValue.setVarValue(globalSymbolTable);
        for (FuncDef funcDef : funcDefs) {
            Register.resetReg();
            visitFuncDef(funcDef);
        }

        Register.resetReg();
        Register.allocReg();
        visitMainFuncDef();
    }

    private void visitFuncDef(FuncDef funcDef) {
        /* visit FuncType and FuncIdent */
        String funcType = funcDef.getFuncType().identifyFuncType() + "Func";
        String funcName = funcDef.getIdent().getIdenfr();

        ArrayList<FuncFParam> funcFParamList = funcDef.getFuncFParams() == null ? new ArrayList<>() : funcDef.getFuncFParams().getFuncFParamList();
        SymbolFunc symbolFunc = new SymbolFunc(funcType, funcName, funcFParamList);
        globalSymbolTable.addSymbol(symbolFunc);

        /* extend symbolTable */
        SymbolTable childSymbolTable = new SymbolTable(globalSymbolTable);

        /* create FuncFParams */
        ArrayList<Param> params = new ArrayList<>();
        for (FuncFParam funcFParam : funcFParamList) {
            String llvmType = Support.tokenTypeTransfer(funcFParam.getBType().getToken().getType());
            if (funcFParam.isArray()) {
                llvmType += "*";
            }
            int reg = Register.allocReg();
            Param param = new Param(llvmType, "%"+reg);
            params.add(param);
        }

        /* load FuncFParams */
        Register.allocReg();
        IrBlock irBlock = new IrBlock();
        for (FuncFParam funcFParam : funcFParamList) {
            String llvmType = Support.tokenTypeTransfer(funcFParam.getBType().getToken().getType());
            String symbolType = funcFParam.getBType().identifyType();
            if (funcFParam.isArray()) {
                llvmType += "*";
                symbolType += "Pointer";
            }
            int memory = Register.allocReg();
            irBlock.addCode("%" + memory + " = alloca " + llvmType);
            irBlock.addCode("store " +  llvmType + " %" + (memory - funcFParamList.size() - 1) + ", " + llvmType + "* %" + memory);

            String name = funcFParam.getIdent().getIdenfr();
            SymbolVar symbolVar = new SymbolVar(symbolType, name, "%" + memory);
            symbolFunc.addSymbol(symbolVar);
            childSymbolTable.addSymbol(symbolVar);
        }

        /* visit Block */
        Stmt.setFuncType(funcType);
        Stmt.setLocalStmtIrBlock(irBlock);
        VarValue.setVarValueIrBlock(irBlock);
        Cond.setCondIrBlock(irBlock);
        LocalDecl.setLocalDeclIrBlock(irBlock);
        Stmt.visitBlock(funcDef.getBlock(), childSymbolTable, funcDef.getFuncType().getToken().getType(), false);
        irBlock.addRetIfNotExist();

        Function function = new Function(funcName, funcType, params, irBlock);
        Module.addFunc(function);
    }

    private void visitMainFuncDef() {
        Stmt.setFuncType("IntFunc");
        SymbolTable childSymbolTable = new SymbolTable(globalSymbolTable);
        IrBlock irBlock = new IrBlock();
        Stmt.setLocalStmtIrBlock(irBlock);
        VarValue.setVarValueIrBlock(irBlock);
        Cond.setCondIrBlock(irBlock);
        LocalDecl.setLocalDeclIrBlock(irBlock);
        Stmt.visitBlock(mainFuncDef.getBlock(), childSymbolTable, Token.Type.INTTK, false);

        Function function = new Function("main", "IntFunc", new ArrayList<>(), irBlock);
        Module.addFunc(function);
    }
}
