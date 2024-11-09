package midend.llvm;

import frontend.lexer.Token;
import frontend.parser.CompUnit;
import frontend.parser.declaration.Decl;
import frontend.parser.function.FuncDef;
import frontend.parser.function.MainFuncDef;
import frontend.parser.function.params.FuncFParam;
import midend.symbol.SymbolFunc;
import midend.symbol.SymbolTable;
import midend.symbol.SymbolVar;

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

        LocalDecl.setLocalDecl(globalSymbolTable, module);
        LocalStmt.setLocalStmt(module);
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
        String symbolType = funcDef.getFuncType().identifyFuncType() + "Func";
        String funcName = funcDef.getIdent().getIdenfr();
        int line = funcDef.getIdent().getLine();

        ArrayList<FuncFParam> funcFParamList = funcDef.getFuncFParams() == null ? new ArrayList<>() : funcDef.getFuncFParams().getFuncFParamList();
        SymbolFunc symbolFunc = new SymbolFunc(symbolType, funcName, line, 1, funcFParamList);
        globalSymbolTable.addSymbol(symbolFunc);
        LocalStmt.setFuncType(symbolType);

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
            String fParamType = Support.tokenTypeTransfer(funcFParam.getBType().getToken().getType());
            if (funcFParam.isArray()) {
                fParamType += "*";
            }
            int memory = Register.allocReg();
            storeFParam.add("%" + memory + " = alloca " + fParamType);
            storeFParam.add("store " +  fParamType + " %" + (memory - funcFParamList.size() - 1) + ", " + fParamType + "* %" + memory);

            String type = funcFParam.getBType().identifyType();
            symbolType = funcFParam.isArray() ? type + "Array" : type;
            String name = funcFParam.getIdent().getIdenfr();
            line = funcFParam.getIdent().getLine();
            SymbolVar symbolVar = new SymbolVar(symbolType, name, line, "%" + memory);
            symbolFunc.addSymbol(symbolVar);
            childSymbolTable.addSymbol(symbolVar);
        }

        /* load Func Declare */
        module.addCode("");
        String funcType = Support.tokenTypeTransfer(funcDef.getFuncType().getToken().getType());
        declFParam = declFParam.length() > 2 ? declFParam.substring(0, declFParam.length() - 2) : declFParam;
        module.addCode("define dso_local " + funcType + " @" + funcName + "(" + declFParam + ") {");
        module.addCode(storeFParam);

        /* visit Block */
        LocalStmt.visitBlock(funcDef.getBlock(), childSymbolTable, funcDef.getFuncType().getToken().getType(), false);
        module.addRetIfNotExist();
        module.addCode("}");
    }

    private void visitMainFuncDef() {
        module.addCode("");
        module.addCode("define dso_local i32 @main() {");
        LocalStmt.setFuncType("IntFunc");
        SymbolTable childSymbolTable = new SymbolTable(globalSymbolTable);
        LocalStmt.visitBlock(mainFuncDef.getBlock(), childSymbolTable, Token.Type.INTTK, false);
        module.addCode("}");
    }
}
