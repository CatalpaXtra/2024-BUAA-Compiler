package midend.llvm;

import frontend.lexer.Token;
import frontend.parser.block.Block;
import frontend.parser.block.BlockItem;
import frontend.parser.block.BlockItemEle;
import frontend.parser.block.statement.Stmt;
import frontend.parser.block.statement.StmtEle;
import frontend.parser.block.statement.stmtVariant.*;
import frontend.parser.declaration.Decl;
import frontend.parser.expression.Exp;
import frontend.parser.expression.add.AddExp;
import frontend.parser.expression.cond.*;
import frontend.parser.expression.primary.LVal;
import frontend.parser.terminal.Ident;
import midend.symbol.Symbol;
import midend.symbol.SymbolTable;

import java.util.ArrayList;

public class LocalStmt {
    private static Module module;
    public static int nextLabel;

    public static void setLocalStmt(Module md) {
        module = md;
    }

    public static void visitBlock(Block block, SymbolTable symbolTable, Token.Type type, boolean isInFor) {
        ArrayList<BlockItem> blockItems = block.getBlockItems();
        for (BlockItem blockItem : blockItems) {
            BlockItemEle blockItemEle = blockItem.getBlockItemEle();
            if (blockItemEle instanceof Decl) {
                LocalDecl.visitDecl((Decl) blockItemEle, symbolTable);
            } else {
                visitStmt((Stmt) blockItemEle, symbolTable, type, isInFor);
            }
        }
    }

    private static void visitStmt(Stmt stmt, SymbolTable symbolTable, Token.Type type, boolean isInFor) {
        StmtEle stmtEle = stmt.getStmtEle();
        if (stmtEle instanceof StmtAssign) {
            visitStmtAssign((StmtAssign) stmtEle, symbolTable);
        } else if (stmtEle instanceof StmtGetInt) {
            visitStmtGetInt((StmtGetInt) stmtEle, symbolTable);
        } else if (stmtEle instanceof StmtGetChar) {
            visitStmtGetChar((StmtGetChar) stmtEle, symbolTable);
        } else if (stmtEle instanceof StmtExp) {
            // TODO no need?
            LocalDecl.visitExp(((StmtExp) stmtEle).getExp(), symbolTable);
        } else if (stmtEle instanceof StmtPrint) {
            visitStmtPrint((StmtPrint) stmtEle, symbolTable);
        }

        else if (stmtEle instanceof StmtIf) {
            visitStmtIf((StmtIf) stmtEle, symbolTable, type, isInFor);
        } else if (stmtEle instanceof StmtFor) {
            visitStmtFor((StmtFor) stmtEle, symbolTable, type);
        }

        else if (stmtEle instanceof Block) {
            SymbolTable childSymbolTable = new SymbolTable(symbolTable);
            visitBlock((Block) stmtEle, childSymbolTable, type, isInFor);
        } else if (stmtEle instanceof StmtReturn) {
            if (((StmtReturn) stmtEle).existReturnValue()) {
                RetValue result = LocalDecl.visitExp(((StmtReturn) stmtEle).getExp(), symbolTable);
                module.addCode("ret i32 " + result.irOut());
            } else {
                module.addCode("ret void");
            }
        } else if (stmtEle instanceof StmtBreak) {
            module.addCode("br label <BLOCK2 OR STMT>");
        } else if (stmtEle instanceof StmtContinue) {
            module.addCode("br label <FORSTMT2>");
        }
    }

    private static void visitStmtAssign(StmtAssign stmtAssign, SymbolTable symbolTable) {
        Ident ident = stmtAssign.getlVal().getIdent();
        Symbol symbol = symbolTable.getSymbol(ident.getIdenfr());
        String memory = symbol.getMemory();
        RetValue result = LocalDecl.visitExp(stmtAssign.getExp(), symbolTable);
        module.addCode("store i32 " + result.irOut() + ", i32* " + memory);
    }

    private static void visitStmtGetInt(StmtGetInt stmtGetInt, SymbolTable symbolTable) {
        int memoryReg = Register.allocReg();
        module.addCode("%" + memoryReg + " = call i32 @getint()");
        Ident ident = stmtGetInt.getlVal().getIdent();
        Symbol symbol = symbolTable.getSymbol(ident.getIdenfr());
        String memory = symbol.getMemory();
        module.addCode("store i32 %" + memoryReg + ", i32* " + memory);
    }

    private static void visitStmtGetChar(StmtGetChar stmtGetChar, SymbolTable symbolTable) {
        int memoryReg = Register.allocReg();
        module.addCode("%" + memoryReg + " = call i32 @getchar()");
        Ident ident = stmtGetChar.getlVal().getIdent();
        Symbol symbol = symbolTable.getSymbol(ident.getIdenfr());
        String memory = symbol.getMemory();
        module.addCode("store i32 %" + memoryReg + ", i32* " + memory);
    }

    private static void visitStmtPrint(StmtPrint stmtPrint, SymbolTable symbolTable) {
        if (stmtPrint.getExpNum() > 0) {
            ArrayList<Exp> exps = stmtPrint.getExps();
            for (Exp exp : exps) {
                // TODO
                // LocalDecl.visitExp(exp, symbolTable);
            }
        }
    }

