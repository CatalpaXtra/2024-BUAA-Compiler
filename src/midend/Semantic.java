package midend;

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
import frontend.parser.declaration.constDecl.ConstDef;
import frontend.parser.declaration.constDecl.constInitVal.ConstExpSet;
import frontend.parser.declaration.constDecl.constInitVal.ConstInitVal;
import frontend.parser.declaration.constDecl.constInitVal.ConstInitValEle;
import frontend.parser.declaration.varDecl.VarDecl;
import frontend.parser.declaration.varDecl.VarDef;
import frontend.parser.declaration.varDecl.initVal.ExpSet;
import frontend.parser.declaration.varDecl.initVal.InitVal;
import frontend.parser.declaration.varDecl.initVal.InitValEle;
import frontend.parser.expression.ConstExp;
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
import midend.symbol.*;

import java.util.ArrayList;

public class Semantic {
    private static int scopeNum = 1;
    private final ArrayList<SymbolTable> symbolTables;
    private final ArrayList<Decl> decls;
    private final ArrayList<FuncDef> funcDefs;
    private final MainFuncDef mainFuncDef;
    private final SymbolTable globalSymbolTable;

    public Semantic(CompUnit compUnit) {
        this.symbolTables = new ArrayList<>();
        this.decls = compUnit.getDecls();
        this.funcDefs = compUnit.getFuncDefs();
        this.mainFuncDef = compUnit.getMainFuncDef();
        this.globalSymbolTable = new SymbolTable();
        symbolTables.add(globalSymbolTable);
    }

    public String getAllSymbolsAsString() {
        StringBuilder sb = new StringBuilder();
        for (SymbolTable symbolTable : symbolTables) {
            ArrayList<Symbol> symbolList = symbolTable.getSymbolList();
            for (Symbol symbol : symbolList) {
                sb.append(symbol).append("\n");
            }
        }
        return sb.toString();
    }

    public void visit() {
        for (Decl decl : decls) {
            visitDecl(decl, globalSymbolTable, 1);
        }
        for (FuncDef funcDef : funcDefs) {
            visitFuncDef(funcDef);
        }
        visitMainFuncDef();
    }

    private void visitConstExp(ConstExp constExp, SymbolTable symbolTable) {
        visitAddExp(constExp.getAddExp(), symbolTable);
    }

    private void visitConstInitVal(ConstInitVal constInitVal, SymbolTable symbolTable) {
        ConstInitValEle constInitValEle = constInitVal.getConstInitValEle();
        if (constInitValEle instanceof ConstExpSet) {
            ArrayList<ConstExp> constExps = ((ConstExpSet) constInitValEle).getConstExps();
            for (ConstExp constExp : constExps) {
                visitConstExp(constExp, symbolTable);
            }
        } else if (constInitValEle instanceof ConstExp) {
            visitConstExp((ConstExp) constInitValEle, symbolTable);
        }
    }

    private void visitConstDef(ConstDef constDef, String type, SymbolTable symbolTable, int scope) {
        String symbolType = "Const" + type;
        if (constDef.isArray()) {
            symbolType += "Array";
            visitConstExp(constDef.getConstExp(), symbolTable);
        }
        String name = constDef.getIdent().getIdenfr();
        int line = constDef.getIdent().getLine();
        visitConstInitVal(constDef.getConstInitVal(), symbolTable);
        SymbolCon symbolCon = new SymbolCon(symbolType, name, line, scope);
        symbolTable.addSymbol(symbolCon);
    }

    private void visitConstDecl(ConstDecl constDecl, SymbolTable symbolTable, int scope) {
        String type = constDecl.getBType().identifyType();
        ArrayList<ConstDef> constDefs = constDecl.getConstDefs();
        for (ConstDef constDef : constDefs) {
            visitConstDef(constDef, type, symbolTable, scope);
        }
    }

