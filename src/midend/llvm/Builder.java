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
import midend.llvm.global.GlobalBuilder;
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
            GlobalBuilder.visitGlobalDecl(decl, globalSymbolTable);
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
        /* create new Function */
        String funcType = funcDef.getFuncType().identifyFuncType() + "Func";
        String funcName = funcDef.getIdent().getIdenfr();
        ArrayList<Param> params = new ArrayList<>();
        IrBlock irBlock = new IrBlock();
        Function function = new Function(funcName, funcType, params, irBlock);
        Module.addFunc(function);

        /* extend symbolTable */
        SymbolTable childSymbolTable = new SymbolTable(globalSymbolTable);
        globalSymbolTable.addSymbol(function);

        /* create FuncFParams */
        ArrayList<FuncFParam> funcFParamList = funcDef.getFuncFParams() == null ? new ArrayList<>() : funcDef.getFuncFParams().getFuncFParamList();
        for (FuncFParam funcFParam : funcFParamList) {
            String irType = Support.tokenTypeTransfer(funcFParam.getBType().getToken().getType());
            if (funcFParam.isArray()) {
                irType += "*";
            }
            RetValue reg = new RetValue(Register.allocReg(), 1);
            Param param = new Param(irType, reg.irOut());
            params.add(param);
        }

        /* load FuncFParams */
        Register.allocReg();
        for (FuncFParam funcFParam : funcFParamList) {
            String irType = Support.tokenTypeTransfer(funcFParam.getBType().getToken().getType());
            String symbolType = funcFParam.getBType().identifyType();
            if (funcFParam.isArray()) {
                irType += "*";
                symbolType += "Pointer";
            }
            RetValue memory = new RetValue(Register.allocReg(), 1);
            irBlock.addInstrAlloca(memory, irType, -1);
            irBlock.addInstrStore(irType, "%" + (memory.getValue() - funcFParamList.size() - 1), memory.irOut());

            String name = funcFParam.getIdent().getIdenfr();
            SymbolVar symbolVar = new SymbolVar(symbolType, name, memory.irOut());
            childSymbolTable.addSymbol(symbolVar);
        }

        /* visit Block */
        Stmt.setStmt(irBlock, funcType);
        VarValue.setVarValueIrBlock(irBlock);
        Cond.setCondIrBlock(irBlock);
        LocalDecl.setLocalDeclIrBlock(irBlock);
        Stmt.visitBlock(funcDef.getBlock(), childSymbolTable, funcDef.getFuncType().getToken().getType(), false);
        irBlock.addRetIfNotExist();
    }

    private void visitMainFuncDef() {
        SymbolTable childSymbolTable = new SymbolTable(globalSymbolTable);
        IrBlock irBlock = new IrBlock();
        Stmt.setStmt(irBlock, "IntFunc");
        VarValue.setVarValueIrBlock(irBlock);
        Cond.setCondIrBlock(irBlock);
        LocalDecl.setLocalDeclIrBlock(irBlock);
        Stmt.visitBlock(mainFuncDef.getBlock(), childSymbolTable, Token.Type.INTTK, false);

        Function function = new Function("main", "IntFunc", new ArrayList<>(), irBlock);
        Module.addFunc(function);
    }
}
