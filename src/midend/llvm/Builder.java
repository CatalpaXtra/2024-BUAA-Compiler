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
        SymbolFunc symbolFunc = new SymbolFunc(symbolType, funcName, line, 1);
        globalSymbolTable.addSymbol(symbolFunc);

        /* extend symbolTable */
        SymbolTable childSymbolTable = new SymbolTable(globalSymbolTable);

        /* create FuncFParams */
        ArrayList<FuncFParam> funcFParamList = funcDef.getFuncFParams() == null ? new ArrayList<>() : funcDef.getFuncFParams().getFuncFParamList();
        String declFParam = "";
        for (FuncFParam funcFParam : funcFParamList) {
            int reg = Register.allocReg();
            declFParam += typeTransfer(funcFParam.getBType().getToken().getType()) + " %" + reg + ", ";
        }

        /* load FuncFParams */
        Register.allocReg();
        ArrayList<String> storeFParam = new ArrayList<>();
        for (FuncFParam funcFParam : funcFParamList) {
            int memory = Register.allocReg();
            String valueType = typeTransfer(funcFParam.getBType().getToken().getType());
            storeFParam.add("%" + memory + " = alloca i32");
            storeFParam.add("store " +  valueType + " %" + (memory - funcFParamList.size()) + ", i32* %" + memory);

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
        String funcType = typeTransfer(funcDef.getFuncType().getToken().getType());
        declFParam = declFParam.length() > 2 ? declFParam.substring(0, declFParam.length() - 2) : declFParam;
        module.addCode("define dso_local " + funcType + " @" + funcName + "(" + declFParam + ") {");
        module.addCode(storeFParam);

        /* visit Block */
        LocalStmt.visitBlock(funcDef.getBlock(), childSymbolTable, funcDef.getFuncType().getToken().getType(), false);
        module.addCode("}");
    }

    private void visitMainFuncDef() {
        module.addCode("");
        module.addCode("define dso_local i32 @main() {");
        SymbolTable childSymbolTable = new SymbolTable(globalSymbolTable);
        LocalStmt.visitBlock(mainFuncDef.getBlock(), childSymbolTable, Token.Type.INTTK, false);
        module.addCode("}");
    }

    private String typeTransfer(Token.Type type) {
        if (type.equals(Token.Type.INTTK)) {
            return "i32";
        } else if (type.equals(Token.Type.CHARTK)) {
            return "i8";
        } else {
            return "void";
        }
    }
}
