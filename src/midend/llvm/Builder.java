package midend.llvm;

import frontend.lexer.Token;
import frontend.parser.CompUnit;
import frontend.parser.block.Block;
import frontend.parser.block.BlockItem;
import frontend.parser.block.BlockItemEle;
import frontend.parser.block.statement.Stmt;
import frontend.parser.block.statement.StmtEle;
import frontend.parser.block.statement.stmtVariant.*;
import frontend.parser.declaration.Decl;
import frontend.parser.declaration.DeclEle;
import frontend.parser.declaration.constDecl.ConstDecl;
import frontend.parser.declaration.varDecl.VarDecl;
import frontend.parser.expression.Exp;
import frontend.parser.expression.add.AddExp;
import frontend.parser.expression.add.MulExp;
import frontend.parser.expression.cond.*;
import frontend.parser.expression.primary.ExpInParent;
import frontend.parser.expression.primary.LVal;
import frontend.parser.expression.primary.PrimaryEle;
import frontend.parser.expression.primary.PrimaryExp;
import frontend.parser.expression.unary.*;
import frontend.parser.function.FuncDef;
import frontend.parser.function.MainFuncDef;
import frontend.parser.function.params.FuncFParam;
import frontend.parser.terminal.Ident;
import midend.symbol.Symbol;
import midend.symbol.SymbolFunc;
import midend.symbol.SymbolTable;
import midend.symbol.SymbolVar;

import java.util.ArrayList;

public class Builder {
    private static int scopeNum = 1;
    private final ArrayList<SymbolTable> symbolTables;
    private final ArrayList<Decl> decls;
    private final ArrayList<FuncDef> funcDefs;
    private final MainFuncDef mainFuncDef;
    private final SymbolTable globalSymbolTable;
    private final Module module;

    public Builder(CompUnit compUnit) {
        this.symbolTables = new ArrayList<>();
        this.decls = compUnit.getDecls();
        this.funcDefs = compUnit.getFuncDefs();
        this.mainFuncDef = compUnit.getMainFuncDef();
        this.globalSymbolTable = new SymbolTable();
        symbolTables.add(globalSymbolTable);
        this.module = new Module();
    }

    public Module getModule() {
        return module;
    }

    public void build() {
        GlobalDecl.setGlobalDecl(module);
        for (Decl decl : decls) {
            visitGlobalDecl(decl, globalSymbolTable, 1);
        }
        LocalDecl.setLocalDecl(globalSymbolTable, module);
        for (FuncDef funcDef : funcDefs) {
            LocalDecl.resetReg(0);
            visitFuncDef(funcDef);
        }
        LocalDecl.resetReg(1);
        visitMainFuncDef();
    }