    private static void visitStmtIf(StmtIf stmtIf, SymbolTable symbolTable, Token.Type type, boolean isInFor) {
        /* Handle Cond */
        LocalStmt.nextLabel = Register.allocReg();
        module.addCode("br label %" + LocalStmt.nextLabel);
        module.addCode("");
        int left = module.getLoc() + 1;
        LocalStmt.visitCond(stmtIf.getCond(), symbolTable);
        int right = module.getLoc();
        module.addCode(LocalStmt.nextLabel + ":");

        /* Handle Stmt1 */
        visitStmt(stmtIf.getStmt1(), symbolTable, type, isInFor);
        module.replaceInterval(left, right, "%" + (LocalStmt.nextLabel + 1), "<BLOCK2 OR STMT>");

        /* Handle Stmt2 */
        if (stmtIf.getStmt2() != null) {
            int replaceLoc = module.getLoc() + 1;
            /* Branch to Next Stmt */
            LocalStmt.nextLabel = Register.allocReg();
            module.addCode("br label <NEXT STMT>");
            module.addCode("");
            module.addCode(LocalStmt.nextLabel + ":");

            visitStmt(stmtIf.getStmt2(), symbolTable, type, isInFor);
            module.replaceInterval(replaceLoc, replaceLoc, "%" + (LocalStmt.nextLabel + 1), "<NEXT STMT>");
        }

        /* Branch To Next Stmt */
        LocalStmt.nextLabel = Register.allocReg();
        module.addCode("br label %" + LocalStmt.nextLabel);
        module.addCode("");
        module.addCode(LocalStmt.nextLabel + ":");
    }

    private static void visitStmtFor(StmtFor stmtFor, SymbolTable symbolTable, Token.Type type) {
        /* Handle ForStmt1 */
        if (stmtFor.getForStmt1() != null) {
            visitForStmt(stmtFor.getForStmt1(), symbolTable);
        }

        /* Handle Cond */
        int condLabel = Register.getRegNum();
        int left = module.getLoc() + 1;
        if (stmtFor.getCond() != null) {
            LocalStmt.nextLabel = Register.allocReg();
            module.addCode("br label %" + LocalStmt.nextLabel);
            module.addCode("");
            LocalStmt.visitCond(stmtFor.getCond(), symbolTable);
            module.addCode(LocalStmt.nextLabel + ":");
        } else {
            /* Directly Branch to Stmt */
            LocalStmt.nextLabel = Register.allocReg();
            module.addCode("br label %" + LocalStmt.nextLabel);
            module.addCode("");
            module.addCode(LocalStmt.nextLabel + ":");
        }

        /* Handle Stmt */
        visitStmt(stmtFor.getStmt(), symbolTable, type, true);
        int right = module.getLoc();
        /* Branch to ForStmt2 */
        LocalStmt.nextLabel = Register.allocReg();
        module.addCode("br label %" + LocalStmt.nextLabel);
        module.addCode("");
        module.addCode(LocalStmt.nextLabel + ":");

        /* Handle ForStmt2 */
        int forstmt2Label = LocalStmt.nextLabel;
        if (stmtFor.getForStmt2() != null) {
            visitForStmt(stmtFor.getForStmt2(), symbolTable);
        }

        /* Reach Loop Bottom */
        LocalStmt.nextLabel = Register.allocReg();
        /* Branch to Cond */
        module.addCode("br label %" + condLabel);
        module.addCode("");
        module.addCode(LocalStmt.nextLabel + ":");

        module.replaceInterval(left, right, "%" + LocalStmt.nextLabel, "<BLOCK2 OR STMT>");
        module.replaceInterval(left, right, "%" + forstmt2Label, "<FORSTMT2>");
    }

    private static void visitForStmt(ForStmt forStmt, SymbolTable symbolTable) {
        LVal lVal = forStmt.getlVal();
        if (lVal.isArray()) {
            // TODO
        }
        Symbol symbol = symbolTable.getSymbol(lVal.getIdent().getIdenfr());
        String memory = symbol.getMemory();
        RetValue result = LocalDecl.visitExp(forStmt.getExp(), symbolTable);
        module.addCode("store i32 " + result.irOut() + ", i32* " + memory);
    }

    private static void visitCond(Cond cond, SymbolTable symbolTable) {
        visitLOrExp(cond.getlOrExp(), symbolTable);
    }

    private static void visitSingleLOrExp(LAndExp lAndExp, SymbolTable symbolTable) {
        module.addCode(nextLabel + ":");
        int left = module.getLoc() + 1;
        RetValue result = visitLAndExp(lAndExp, symbolTable, true);
        if (result.isDigit()) {
            if (result.getValue() != 0) {
                /* Cond is true, Jump to Stmt1 */
                module.delLastCode();
                return;
            } else {
                /* Cond is false, Jump to BLOCK2 OR STMT */
                module.addCode("br label <BLOCK2 OR STMT>");
                module.addCode("");
                nextLabel = Register.allocReg();
            }
        } else if (result.isReg()) {
            nextLabel = Register.allocReg();
            module.addCode("br i1 " + result.irOut() + ", label %" + nextLabel + ", label <BLOCK2 OR STMT>");
            module.addCode("");
        }
        module.replaceInterval(left, module.getLoc(), "%" + nextLabel, "<BLOCK1>");
    }

    private static void visitLOrExp(LOrExp lOrExp, SymbolTable symbolTable) {
        ArrayList<LAndExp> lAndExps = lOrExp.getLowerExps();
        if (lAndExps.size() == 1) {
            visitSingleLOrExp(lAndExps.get(0), symbolTable);
            return;
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
                RetValue temp = new RetValue(Register.allocReg(), 1);
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
                    return new RetValue(nextLabel, 2);
                } else {
                    /* Cond may be false, continue */
                    module.delLastCode();
                }
            } else if (result.isReg()) {
                /* Return Value In Register */
                RetValue temp = new RetValue(Register.allocReg(), 1);
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
        return new RetValue(nextLabel, 2);
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
            result = new RetValue(Register.allocReg(), 1);

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
            result = new RetValue(Register.allocReg(), 1);

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
