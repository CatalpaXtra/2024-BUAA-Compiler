package midend;

import frontend.lexer.Token;
import frontend.parser.CompUnit;
import frontend.parser.block.Block;
import frontend.parser.block.BlockItem;
import frontend.parser.block.BlockItemEle;
import frontend.parser.block.statement.Stmt;
import frontend.parser.block.statement.StmtEle;
import frontend.parser.block.statement.stmtVariant.StmtFor;
import frontend.parser.block.statement.stmtVariant.StmtIf;
import frontend.parser.block.statement.stmtVariant.StmtPrint;
import frontend.parser.declaration.Decl;
import frontend.parser.declaration.DeclEle;
import frontend.parser.declaration.constDecl.ConstDecl;
import frontend.parser.declaration.constDecl.ConstDef;
import frontend.parser.declaration.varDecl.VarDecl;
import frontend.parser.declaration.varDecl.VarDef;
import frontend.parser.function.FuncDef;
import frontend.parser.function.MainFuncDef;
import frontend.parser.function.params.FuncFParam;
import midend.symbol.*;

import java.util.ArrayList;

public class Semantic {
    private static int scopeNum = 1;
    private final ArrayList<SymbolTable> symbolTables;
    private final ArrayList<Decl> decls;
    private final ArrayList<FuncDef> funcDefs;
    private final MainFuncDef mainFuncDef;

    public Semantic(CompUnit compUnit) {
        this.symbolTables = new ArrayList<>();
        this.decls = compUnit.getDecls();
        this.funcDefs = compUnit.getFuncDefs();
        this.mainFuncDef = compUnit.getMainFuncDef();
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

    public void analyze() {
        SymbolTable symbolTable = new SymbolTable();
        symbolTables.add(symbolTable);
        for (Decl decl : decls) {
            analyzeDecl(decl, 1, symbolTable);
        }
        for (FuncDef funcDef : funcDefs) {
            analyzeFuncDef(funcDef, symbolTable);
        }
        analyzeMainFuncDef(symbolTable);
    }

    private void analyzeDecl(Decl decl, int scope, SymbolTable symbolTable) {
        DeclEle declEle = decl.getDeclEle();
        if (declEle instanceof ConstDecl) {
            String type = ((ConstDecl) declEle).getBType().identifyType();
            ArrayList<ConstDef> constDefs = ((ConstDecl) declEle).getConstDefs();
            for (ConstDef constDef : constDefs) {
                String symbolType = constDef.isArray() ? "Const" + type + "Array" : "Const" + type;
                String name = constDef.getIdent().getIdenfr();
                int line = constDef.getIdent().getLine();
                SymbolCon symbolCon = new SymbolCon(symbolType, name, line, scope);
                symbolTable.addSymbol(symbolCon);
            }
        } else {
            String type = ((VarDecl) declEle).getBType().identifyType();
            ArrayList<VarDef> varDefs = ((VarDecl) declEle).getVarDefs();
            for (VarDef varDef : varDefs) {
                String symbolType = varDef.isArray() ? type + "Array" : type;
                String name = varDef.getIdent().getIdenfr();
                int line = varDef.getIdent().getLine();
                SymbolVar symbolVar = new SymbolVar(symbolType, name, line, scope);
                symbolTable.addSymbol(symbolVar);
            }
        }
    }

    private void analyzeFuncDef(FuncDef funcDef, SymbolTable symbolTable) {
        /* analyze FuncType and FuncIdent */
        String symbolType = funcDef.getFuncType().identifyFuncType() + "Func";
        String name = funcDef.getIdent().getIdenfr();
        int line = funcDef.getIdent().getLine();
        SymbolFunc symbolFunc = new SymbolFunc(symbolType, name, line, 1);
        symbolTable.addSymbol(symbolFunc);

        /* extend symbolTable */
        int funcScopeNum = ++scopeNum;
        SymbolTable childSymbolTable = new SymbolTable(symbolTable);
        symbolTables.add(childSymbolTable);

        /* analyze FuncFParams */
        ArrayList<FuncFParam> funcFParamList = funcDef.getFuncFParams() == null ? new ArrayList<>() : funcDef.getFuncFParams().getFuncFParamList();
        for (FuncFParam funcFParam : funcFParamList) {
            String type = funcFParam.getBType().identifyType();
            symbolType = funcFParam.isArray() ? type + "Array" : type;
            name = funcFParam.getIdent().getIdenfr();
            line = funcFParam.getIdent().getLine();
            SymbolVar symbolVar = new SymbolVar(symbolType, name, line, funcScopeNum);
            childSymbolTable.addSymbol(symbolVar);
        }

        /* analyze Block */
        analyzeBlock(funcDef.getBlock(), funcScopeNum, childSymbolTable, funcDef.getFuncType().getToken().getType(), false);

        /* handle Error G */
        ArrayList<BlockItem> blockItems = funcDef.getBlock().getBlockItems();
        ErrorHandler.handleErrorG(funcDef.getFuncType().getToken().getType(), blockItems, funcDef.getBlock().getRBrace().getLine());
    }

    private void analyzeBlock(Block block, int scope, SymbolTable symbolTable, Token.Type type, boolean isInFor) {
        ArrayList<BlockItem> blockItems = block.getBlockItems();
        for (BlockItem blockItem : blockItems) {
            BlockItemEle blockItemEle = blockItem.getBlockItemEle();
            if (blockItemEle instanceof Decl) {
                analyzeDecl((Decl) blockItemEle, scope, symbolTable);
            } else {
                analyzeStmt((Stmt) blockItemEle, symbolTable, type, isInFor);
                ErrorHandler.handleErrorF(type, ((Stmt) blockItemEle).getStmtEle());
                ErrorHandler.handleErrorM(isInFor, ((Stmt) blockItemEle).getStmtEle());
            }
        }
    }

    private void analyzeStmt(Stmt stmt, SymbolTable symbolTable, Token.Type type, boolean isInFor) {
        StmtEle stmtEle = stmt.getStmtEle();
        if (stmtEle instanceof StmtFor) {
            analyzeStmt(((StmtFor) stmtEle).getStmt(), symbolTable, type, true);
        } else if (stmtEle instanceof StmtIf) {
            analyzeStmt(((StmtIf) stmtEle).getStmt1(), symbolTable, type, isInFor);
            if (((StmtIf) stmtEle).getStmt2() != null) {
                analyzeStmt(((StmtIf) stmtEle).getStmt2(), symbolTable, type, isInFor);
            }
        } else if (stmtEle instanceof Block) {
            SymbolTable childSymbolTable = new SymbolTable(symbolTable);
            symbolTables.add(childSymbolTable);
            analyzeBlock((Block) stmtEle, ++scopeNum, childSymbolTable, type, isInFor);
        } else if (stmtEle instanceof StmtPrint) {
            ErrorHandler.handleErrorL((StmtPrint) stmtEle);
        }
    }

    private void analyzeMainFuncDef(SymbolTable symbolTable) {
        /* analyze Block */
        SymbolTable childSymbolTable = new SymbolTable(symbolTable);
        symbolTables.add(childSymbolTable);
        analyzeBlock(mainFuncDef.getBlock(), ++scopeNum, childSymbolTable, Token.Type.INTTK, false);

        /* handle Error G */
        ArrayList<BlockItem> blockItems = mainFuncDef.getBlock().getBlockItems();
        ErrorHandler.handleErrorG(Token.Type.INTTK, blockItems, mainFuncDef.getBlock().getRBrace().getLine());
    }

}