    private void visitGlobalDecl(Decl decl, SymbolTable symbolTable, int scope) {
        DeclEle declEle = decl.getDeclEle();
        if (declEle instanceof ConstDecl) {
            GlobalDecl.visitGlobalConstDecl((ConstDecl) declEle, symbolTable, scope);
        } else {
            GlobalDecl.visitGlobalVarDecl((VarDecl) declEle, symbolTable, scope);
        }
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

    private void visitFuncDef(FuncDef funcDef) {
        /* visit FuncType and FuncIdent */
        String symbolType = funcDef.getFuncType().identifyFuncType() + "Func";
        String funcName = funcDef.getIdent().getIdenfr();
        int line = funcDef.getIdent().getLine();
        SymbolFunc symbolFunc = new SymbolFunc(symbolType, funcName, line, 1);
        globalSymbolTable.addSymbol(symbolFunc);

        /* extend symbolTable */
        int funcScopeNum = ++scopeNum;
        SymbolTable childSymbolTable = new SymbolTable(globalSymbolTable);
        symbolTables.add(childSymbolTable);

        /* visit FuncFParams */
        ArrayList<FuncFParam> funcFParamList = funcDef.getFuncFParams() == null ? new ArrayList<>() : funcDef.getFuncFParams().getFuncFParamList();
        String declFParam = "";
        for (FuncFParam funcFParam : funcFParamList) {
            int reg = LocalDecl.allocReg();
            declFParam += typeTransfer(funcFParam.getBType().getToken().getType()) + " %" + reg + ", ";
        }
        LocalDecl.allocReg();
        ArrayList<String> storeFParam = new ArrayList<>();
        for (FuncFParam funcFParam : funcFParamList) {
            int memory = LocalDecl.allocReg();
            String valueType = typeTransfer(funcFParam.getBType().getToken().getType());
            storeFParam.add("%" + memory + " = alloca i32");
            storeFParam.add("store " +  valueType + " %" + (memory - funcFParamList.size()) + ", i32* %" + memory);

            String type = funcFParam.getBType().identifyType();
            symbolType = funcFParam.isArray() ? type + "Array" : type;
            String name = funcFParam.getIdent().getIdenfr();
            line = funcFParam.getIdent().getLine();
            SymbolVar symbolVar = new SymbolVar(symbolType, name, line, funcScopeNum, "%" + memory);
            symbolFunc.addSymbol(symbolVar);
            childSymbolTable.addSymbol(symbolVar);
        }

        module.addCode("");
        String funcType = typeTransfer(funcDef.getFuncType().getToken().getType());
        declFParam = declFParam.length() > 2 ? declFParam.substring(0, declFParam.length() - 2) : declFParam;
        module.addCode("define dso_local " + funcType + " @" + funcName + "(" + declFParam + ") {");
        module.addCode(storeFParam);

        /* visit Block */
        visitBlock(funcDef.getBlock(), funcScopeNum, childSymbolTable, funcDef.getFuncType().getToken().getType(), false);
        module.addCode("}");
    }

    private void visitBlock(Block block, int scope, SymbolTable symbolTable, Token.Type type, boolean isInFor) {
        ArrayList<BlockItem> blockItems = block.getBlockItems();
        for (BlockItem blockItem : blockItems) {
            BlockItemEle blockItemEle = blockItem.getBlockItemEle();
            if (blockItemEle instanceof Decl) {
                visitDecl((Decl) blockItemEle, symbolTable, scope);
            } else {
                visitStmt((Stmt) blockItemEle, symbolTable, type, isInFor);
            }
        }
    }

    private void visitDecl(Decl decl, SymbolTable symbolTable, int scope) {
        DeclEle declEle = decl.getDeclEle();
        if (declEle instanceof ConstDecl) {
            LocalDecl.visitConstDecl((ConstDecl) declEle, symbolTable, scope);
        } else {
            LocalDecl.visitVarDecl((VarDecl) declEle, symbolTable, scope);
        }
    }

    private void visitStmt(Stmt stmt, SymbolTable symbolTable, Token.Type type, boolean isInFor) {
        StmtEle stmtEle = stmt.getStmtEle();
        if (stmtEle instanceof StmtAssign) {
            LocalDecl.visitStmtAssign((StmtAssign) stmtEle, symbolTable);
        } else if (stmtEle instanceof StmtExp) {
            // TODO no need?
            LocalDecl.visitExp(((StmtExp) stmtEle).getExp(), symbolTable);
        } else if (stmtEle instanceof StmtGetInt) {
            LocalDecl.visitStmtGetInt((StmtGetInt) stmtEle, symbolTable);
        } else if (stmtEle instanceof StmtGetChar) {
            LocalDecl.visitStmtGetChar((StmtGetChar) stmtEle, symbolTable);
        }

        if (stmtEle instanceof StmtFor) {
            visitStmtFor((StmtFor) stmtEle, symbolTable, type);
        } else if (stmtEle instanceof StmtIf) {
            visitStmtIf((StmtIf) stmtEle, symbolTable, type, isInFor);
        } else if (stmtEle instanceof Block) {
            SymbolTable childSymbolTable = new SymbolTable(symbolTable);
            symbolTables.add(childSymbolTable);
            visitBlock((Block) stmtEle, ++scopeNum, childSymbolTable, type, isInFor);
        } else if (stmtEle instanceof StmtPrint) {
            if (((StmtPrint) stmtEle).getExpNum() > 0) {
                ArrayList<Exp> exps = ((StmtPrint) stmtEle).getExps();
                for (Exp exp : exps) {
                    visitExp(exp, symbolTable);
                }
            }
        } else if (stmtEle instanceof StmtReturn) {
            if (((StmtReturn) stmtEle).existReturnValue()) {
                RetValue result = LocalDecl.visitExp(((StmtReturn) stmtEle).getExp(), symbolTable);
                module.addCode("ret i32 " + result.irOut());
            } else {
                module.addCode("ret void");
            }
        }
    }

    private void visitStmtFor(StmtFor stmtFor, SymbolTable symbolTable, Token.Type type) {
        if (stmtFor.getForStmt1() != null) {
            visitForStmt(stmtFor.getForStmt1(), symbolTable);
        }
        if (stmtFor.getCond() != null) {
            visitCond(stmtFor.getCond(), symbolTable);
        }
        if (stmtFor.getForStmt2() != null) {
            visitForStmt(stmtFor.getForStmt2(), symbolTable);
        }
        visitStmt(stmtFor.getStmt(), symbolTable, type, true);
    }

    private void visitForStmt(ForStmt forStmt, SymbolTable symbolTable) {
        Ident ident = forStmt.getlVal().getIdent();
        Symbol symbol = symbolTable.getSymbol(ident.getIdenfr());
        String memory = symbol.getMemory();
        RetValue result = LocalDecl.visitExp(forStmt.getExp(), symbolTable);
        module.addCode("store i32 " + result.irOut() + ", i32* " + memory);
    }

    private void visitStmtIf(StmtIf stmtIf, SymbolTable symbolTable, Token.Type type, boolean isInFor) {
        visitCond(stmtIf.getCond(), symbolTable);
        visitStmt(stmtIf.getStmt1(), symbolTable, type, isInFor);
        if (stmtIf.getStmt2() != null) {
            visitStmt(stmtIf.getStmt2(), symbolTable, type, isInFor);
        }
    }

    private void visitCond(Cond cond, SymbolTable symbolTable) {
        visitLOrExp(cond.getlOrExp(), symbolTable);
    }

    private void visitLOrExp(LOrExp lOrExp, SymbolTable symbolTable) {
        ArrayList<LAndExp> lAndExps = lOrExp.getLowerExps();
        for (LAndExp lAndExp : lAndExps) {
            visitLAndExp(lAndExp, symbolTable);
        }
    }

    private void visitLAndExp(LAndExp lAndExp, SymbolTable symbolTable) {
        ArrayList<EqExp> EqExps = lAndExp.getLowerExps();
        for (EqExp eqExp : EqExps) {
            visitEqExp(eqExp, symbolTable);
        }
    }

    private void visitEqExp(EqExp eqExp, SymbolTable symbolTable) {
        ArrayList<RelExp> relExps = eqExp.getLowerExps();
        for (RelExp relExp : relExps) {
            visitRelExp(relExp, symbolTable);
        }
    }

    private void visitRelExp(RelExp relExp, SymbolTable symbolTable) {
        ArrayList<AddExp> addExps = relExp.getLowerExps();
        for (AddExp addExp : addExps) {
            visitAddExp(addExp, symbolTable);
        }
    }

    private void visitPrimaryExp(PrimaryExp primaryExp, SymbolTable symbolTable) {
        PrimaryEle primaryEle = primaryExp.getPrimaryEle();
        if (primaryEle instanceof ExpInParent) {
            visitExp(((ExpInParent) primaryEle).getExp(), symbolTable);
        } else if (primaryEle instanceof LVal) {
            visitLVal((LVal) primaryEle, symbolTable);
        }
    }

    private void visitUnaryExp(UnaryExp unaryExp, SymbolTable symbolTable) {
        UnaryEle unaryEle = unaryExp.getUnaryEle();
        if (unaryEle instanceof UnaryFunc) {
            visitUnaryFunc((UnaryFunc) unaryEle, symbolTable);
        } else if (unaryEle instanceof UnaryOpExp) {
            visitUnaryOpExp((UnaryOpExp) unaryEle, symbolTable);
        } else if (unaryEle instanceof PrimaryExp) {
            visitPrimaryExp((PrimaryExp) unaryEle, symbolTable);
        }
    }

    private void visitMulExp(MulExp mulExp, SymbolTable symbolTable) {
        ArrayList<UnaryExp> unaryExps = mulExp.getLowerExps();
        for (UnaryExp unaryExp : unaryExps) {
            visitUnaryExp(unaryExp, symbolTable);
        }
    }

    private void visitAddExp(AddExp addExp, SymbolTable symbolTable) {
        ArrayList<MulExp> mulExps = addExp.getLowerExps();
        for (MulExp mulExp : mulExps) {
            visitMulExp(mulExp, symbolTable);
        }
    }

    private void visitExp(Exp exp, SymbolTable symbolTable) {
        visitAddExp(exp.getAddExp(), symbolTable);
    }

    private void visitUnaryFunc(UnaryFunc unaryFunc, SymbolTable symbolTable) {
        Ident funcIdent = unaryFunc.getIdent();
        FuncRParams funcRParams = unaryFunc.getFuncRParams();
        if (funcRParams != null) {
            ArrayList<Exp> funcExps = funcRParams.getExps();
            for (Exp exp : funcExps) {
                visitExp(exp, symbolTable);
            }
        }
    }

    private void visitUnaryOpExp(UnaryOpExp unaryOpExp, SymbolTable symbolTable) {
        UnaryOp unaryOp = unaryOpExp.getUnaryOp();
        UnaryExp unaryExp = unaryOpExp.getUnaryExp();
        visitUnaryExp(unaryExp, symbolTable);
    }

    private void visitLVal(LVal lVal, SymbolTable symbolTable) {
        Ident ident = lVal.getIdent();
        if (lVal.isArray()) {
            Exp exp = lVal.getExp();
            visitExp(exp, symbolTable);
        }
    }

    private void visitMainFuncDef() {
        module.addCode("");
        module.addCode("define dso_local i32 @main() {");
        SymbolTable childSymbolTable = new SymbolTable(globalSymbolTable);
        symbolTables.add(childSymbolTable);
        visitBlock(mainFuncDef.getBlock(), ++scopeNum, childSymbolTable, Token.Type.INTTK, false);
        module.addCode("}");
    }
}
