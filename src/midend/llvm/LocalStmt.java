package midend.llvm;

import frontend.parser.block.statement.stmtVariant.StmtAssign;
import frontend.parser.block.statement.stmtVariant.StmtGetChar;
import frontend.parser.block.statement.stmtVariant.StmtGetInt;
import frontend.parser.expression.add.AddExp;
import frontend.parser.expression.cond.*;
import frontend.parser.terminal.Ident;
import midend.symbol.Symbol;
import midend.symbol.SymbolTable;

import java.util.ArrayList;

public class LocalStmt {
    private static SymbolTable globalSymbolTable;
    private static Module module;

    public static void setLocalStmt(SymbolTable symbolTable, Module md) {
        globalSymbolTable = symbolTable;
        module = md;
    }

    public static void visitStmtAssign(StmtAssign stmtAssign, SymbolTable symbolTable) {
        Ident ident = stmtAssign.getlVal().getIdent();
        Symbol symbol = symbolTable.getSymbol(ident.getIdenfr());
        String memory = symbol.getMemory();
        RetValue result = LocalDecl.visitExp(stmtAssign.getExp(), symbolTable);
        module.addCode("store i32 " + result.irOut() + ", i32* " + memory);
    }

    public static void visitStmtGetInt(StmtGetInt stmtGetInt, SymbolTable symbolTable) {
        int memoryReg = Register.allocReg();
        module.addCode("%" + memoryReg + " = call i32 @getint()");
        Ident ident = stmtGetInt.getlVal().getIdent();
        Symbol symbol = symbolTable.getSymbol(ident.getIdenfr());
        String memory = symbol.getMemory();
        module.addCode("store i32 %" + memoryReg + ", i32* " + memory);
    }

    public static void visitStmtGetChar(StmtGetChar stmtGetChar, SymbolTable symbolTable) {
        int memoryReg = Register.allocReg();
        module.addCode("%" + memoryReg + " = call i32 @getchar()");
        Ident ident = stmtGetChar.getlVal().getIdent();
        Symbol symbol = symbolTable.getSymbol(ident.getIdenfr());
        String memory = symbol.getMemory();
        module.addCode("store i32 %" + memoryReg + ", i32* " + memory);
    }

    public static void visitCond(Cond cond, SymbolTable symbolTable) {
        int memoryReg = Register.allocReg();
        module.addCode("br label %" + memoryReg);
        module.addCode("");
        module.addCode(memoryReg + ":");
        visitLOrExp(cond.getlOrExp(), symbolTable);
    }

    private static RetValue visitLOrExp(LOrExp lOrExp, SymbolTable symbolTable) {
        ArrayList<LAndExp> lAndExps = lOrExp.getLowerExps();
        RetValue result = visitLAndExp(lAndExps.get(0), symbolTable);
        if (result.isDigit()) {
            if (result.getValue() != 0) {
                // end
            } else {
                // ignore but continue
            }
        } else {
            // create register to jump
        }

        for (int i = 1; i < lAndExps.size(); i++) {
            result = visitLAndExp(lAndExps.get(i), symbolTable);
        }
        return result;
    }

    private static RetValue visitLAndExp(LAndExp lAndExp, SymbolTable symbolTable) {
        ArrayList<EqExp> eqExps = lAndExp.getLowerExps();
        RetValue result = visitEqExp(eqExps.get(0), symbolTable);
        return result;
    }

    private static RetValue visitEqExp(EqExp eqExp, SymbolTable symbolTable) {
        ArrayList<RelExp> relExps = eqExp.getLowerExps();
        RetValue result = visitRelExp(relExps.get(0), symbolTable);
        return result;
    }

    private static RetValue visitRelExp(RelExp relExp, SymbolTable symbolTable) {
        ArrayList<AddExp> addExps = relExp.getLowerExps();
        RetValue result = LocalDecl.visitAddExp(addExps.get(0), symbolTable);
        return result;
    }
}
