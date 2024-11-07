package midend.llvm;

import frontend.lexer.Token;
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
    public static int nextLabel;

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
        nextLabel = Register.allocReg();
        module.addCode("br label %" + nextLabel);
        module.addCode("");

        visitLOrExp(cond.getlOrExp(), symbolTable);

        module.addCode(nextLabel + ":");
    }

    private static void visitLOrExp(LOrExp lOrExp, SymbolTable symbolTable) {
        ArrayList<LAndExp> lAndExps = lOrExp.getLowerExps();
        if (lAndExps.size() == 1) {
            // TODO optimize single
//            module.addCode(nextLabel + ":");
//            RetValue result = visitLAndExp(lAndExps.get(0), symbolTable, true);
//            if (result.isReg() || result.isDigit()) {
//                module.addCode("br i1 " + result.irOut() + ", label <BLOCK1>, label <BLOCK2 OR STMT>");
//                module.addCode("");
//            }
//            return;
        }

        int left = module.getLoc() + 1;
        for (int i = 0; i < lAndExps.size(); i++) {
            module.addCode(nextLabel + ":");
            RetValue result = visitLAndExp(lAndExps.get(i), symbolTable, i == lAndExps.size() - 1);
            if (result.isDigit()) {
                if (result.getValue() != 0) {
                    /* Cond is true, end */
                    nextLabel = Register.allocReg();
                    module.addCode("br label <BLOCK1>");
                    module.addCode("");
                    module.replaceInterval(left, module.getLoc(), "%" + nextLabel, "<BLOCK1>");
                    return;
                } else {
                    /* Cond may be true, continue */
                    module.delLastCode();
                }
            } else if (result.isReg()) {
                /* Return Value In Register */
                RetValue temp = new RetValue(Register.allocReg(), false);
                module.addCode(temp.irOut() + " = icmp ne i32 " + result.irOut() + ", 0");

                nextLabel = Register.allocReg();
                if (i == lAndExps.size() - 1) {
                    module.addCode("br i1 " + temp.irOut() + ", label <BLOCK1>, label <BLOCK2 OR STMT>");
                } else {
                    module.addCode("br i1 " + temp.irOut() + ", label <BLOCK1>, label %" + nextLabel);
                }
                module.addCode("");
            }
        }
        module.replaceInterval(left, module.getLoc(), "%" + nextLabel, "<BLOCK1>");
    }

    private static RetValue visitLAndExp(LAndExp lAndExp, SymbolTable symbolTable, boolean isLast) {
        ArrayList<EqExp> eqExps = lAndExp.getLowerExps();
        if (eqExps.size() == 1) {
            return visitEqExp(eqExps.get(0), symbolTable);
        }

        int left = module.getLoc() + 1;
        module.delLastCode();
        for (int i = 0; i < eqExps.size(); i++) {
            module.addCode(nextLabel + ":");
            RetValue result = visitEqExp(eqExps.get(i), symbolTable);
            if (result.isDigit()) {
                if (result.getValue() == 0) {
                    /* Cond is false, end */
                    module.addCode("br label <NEXT LOREXP>");
                    module.addCode("");

                    nextLabel = Register.allocReg();
                    if (isLast) {
                        module.replaceInterval(left, module.getLoc(), "<BLOCK2 OR STMT>", "<NEXT LOREXP>");
                    }
                    return new RetValue(nextLabel, false, true);
                } else {
                    /* Cond may be false, continue */
                    module.delLastCode();
                }
            } else if (result.isReg()) {
                /* Return Value In Register */
                RetValue temp = new RetValue(Register.allocReg(), false);
                module.addCode(temp.irOut() + " = icmp ne i32 " + result.irOut() + ", 0");

                nextLabel = Register.allocReg();
                if (i == eqExps.size() - 1) {
                    module.addCode("br i1 " + temp.irOut() + ", label <BLOCK1>, label <NEXT LOREXP>");
                } else {
                    module.addCode("br i1 " + temp.irOut() + ", label %" + nextLabel + ", label <NEXT LOREXP>");
                }
                module.addCode("");
            }
        }
        if (isLast) {
            module.replaceInterval(left, module.getLoc(), "<BLOCK2 OR STMT>", "<NEXT LOREXP>");
        } else {
            module.replaceInterval(left, module.getLoc(), "%" + nextLabel, "<NEXT LOREXP>");
        }
        return new RetValue(nextLabel, false, true);
    }

    private static RetValue visitEqExp(EqExp eqExp, SymbolTable symbolTable) {
        ArrayList<RelExp> relExps = eqExp.getLowerExps();
        if (relExps.size() == 1) {
            return visitRelExp(relExps.get(0), symbolTable);
        }

        ArrayList<Token> operators = eqExp.getOperators();
        RetValue result = visitRelExp(relExps.get(0), symbolTable);
        for (int i = 1; i < relExps.size(); i++) {
            // TODO value transfer

            String cond = condTransfer(operators.get(i-1).getType());
            RetValue left = result;
            RetValue right = visitRelExp(relExps.get(i), symbolTable);
            result = new RetValue(Register.allocReg(), false);

            module.addCode(result.irOut() + " = icmp " + cond + " i32 " + left.irOut() + ", " + right.irOut());
        }
        return result;
    }

    private static RetValue visitRelExp(RelExp relExp, SymbolTable symbolTable) {
        ArrayList<AddExp> addExps = relExp.getLowerExps();
        if (addExps.size() == 1) {
            return LocalDecl.visitAddExp(addExps.get(0), symbolTable);
        }

        ArrayList<Token> operators = relExp.getOperators();
        RetValue result = LocalDecl.visitAddExp(addExps.get(0), symbolTable);
        for (int i = 1; i < addExps.size(); i++) {
            // TODO value transfer

            String cond = condTransfer(operators.get(i-1).getType());
            RetValue left = result;
            RetValue right = LocalDecl.visitAddExp(addExps.get(i), symbolTable);
            result = new RetValue(Register.allocReg(), false);

            module.addCode(result.irOut() + " = icmp " + cond + " i32 " + left.irOut() + ", " + right.irOut());
        }
        return result;
    }

    private static String condTransfer(Token.Type type) {
        switch (type) {
            case GRE:
                return "sgt";
            case GEQ:
                return "sge";
            case LSS:
                return "slt";
            case LEQ:
                return "sle";
            case EQL:
                return "eq";
            case NEQ:
                return "ne";
            default:
                System.out.println("Cond Reach Unknown Branch");
                return "ERROR";
        }
    }
}
