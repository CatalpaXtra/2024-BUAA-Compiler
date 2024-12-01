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
import midend.llvm.instr.IrInstr;
import midend.llvm.visit.*;
import midend.llvm.symbol.*;

import java.util.ArrayList;

public class IrBuilder {
    private final ArrayList<Decl> decls;
    private final ArrayList<FuncDef> funcDefs;
    private final MainFuncDef mainFuncDef;
    private final SymbolTable globalSymbolTable;

    public IrBuilder(CompUnit compUnit) {
        this.decls = compUnit.getDecls();
        this.funcDefs = compUnit.getFuncDefs();
        this.mainFuncDef = compUnit.getMainFuncDef();
        this.globalSymbolTable = new SymbolTable();
    }

    public void build() {
        for (Decl decl : decls) {
            GlobalBuilder.visitGlobalDecl(decl, globalSymbolTable);
        }

        LocalDecl.setLocalDecl(globalSymbolTable);
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
        Function function = new Function(funcName, Support.varTransfer(funcType), params, irBlock);
        IrModule.addFunc(function);

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
            Param param = new Param(irType, "%"+Register.allocReg());
            params.add(param);
        }

        /* load FuncFParams */
        Register.allocReg();
        for (int i = 0; i < funcFParamList.size(); i++) {
            FuncFParam funcFParam = funcFParamList.get(i);
            String irType = Support.tokenTypeTransfer(funcFParam.getBType().getToken().getType());
            if (funcFParam.isArray()) {
                irType += "*";
            }
            int regNum = Register.allocReg();
            IrInstr irAlloca = irBlock.addInstrAlloca("%"+regNum, irType, -1);
            Param param = params.get(i);
            irBlock.addInstrStore(irType, param, irAlloca);

            String name = funcFParam.getIdent().getIdenfr();
            SymbolVar symbolVar = new SymbolVar(name, irType, irAlloca, -1, null);
            childSymbolTable.addSymbol(symbolVar);
        }

        /* visit Block */
        Stmt.setStmt(irBlock, funcType);
        LocalDecl.setLocalDeclIrBlock(irBlock);
        Stmt.visitBlock(funcDef.getBlock(), childSymbolTable, funcDef.getFuncType().getToken().getType(), false);
        irBlock.addRetIfNotExist();
    }

    private void visitMainFuncDef() {
        SymbolTable childSymbolTable = new SymbolTable(globalSymbolTable);
        IrBlock irBlock = new IrBlock();
        Stmt.setStmt(irBlock, "IntFunc");
        LocalDecl.setLocalDeclIrBlock(irBlock);
        Stmt.visitBlock(mainFuncDef.getBlock(), childSymbolTable, Token.Type.INTTK, false);

        Function function = new Function("main", "i32", new ArrayList<>(), irBlock);
        IrModule.addFunc(function);
    }
}