    private void visitUnaryFunc(UnaryFunc unaryFunc, SymbolTable symbolTable) {
        Ident funcIdent = unaryFunc.getIdent();
        if (ErrorHandler.handleErrorC(funcIdent, globalSymbolTable)) {
            return;
        }

        FuncRParams funcRParams = unaryFunc.getFuncRParams();
        if (ErrorHandler.handleErrorD(funcRParams, funcIdent, globalSymbolTable)) {
            return;
        }
        if (ErrorHandler.handleErrorE(funcRParams, funcIdent, symbolTable)) {
            return;
        }
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
        ErrorHandler.handleErrorC(ident, symbolTable);
        if (lVal.isArray()) {
            Exp exp = lVal.getExp();
            visitExp(exp, symbolTable);
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

    private void visitInitVal(InitVal initVal, SymbolTable symbolTable) {
        InitValEle initValEle = initVal.getInitValEle();
        if (initValEle instanceof ExpSet) {
            ArrayList<Exp> exps = ((ExpSet) initValEle).getExps();
            for (Exp exp : exps) {
                visitExp(exp, symbolTable);
            }
        } else if (initValEle instanceof Exp) {
            visitExp((Exp) initValEle, symbolTable);
        }
    }

    private void visitVarDef(VarDef varDef, String type, SymbolTable symbolTable, int scope) {
        String symbolType = type;
        if (varDef.isArray()) {
            symbolType += "Array";
            visitConstExp(varDef.getConstExp(), symbolTable);
        }
        String name = varDef.getIdent().getIdenfr();
        int line = varDef.getIdent().getLine();
        if (varDef.hasInitValue()) {
            visitInitVal(varDef.getInitVal(), symbolTable);
        }
        SymbolVar symbolVar = new SymbolVar(symbolType, name, line, scope);
        symbolTable.addSymbol(symbolVar);
    }

    private void visitVarDecl(VarDecl varDecl, SymbolTable symbolTable, int scope) {
        String type = varDecl.getBType().identifyType();
        ArrayList<VarDef> varDefs = varDecl.getVarDefs();
        for (VarDef varDef : varDefs) {
            visitVarDef(varDef, type, symbolTable, scope);
        }
    }

    private void visitDecl(Decl decl, SymbolTable symbolTable, int scope) {
        DeclEle declEle = decl.getDeclEle();
        if (declEle instanceof ConstDecl) {
            visitConstDecl((ConstDecl) declEle, symbolTable, scope);
        } else {
            visitVarDecl((VarDecl) declEle, symbolTable, scope);
        }
    }

    private void visitFuncDef(FuncDef funcDef) {
        /* visit FuncType and FuncIdent */
        String symbolType = funcDef.getFuncType().identifyFuncType() + "Func";
        String name = funcDef.getIdent().getIdenfr();
        int line = funcDef.getIdent().getLine();

        ArrayList<FuncFParam> funcFParamList = funcDef.getFuncFParams() == null ? new ArrayList<>() : funcDef.getFuncFParams().getFuncFParamList();
        SymbolFunc symbolFunc = new SymbolFunc(symbolType, name, line, 1, funcFParamList);
        globalSymbolTable.addSymbol(symbolFunc);

        /* extend symbolTable */
        int funcScopeNum = ++scopeNum;
        SymbolTable childSymbolTable = new SymbolTable(globalSymbolTable);
        symbolTables.add(childSymbolTable);

        /* visit FuncFParams */
        for (FuncFParam funcFParam : funcFParamList) {
            String type = funcFParam.getBType().identifyType();
            symbolType = funcFParam.isArray() ? type + "Array" : type;
            name = funcFParam.getIdent().getIdenfr();
            line = funcFParam.getIdent().getLine();
            SymbolVar symbolVar = new SymbolVar(symbolType, name, line, funcScopeNum);
            symbolFunc.addSymbol(symbolVar);
            childSymbolTable.addSymbol(symbolVar);
        }

        /* visit Block */
        visitBlock(funcDef.getBlock(), funcScopeNum, childSymbolTable, funcDef.getFuncType().getToken().getType(), false);

        /* handle Error G */
        ArrayList<BlockItem> blockItems = funcDef.getBlock().getBlockItems();
        ErrorHandler.handleErrorG(funcDef.getFuncType().getToken().getType(), blockItems, funcDef.getBlock().getRBrace().getLine());
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

    private void visitRelExp(RelExp relExp, SymbolTable symbolTable) {
        ArrayList<AddExp> addExps = relExp.getLowerExps();
        for (AddExp addExp : addExps) {
            visitAddExp(addExp, symbolTable);
        }
    }

    private void visitEqExp(EqExp eqExp, SymbolTable symbolTable) {
        ArrayList<RelExp> relExps = eqExp.getLowerExps();
        for (RelExp relExp : relExps) {
            visitRelExp(relExp, symbolTable);
        }
    }

    private void visitLAndExp(LAndExp lAndExp, SymbolTable symbolTable) {
        ArrayList<EqExp> EqExps = lAndExp.getLowerExps();
        for (EqExp eqExp : EqExps) {
            visitEqExp(eqExp, symbolTable);
        }
    }

    private void visitLOrExp(LOrExp lOrExp, SymbolTable symbolTable) {
        ArrayList<LAndExp> lAndExps = lOrExp.getLowerExps();
        for (LAndExp lAndExp : lAndExps) {
            visitLAndExp(lAndExp, symbolTable);
        }
    }

    private void visitCond(Cond cond, SymbolTable symbolTable) {
        visitLOrExp(cond.getlOrExp(), symbolTable);
    }

    private void visitForStmt(ForStmt forStmt, SymbolTable symbolTable) {
        visitLVal(forStmt.getlVal(), symbolTable);
        visitExp(forStmt.getExp(), symbolTable);
    }

    private void visitStmtFor(StmtFor stmtFor, SymbolTable symbolTable, Token.Type type) {
        if (stmtFor.getForStmt1() != null) {
            ErrorHandler.handleErrorH(stmtFor.getForStmt1().getlVal(), symbolTable);
            visitForStmt(stmtFor.getForStmt1(), symbolTable);
        }
        if (stmtFor.getCond() != null) {
            visitCond(stmtFor.getCond(), symbolTable);
        }
        if (stmtFor.getForStmt2() != null) {
            ErrorHandler.handleErrorH(stmtFor.getForStmt2().getlVal(), symbolTable);
            visitForStmt(stmtFor.getForStmt2(), symbolTable);
        }
        visitStmt(stmtFor.getStmt(), symbolTable, type, true);
    }

    private void visitStmtIf(StmtIf stmtIf, SymbolTable symbolTable, Token.Type type, boolean isInFor) {
        visitCond(stmtIf.getCond(), symbolTable);
        visitStmt(stmtIf.getStmt1(), symbolTable, type, isInFor);
        if (stmtIf.getStmt2() != null) {
            visitStmt(stmtIf.getStmt2(), symbolTable, type, isInFor);
        }
    }

    private void visitStmt(Stmt stmt, SymbolTable symbolTable, Token.Type type, boolean isInFor) {
        StmtEle stmtEle = stmt.getStmtEle();
        if (stmtEle instanceof StmtAssign) {
            ErrorHandler.handleErrorH(((StmtAssign) stmtEle).getlVal(), symbolTable);
            visitLVal(((StmtAssign) stmtEle).getlVal(), symbolTable);
            visitExp(((StmtAssign) stmtEle).getExp(), symbolTable);
        } else if (stmtEle instanceof StmtExp) {
            visitExp(((StmtExp) stmtEle).getExp(), symbolTable);
        } else if (stmtEle instanceof StmtGetInt) {
            ErrorHandler.handleErrorH(((StmtGetInt) stmtEle).getlVal(), symbolTable);
            visitLVal(((StmtGetInt) stmtEle).getlVal(), symbolTable);
        } else if (stmtEle instanceof StmtGetChar) {
            ErrorHandler.handleErrorH(((StmtGetChar) stmtEle).getlVal(), symbolTable);
            visitLVal(((StmtGetChar) stmtEle).getlVal(), symbolTable);
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
            ErrorHandler.handleErrorL((StmtPrint) stmtEle);
        } else if (stmtEle instanceof StmtReturn) {
            if (((StmtReturn) stmtEle).existReturnValue()) {
                visitExp(((StmtReturn) stmtEle).getExp(), symbolTable);
            }
            ErrorHandler.handleErrorF(type, (StmtReturn) stmt.getStmtEle());
        } else {
            ErrorHandler.handleErrorM(isInFor, stmt.getStmtEle());
        }
    }

    private void visitMainFuncDef() {
        /* visit Block */
        SymbolTable childSymbolTable = new SymbolTable(globalSymbolTable);
        symbolTables.add(childSymbolTable);
        visitBlock(mainFuncDef.getBlock(), ++scopeNum, childSymbolTable, Token.Type.INTTK, false);

        /* handle Error G */
        ArrayList<BlockItem> blockItems = mainFuncDef.getBlock().getBlockItems();
        ErrorHandler.handleErrorG(Token.Type.INTTK, blockItems, mainFuncDef.getBlock().getRBrace().getLine());
    }

}
