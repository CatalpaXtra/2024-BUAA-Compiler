package midend.llvm;

import frontend.lexer.Token;
import frontend.parser.CompUnit;
import frontend.parser.declaration.Decl;
import frontend.parser.function.FuncDef;
import frontend.parser.function.MainFuncDef;
import frontend.parser.function.params.FuncFParam;
import midend.llvm.decl.Cond;
import midend.llvm.decl.GlobalDecl;
import midend.llvm.decl.LocalDecl;
import midend.llvm.decl.VarValue;
import midend.llvm.symbol.*;

import java.util.ArrayList;

public class Builder {
    private final ArrayList<Decl> decls;
    private final ArrayList<FuncDef> funcDefs;
    private final MainFuncDef mainFuncDef;
    private final SymbolTable globalSymbolTable;
    private final Module module;

    public Builder(CompUnit compUnit) {
        this.decls = compUnit.getDecls();
        this.funcDefs = compUnit.getFuncDefs();
        this.mainFuncDef = compUnit.getMainFuncDef();
        this.globalSymbolTable = new SymbolTable();
        this.module = new Module();
    }

    public Module getModule() {
        return module;
    }

    public void build() {
        GlobalDecl.setGlobalDecl(module);
        for (Decl decl : decls) {
            GlobalDecl.visitGlobalDecl(decl, globalSymbolTable);
        }

        VarValue.setVarValue(globalSymbolTable, module);
        Cond.setCond(module);
        LocalDecl.setLocalDecl(globalSymbolTable, module);
        Stmt.setLocalStmt(module);
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
        Stmt.setFuncType(funcType);

        /* extend symbolTable */
        SymbolTable childSymbolTable = new SymbolTable(globalSymbolTable);

        /* create FuncFParams */
        String declFParam = "";
        for (FuncFParam funcFParam : funcFParamList) {
            String fParamType = Support.tokenTypeTransfer(funcFParam.getBType().getToken().getType());
            if (funcFParam.isArray()) {
                fParamType += "*";
            }
            int reg = Register.allocReg();
            declFParam += fParamType + " %" + reg + ", ";
        }

        /* load FuncFParams */
        Register.allocReg();
        ArrayList<String> storeFParam = new ArrayList<>();
        for (FuncFParam funcFParam : funcFParamList) {
            String llvmType = Support.tokenTypeTransfer(funcFParam.getBType().getToken().getType());
            String symbolType = funcFParam.getBType().identifyType();
            if (funcFParam.isArray()) {
                llvmType += "*";
                symbolType += "Pointer";
            }
            int memory = Register.allocReg();
            storeFParam.add("%" + memory + " = alloca " + llvmType);
            storeFParam.add("store " +  llvmType + " %" + (memory - funcFParamList.size() - 1) + ", " + llvmType + "* %" + memory);

            String name = funcFParam.getIdent().getIdenfr();
            SymbolVar symbolVar = new SymbolVar(symbolType, name, "%" + memory);
            symbolFunc.addSymbol(symbolVar);
            childSymbolTable.addSymbol(symbolVar);
        }

        /* load Func Declare */
        module.addCode("");
        String llvmType = Support.tokenTypeTransfer(funcDef.getFuncType().getToken().getType());
        declFParam = declFParam.length() > 2 ? declFParam.substring(0, declFParam.length() - 2) : declFParam;
        module.addCode("define dso_local " + llvmType + " @" + funcName + "(" + declFParam + ") {");
        module.addCode(storeFParam);

        /* visit Block */
        Stmt.visitBlock(funcDef.getBlock(), childSymbolTable, funcDef.getFuncType().getToken().getType(), false);
        module.addRetIfNotExist();
        module.addCode("}");
    }

    private void visitMainFuncDef() {
        module.addCode("");
        module.addCode("define dso_local i32 @main() {");
        Stmt.setFuncType("IntFunc");
        SymbolTable childSymbolTable = new SymbolTable(globalSymbolTable);
        Stmt.visitBlock(mainFuncDef.getBlock(), childSymbolTable, Token.Type.INTTK, false);
        module.addCode("}");
    }
}
