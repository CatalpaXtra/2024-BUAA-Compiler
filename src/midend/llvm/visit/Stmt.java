package midend.llvm.visit;

import frontend.lexer.Token;
import frontend.parser.block.Block;
import frontend.parser.block.BlockItem;
import frontend.parser.block.BlockItemEle;
import frontend.parser.block.statement.StmtEle;
import frontend.parser.block.statement.stmtVariant.*;
import frontend.parser.declaration.Decl;
import frontend.parser.expression.Exp;
import frontend.parser.expression.add.AddExp;
import frontend.parser.expression.cond.EqExp;
import frontend.parser.expression.cond.LAndExp;
import frontend.parser.expression.cond.LOrExp;
import frontend.parser.expression.cond.RelExp;
import frontend.parser.expression.primary.LVal;
import midend.llvm.*;
import midend.llvm.Module;
import midend.llvm.function.IrBlock;
import midend.llvm.global.GlobalStr;
import midend.llvm.symbol.*;

import java.util.ArrayList;

public class Stmt {
    private static IrBlock irBlock;
    private static String funcType;
    public static int nextLabel;

    public static void setStmt(IrBlock irBlock, String funcType) {
        Stmt.irBlock = irBlock;
        Stmt.funcType = funcType;
    }

    public static boolean visitBlock(Block block, SymbolTable symbolTable, Token.Type type, boolean isInFor) {
        ArrayList<BlockItem> blockItems = block.getBlockItems();
        for (BlockItem blockItem : blockItems) {
            BlockItemEle blockItemEle = blockItem.getBlockItemEle();
            if (blockItemEle instanceof Decl) {
                LocalDecl.visitDecl((Decl) blockItemEle, symbolTable);
            } else {
                visitStmt((frontend.parser.block.statement.Stmt) blockItemEle, symbolTable, type, isInFor);
                StmtEle stmtEle = ((frontend.parser.block.statement.Stmt) blockItemEle).getStmtEle();
                if (stmtEle instanceof StmtReturn || stmtEle instanceof StmtBreak || stmtEle instanceof StmtContinue) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean visitStmt(frontend.parser.block.statement.Stmt stmt, SymbolTable symbolTable, Token.Type type, boolean isInFor) {
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
            return visitBlock((Block) stmtEle, childSymbolTable, type, isInFor);
        } else if (stmtEle instanceof StmtReturn) {
            visitStmtReturn((StmtReturn) stmtEle, symbolTable);
            return true;
        } else if (stmtEle instanceof StmtBreak) {
            irBlock.addInstrBr("<BLOCK2 OR STMT>");
            return true;
        } else if (stmtEle instanceof StmtContinue) {
            irBlock.addInstrBr("<FORSTMT2>");
            return true;
        }
        return false;
    }

    private static void visitStmtReturn(StmtReturn stmtReturn, SymbolTable symbolTable) {
        if (stmtReturn.existReturnValue()) {
            Value result = LocalDecl.visitExp(stmtReturn.getExp(), symbolTable);
            if (funcType.contains("Char")) {
                result = irBlock.addInstrTrunc("%"+Register.allocReg(), "i32", result, "i8");
            }
            irBlock.addInstrRet(Support.varTransfer(funcType), result);
        } else {
            irBlock.addInstrRet("void", null);
        }
    }

    private static void storeLVal(Value result, LVal lVal, SymbolTable symbolTable) {
        /* LVal Must Be Var */
        Symbol symbol = symbolTable.getSymbol(lVal.getIdent().getIdenfr());
        Value memory = symbol.getMemory();
        String irType = Support.varTransfer(symbol.getSymbolType());
        if (lVal.isArray()) {
            Value loc = LocalDecl.visitExp(lVal.getExp(), symbolTable);
            Value temp;
            if (symbol.isPointer()) {
                temp = irBlock.addInstrLoad("%"+Register.allocReg(), irType + "*", memory);
                temp = irBlock.addInstrGetelementptr("%"+Register.allocReg(), -1, irType, temp, loc);
            } else {
                temp = irBlock.addInstrGetelementptr("%"+Register.allocReg(), symbol.getArraySize(), irType, memory, loc);
            }
            if (symbol.isChar()) {
                result = irBlock.addInstrTrunc("%"+Register.allocReg(), "i32", result, "i8");
            }
            irBlock.addInstrStore(irType, result, temp);
        } else {
            if (symbol.isChar()) {
                result = irBlock.addInstrTrunc("%"+Register.allocReg(), "i32", result, "i8");
            }
            irBlock.addInstrStore(irType, result, memory);
        }
    }

    private static void visitStmtAssign(StmtAssign stmtAssign, SymbolTable symbolTable) {
        Value result = LocalDecl.visitExp(stmtAssign.getExp(), symbolTable);
        storeLVal(result, stmtAssign.getlVal(), symbolTable);
    }

    private static void visitStmtGetInt(StmtGetInt stmtGetInt, SymbolTable symbolTable) {
        Value result = irBlock.addInstrCall("%"+Register.allocReg(), "i32", "getint", "");
        storeLVal(result, stmtGetInt.getlVal(), symbolTable);
    }

    private static void visitStmtGetChar(StmtGetChar stmtGetChar, SymbolTable symbolTable) {
        Value result = irBlock.addInstrCall("%"+Register.allocReg(), "i32", "getchar", "");
        storeLVal(result, stmtGetChar.getlVal(), symbolTable);
    }

    private static void visitStmtPrint(StmtPrint stmtPrint, SymbolTable symbolTable) {
        String string = stmtPrint.getStringConst().getToken().getContent();
        ArrayList<String> parts = Support.splitPrintString(string);
        ArrayList<Exp> exps = stmtPrint.getExps();
        int expCount = 0;
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).equals("%d") || parts.get(i).equals("%c")) {
                Value result = LocalDecl.visitExp(exps.get(expCount), symbolTable);
                if (parts.get(i).equals("%d")) {
                    irBlock.addInstrCall(null, "void", "putint", "i32 " + result.getName());
                } else {
                    irBlock.addInstrCall(null, "void", "putch", "i32 " + result.getName());
                }
                expCount++;
            } else {
                int strLen = parts.get(i).length();
                for (int j = 0; j < parts.get(i).length(); j++) {
                    if (parts.get(i).charAt(j) == '\\') {
                        strLen -= 2;
                    }
                }
                GlobalStr globalStr = new GlobalStr(parts.get(i), strLen);
                Module.addGlobalStr(globalStr);
                String rParams = "i8* getelementptr inbounds ([" + strLen + " x i8], [" + strLen + " x i8]* " + globalStr.getName() + ", i64 0, i64 0)";
                irBlock.addInstrCall(null, "void", "putstr", rParams);
            }
        }
    }

    private static void visitStmtIf(StmtIf stmtIf, SymbolTable symbolTable, Token.Type type, boolean isInFor) {
        /* Handle Cond */
        nextLabel = Register.allocReg();
        irBlock.addInstrBr("%" + nextLabel);
        irBlock.addInstrNull();
        int left = irBlock.getLoc() + 1;
        visitCond(stmtIf.getCond(), symbolTable);
        int right = irBlock.getLoc();
        irBlock.addInstrLabel(nextLabel);

        /* Handle Stmt1 */
        boolean stop = visitStmt(stmtIf.getStmt1(), symbolTable, type, isInFor);
        irBlock.replaceInterval(left, right, "%" + Register.getRegNum(), "<BLOCK2 OR STMT>");

        /* Handle Stmt2 */
        if (stmtIf.getStmt2() != null) {
            int replaceLoc = irBlock.getLoc() + 1;
            /* Branch to Next Stmt */
            nextLabel = Register.allocReg();
            if (!stop) {
                irBlock.addInstrBr("<NEXT STMT>");
            }
            irBlock.addInstrNull();
            irBlock.addInstrLabel(nextLabel);

            stop = visitStmt(stmtIf.getStmt2(), symbolTable, type, isInFor);
            irBlock.replaceInterval(replaceLoc, replaceLoc, "%" + Register.getRegNum(), "<NEXT STMT>");
        }

        /* Branch To Next Stmt */
        nextLabel = Register.allocReg();
        if (!stop) {
            irBlock.addInstrBr("%" + nextLabel);
        }

        irBlock.addInstrNull();
        irBlock.addInstrLabel(nextLabel);
    }

    private static void visitStmtFor(StmtFor stmtFor, SymbolTable symbolTable, Token.Type type) {
        /* Handle ForStmt1 */
        if (stmtFor.getForStmt1() != null) {
            visitForStmt(stmtFor.getForStmt1(), symbolTable);
        }

        /* Handle Cond */
        int condLabel = Register.getRegNum();
        int left = irBlock.getLoc() + 1;
        if (stmtFor.getCond() != null) {
            nextLabel = Register.allocReg();
            irBlock.addInstrBr("%" + nextLabel);
            irBlock.addInstrNull();
            visitCond(stmtFor.getCond(), symbolTable);
            irBlock.addInstrLabel(nextLabel);
        } else {
            /* Directly Branch to Stmt */
            nextLabel = Register.allocReg();
            irBlock.addInstrBr("%" + nextLabel);
            irBlock.addInstrNull();
            irBlock.addInstrLabel(nextLabel);
        }

        /* Handle Stmt */
        boolean stop = visitStmt(stmtFor.getStmt(), symbolTable, type, true);
        int right = irBlock.getLoc();

        /* If ForStmt2 Not Exist */
        if (stmtFor.getForStmt2() == null) {
            nextLabel = Register.allocReg();
            if (!stop) {
                irBlock.addInstrBr("%" + condLabel);
            }
            irBlock.addInstrNull();
            irBlock.addInstrLabel(nextLabel);
            irBlock.replaceInterval(left, right, "%" + nextLabel, "<BLOCK2 OR STMT>");
            irBlock.replaceInterval(left, right, "%" + condLabel, "<FORSTMT2>");
            return;
        }

        /* Branch to ForStmt2 */
        nextLabel = Register.allocReg();
        if (!stop) {
            irBlock.addInstrBr("%" + nextLabel);
        }
        irBlock.addInstrNull();
        irBlock.addInstrLabel(nextLabel);

        /* Handle ForStmt2 */
        int forstmt2Label = nextLabel;
        visitForStmt(stmtFor.getForStmt2(), symbolTable);

        /* Reach Loop Bottom */
        nextLabel = Register.allocReg();
        /* Branch to Cond */
        irBlock.addInstrBr("%" + condLabel);
        irBlock.addInstrNull();
        irBlock.addInstrLabel(nextLabel);

        irBlock.replaceInterval(left, right, "%" + nextLabel, "<BLOCK2 OR STMT>");
        irBlock.replaceInterval(left, right, "%" + forstmt2Label, "<FORSTMT2>");
    }

    private static void visitForStmt(ForStmt forStmt, SymbolTable symbolTable) {
        Value result = LocalDecl.visitExp(forStmt.getExp(), symbolTable);
        storeLVal(result, forStmt.getlVal(), symbolTable);
    }

    public static void visitCond(frontend.parser.expression.cond.Cond cond, SymbolTable symbolTable) {
        visitLOrExp(cond.getlOrExp(), symbolTable);
    }

    private static void visitSingleLOrExp(LAndExp lAndExp, SymbolTable symbolTable) {
        irBlock.addInstrLabel(Stmt.nextLabel);
        int left = irBlock.getLoc() + 1;
        Value result = visitLAndExp(lAndExp, symbolTable, true);
        if (result instanceof Constant) {
            if (((Constant) result).getValue() != 0) {
                /* Cond is true, Jump to Stmt1 */
                irBlock.delLastInstr();
                return;
            } else {
                /* Cond is false, Jump to BLOCK2 OR STMT */
                irBlock.addInstrBr("<BLOCK2 OR STMT>");
                irBlock.addInstrNull();
                Stmt.nextLabel = Register.allocReg();
            }
        } else if (result.isValue() || result.isCondValue()) {
            if (result.isValue()) {
                Constant zero = new Constant(0);
                result = irBlock.addInstrIcmp("%"+Register.allocReg(), "ne", result, zero);
            }
            Stmt.nextLabel = Register.allocReg();
            irBlock.addInstrBrCond(result, "%" + Stmt.nextLabel, "<BLOCK2 OR STMT>");
            irBlock.addInstrNull();
        }
        irBlock.replaceInterval(left, irBlock.getLoc(), "%" + Stmt.nextLabel, "<BLOCK1>");
    }

    private static void visitLOrExp(LOrExp lOrExp, SymbolTable symbolTable) {
        ArrayList<LAndExp> lAndExps = lOrExp.getLowerExps();
        if (lAndExps.size() == 1) {
            visitSingleLOrExp(lAndExps.get(0), symbolTable);
            return;
        }

        int left = irBlock.getLoc() + 1;
        for (int i = 0; i < lAndExps.size(); i++) {
            irBlock.addInstrLabel(Stmt.nextLabel);
            Value result = visitLAndExp(lAndExps.get(i), symbolTable, i == lAndExps.size() - 1);
            if (result instanceof Constant) {
                if (((Constant) result).getValue() != 0) {
                    /* Cond is true, end */
                    irBlock.addInstrBr("<BLOCK1>");
                    irBlock.addInstrNull();
                    Stmt.nextLabel = Register.allocReg();
                    break;
                } else {
                    /* Cond may be true, continue */
                    if (i == lAndExps.size() - 1) {
                        irBlock.addInstrBr("<BLOCK2 OR STMT>");
                        irBlock.addInstrNull();
                        Stmt.nextLabel = Register.allocReg();
                    } else {
                        irBlock.delLastInstr();
                    }
                }
            } else if (result.isValue() || result.isCondValue()) {
                /* Return Value In Register */
                if (result.isValue()) {
                    Constant zero = new Constant(0);
                    result = irBlock.addInstrIcmp("%"+Register.allocReg(), "ne", result, zero);
                }
                Stmt.nextLabel = Register.allocReg();
                if (i == lAndExps.size() - 1) {
                    irBlock.addInstrBrCond(result, "<BLOCK1>", "<BLOCK2 OR STMT>");
                } else {
                    irBlock.addInstrBrCond(result, "<BLOCK1>", "%" + Stmt.nextLabel);
                }
                irBlock.addInstrNull();
            }
        }
        irBlock.replaceInterval(left, irBlock.getLoc(), "%" + Stmt.nextLabel, "<BLOCK1>");
    }

    private static Value visitLAndExp(LAndExp lAndExp, SymbolTable symbolTable, boolean isLast) {
        ArrayList<EqExp> eqExps = lAndExp.getLowerExps();
        if (eqExps.size() == 1) {
            return visitEqExp(eqExps.get(0), symbolTable);
        }

        /* Delete Repeat Label */
        irBlock.delLastInstr();
        int left = irBlock.getLoc() + 1;
        for (int i = 0; i < eqExps.size(); i++) {
            irBlock.addInstrLabel(Stmt.nextLabel);
            Value result = visitEqExp(eqExps.get(i), symbolTable);
            if (result instanceof Constant) {
                if (((Constant) result).getValue() == 0) {
                    /* Cond is false, end */
                    irBlock.addInstrBr("<NEXT LOREXP>");
                    irBlock.addInstrNull();
                    Stmt.nextLabel = Register.allocReg();
                    break;
                } else {
                    /* Cond may be false, continue */
                    if (i == eqExps.size() - 1) {
                        irBlock.addInstrBr("<BLOCK1>");
                        irBlock.addInstrNull();
                        Stmt.nextLabel = Register.allocReg();
                    } else {
                        irBlock.delLastInstr();
                    }
                }
            } else {
                /* Return Value In Register */
                if (result.isValue()) {
                    Value temp = result;
                    Constant zero = new Constant(0);
                    result = irBlock.addInstrIcmp("%"+Register.allocReg(), "ne", temp, zero);
                }
                Stmt.nextLabel = Register.allocReg();
                if (i == eqExps.size() - 1) {
                    irBlock.addInstrBrCond(result, "<BLOCK1>", "<NEXT LOREXP>");
                } else {
                    irBlock.addInstrBrCond(result, "%" + Stmt.nextLabel, "<NEXT LOREXP>");
                }
                irBlock.addInstrNull();
            }
        }
        if (isLast) {
            irBlock.replaceInterval(left, irBlock.getLoc(), "<BLOCK2 OR STMT>", "<NEXT LOREXP>");
        } else {
            irBlock.replaceInterval(left, irBlock.getLoc(), "%" + Stmt.nextLabel, "<NEXT LOREXP>");
        }
        return new Value(Stmt.nextLabel, "");
    }

    private static Value visitEqExp(EqExp eqExp, SymbolTable symbolTable) {
        ArrayList<RelExp> relExps = eqExp.getLowerExps();
        if (relExps.size() == 1) {
            return visitRelExp(relExps.get(0), symbolTable);
        }

        ArrayList<Token> operators = eqExp.getOperators();
        Value result = visitRelExp(relExps.get(0), symbolTable);
        if (result.isCondValue()) {
            /* Value Transfer */
            result = irBlock.addInstrZext("%"+Register.allocReg(), "i1", result, "i32");
        }
        for (int i = 1; i < relExps.size(); i++) {
            String cond = Support.condTransfer(operators.get(i-1).getType());
            Value left = result;
            Value right = visitRelExp(relExps.get(i), symbolTable);
            if (right.isCondValue()) {
                /* Value Transfer */
                right = irBlock.addInstrZext("%"+Register.allocReg(), "i1", right, "i32");
            }
            result = irBlock.addInstrIcmp("%"+Register.allocReg(), cond, left, right);

            /* Value Transfer */
            result = irBlock.addInstrZext("%"+Register.allocReg(), "i1", result, "i32");
        }
        Register.cancelAlloc();
        irBlock.delLastInstr();
        return new Value(Register.getRegNum() - 1, "i1");
    }

    private static Value visitRelExp(RelExp relExp, SymbolTable symbolTable) {
        ArrayList<AddExp> addExps = relExp.getLowerExps();
        if (addExps.size() == 1) {
            return LocalDecl.visitAddExp(addExps.get(0), symbolTable);
        }

        ArrayList<Token> operators = relExp.getOperators();
        Value result = LocalDecl.visitAddExp(addExps.get(0), symbolTable);
        for (int i = 1; i < addExps.size(); i++) {
            String cond = Support.condTransfer(operators.get(i-1).getType());
            Value left = result;
            Value right = LocalDecl.visitAddExp(addExps.get(i), symbolTable);
            result =irBlock.addInstrIcmp("%"+Register.allocReg(), cond, left, right);

            /* Value Transfer */
            result = irBlock.addInstrZext("%"+Register.allocReg(), "i1", result, "i32");
        }
        Register.cancelAlloc();
        irBlock.delLastInstr();
        return new Value(Register.getRegNum() - 1, "i1");
    }

}
