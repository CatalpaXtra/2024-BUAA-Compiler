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
import midend.symbol.Symbol;
import midend.symbol.SymbolTable;

import java.util.ArrayList;

public class LocalStmt {
    private static Module module;
    public static int nextLabel;
    private static String funcType;

    public static void setLocalStmt(Module md) {
        module = md;
    }

    public static void setFuncType(String type) {
        funcType = type;
    }

    public static boolean visitBlock(Block block, SymbolTable symbolTable, Token.Type type, boolean isInFor) {
        ArrayList<BlockItem> blockItems = block.getBlockItems();
        for (BlockItem blockItem : blockItems) {
            BlockItemEle blockItemEle = blockItem.getBlockItemEle();
            if (blockItemEle instanceof Decl) {
                LocalDecl.visitDecl((Decl) blockItemEle, symbolTable);
            } else {
                visitStmt((Stmt) blockItemEle, symbolTable, type, isInFor);
                StmtEle stmtEle = ((Stmt) blockItemEle).getStmtEle();
                if (stmtEle instanceof StmtReturn || stmtEle instanceof StmtBreak || stmtEle instanceof StmtContinue) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean visitStmt(Stmt stmt, SymbolTable symbolTable, Token.Type type, boolean isInFor) {
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
            module.addInstrBr("<BLOCK2 OR STMT>");
            return true;
        } else if (stmtEle instanceof StmtContinue) {
            module.addInstrBr("<FORSTMT2>");
            return true;
        }
        return false;
    }

    private static void visitStmtReturn(StmtReturn stmtReturn, SymbolTable symbolTable) {
        if (stmtReturn.existReturnValue()) {
            RetValue result = LocalDecl.visitExp(stmtReturn.getExp(), symbolTable);
            if (funcType.contains("Char")) {
                RetValue value = result;
                result = new RetValue(Register.allocReg(), 1);
                module.addInstrTrunc(result, "i32", value, "i8");
            }
            module.addInstrRet(Support.varTransfer(funcType), result);
        } else {
            module.addInstrRet("void", null);
        }
    }

    private static void storeLVal(RetValue result, LVal lVal, SymbolTable symbolTable) {
        /* LVal Must Be Var */
        Symbol symbol = symbolTable.getSymbol(lVal.getIdent().getIdenfr());
        String memory = symbol.getMemory();
        String llvmType = Support.varTransfer(symbol.getSymbolType());
        if (lVal.isArray()) {
            RetValue loc = LocalDecl.visitExp(lVal.getExp(), symbolTable);
            RetValue temp1 = new RetValue(Register.allocReg(), 1);
            if (symbol.isPointer()) {
                module.addInstrLoad(temp1, llvmType + "*", memory);
                RetValue temp2 = temp1;
                temp1 = new RetValue(Register.allocReg(), 1);
                module.addInstrGetelementptrPointer(temp1, llvmType, temp2.irOut(), loc.irOut());
            } else {
                module.addInstrGetelementptrArray(temp1, symbol.getArraySize(), llvmType, memory, loc.irOut());
            }
            if (symbol.isChar()) {
                RetValue value = result;
                result = new RetValue(Register.allocReg(), 1);
                module.addInstrTrunc(result, "i32", value, "i8");
            }
            module.addInstrStore(llvmType, result.irOut(), temp1.irOut());
        } else {
            if (symbol.isChar()) {
                RetValue value = result;
                result = new RetValue(Register.allocReg(), 1);
                module.addInstrTrunc(result, "i32", value, "i8");
            }
            module.addInstrStore(llvmType, result.irOut(), memory);
        }
    }

    private static void visitStmtAssign(StmtAssign stmtAssign, SymbolTable symbolTable) {
        RetValue result = LocalDecl.visitExp(stmtAssign.getExp(), symbolTable);
        storeLVal(result, stmtAssign.getlVal(), symbolTable);
    }

    private static void visitStmtGetInt(StmtGetInt stmtGetInt, SymbolTable symbolTable) {
        RetValue result = new RetValue(Register.allocReg(), 1);
        module.addInstrCall(result, "i32", "getint", "");
        storeLVal(result, stmtGetInt.getlVal(), symbolTable);
    }

    private static void visitStmtGetChar(StmtGetChar stmtGetChar, SymbolTable symbolTable) {
        RetValue result = new RetValue(Register.allocReg(), 1);
        module.addInstrCall(result, "i32", "getchar", "");
        storeLVal(result, stmtGetChar.getlVal(), symbolTable);
    }

    private static void visitStmtPrint(StmtPrint stmtPrint, SymbolTable symbolTable) {
        String string = stmtPrint.getStringConst().getToken().getContent();
        ArrayList<String> parts = Support.splitPrintString(string);
        ArrayList<Exp> exps = stmtPrint.getExps();
        int expCount = 0;
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).equals("%d") || parts.get(i).equals("%c")) {
                RetValue result = LocalDecl.visitExp(exps.get(expCount), symbolTable);
                if (parts.get(i).equals("%d")) {
                    module.addInstrCall(null, "void", "putint", "i32 " + result.irOut());
                } else {
                    RetValue value = result;
                    result = new RetValue(Register.allocReg(), 1);
                    module.addInstrTrunc(result, "i32", value, "i8");
                    module.addInstrCall(null, "void", "putch", "i8 " + result.irOut());
                }
                expCount++;
            } else {
                int strLen = parts.get(i).length();
                for (int j = 0; j < parts.get(i).length(); j++) {
                    if (parts.get(i).charAt(j) == '\\') {
                        strLen -= 2;
                    }
                }
                String strName = module.addGlobalStr(strLen, parts.get(i));
                String rParams = "i8* getelementptr inbounds ([" + strLen + " x i8], [" + strLen + " x i8]* " + strName + ", i64 0, i64 0)";
                module.addInstrCall(null, "void", "putstr", rParams);
            }
        }
    }

    private static void visitStmtIf(StmtIf stmtIf, SymbolTable symbolTable, Token.Type type, boolean isInFor) {
        /* Handle Cond */
        nextLabel = Register.allocReg();
        module.addInstrBr("%" + nextLabel);
        module.addCode("");
        int left = module.getLoc() + 1;
        visitCond(stmtIf.getCond(), symbolTable);
        int right = module.getLoc();
        module.addCode(nextLabel + ":");

        /* Handle Stmt1 */
        boolean stop = visitStmt(stmtIf.getStmt1(), symbolTable, type, isInFor);
        module.replaceInterval(left, right, "%" + Register.getRegNum(), "<BLOCK2 OR STMT>");

        /* Handle Stmt2 */
        if (stmtIf.getStmt2() != null) {
            int replaceLoc = module.getLoc() + 1;
            /* Branch to Next Stmt */
            nextLabel = Register.allocReg();
            if (!stop) {
                module.addInstrBr("<NEXT STMT>");
            }
            module.addCode("");
            module.addCode(nextLabel + ":");

            stop = visitStmt(stmtIf.getStmt2(), symbolTable, type, isInFor);
            module.replaceInterval(replaceLoc, replaceLoc, "%" + Register.getRegNum(), "<NEXT STMT>");
        }

        /* Branch To Next Stmt */
        nextLabel = Register.allocReg();
        if (!stop) {
            module.addInstrBr("%" + nextLabel);
        }

        module.addCode("");
        module.addCode(nextLabel + ":");
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
            nextLabel = Register.allocReg();
            module.addInstrBr("%" + nextLabel);
            module.addCode("");
            visitCond(stmtFor.getCond(), symbolTable);
            module.addCode(nextLabel + ":");
        } else {
            /* Directly Branch to Stmt */
            nextLabel = Register.allocReg();
            module.addInstrBr("%" + nextLabel);
            module.addCode("");
            module.addCode(nextLabel + ":");
        }

        /* Handle Stmt */
        boolean stop = visitStmt(stmtFor.getStmt(), symbolTable, type, true);
        int right = module.getLoc();
        /* Branch to ForStmt2 */
        nextLabel = Register.allocReg();
        if (!stop) {
            module.addInstrBr("%" + nextLabel);
        }
        module.addCode("");
        module.addCode(nextLabel + ":");

        /* Handle ForStmt2 */
        int forstmt2Label = nextLabel;
        if (stmtFor.getForStmt2() != null) {
            visitForStmt(stmtFor.getForStmt2(), symbolTable);
        }

        /* Reach Loop Bottom */
        nextLabel = Register.allocReg();
        /* Branch to Cond */
        module.addInstrBr("%" + condLabel);
        module.addCode("");
        module.addCode(nextLabel + ":");

        module.replaceInterval(left, right, "%" + nextLabel, "<BLOCK2 OR STMT>");
        module.replaceInterval(left, right, "%" + forstmt2Label, "<FORSTMT2>");
    }

    private static void visitForStmt(ForStmt forStmt, SymbolTable symbolTable) {
        RetValue result = LocalDecl.visitExp(forStmt.getExp(), symbolTable);
        storeLVal(result, forStmt.getlVal(), symbolTable);
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
                module.addInstrBr("<BLOCK2 OR STMT>");
                module.addCode("");
                nextLabel = Register.allocReg();
            }
        } else if (result.isReg() || result.isMany()) {
            if (result.isReg()) {
                RetValue value = result;
                result = new RetValue(Register.allocReg(), 1);
                module.addInstrIcmp(result, "ne", value, "0");
            }
            nextLabel = Register.allocReg();
            module.addInstrBrCond(result, "%" + nextLabel, "<BLOCK2 OR STMT>");
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
                    module.addInstrBr("<BLOCK1>");
                    module.addCode("");
                    module.replaceInterval(left, module.getLoc(), "%" + nextLabel, "<BLOCK1>");
                    return;
                } else {
                    /* Cond may be true, continue */
                    module.delLastCode();
                }
            } else if (result.isReg() || result.isMany()) {
                /* Return Value In Register */
                if (result.isReg()) {
                    RetValue temp = result;
                    result = new RetValue(Register.allocReg(), 1);
                    module.addInstrIcmp(result, "ne", temp, "0");
                }
                nextLabel = Register.allocReg();
                if (i == lAndExps.size() - 1) {
                    module.addInstrBrCond(result, "<BLOCK1>", "<BLOCK2 OR STMT>");
                } else {
                    module.addInstrBrCond(result, "<BLOCK1>", "%" + nextLabel);
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

        /* Delete Repeat Label */
        module.delLastCode();
        int left = module.getLoc() + 1;
        for (int i = 0; i < eqExps.size(); i++) {
            module.addCode(nextLabel + ":");
            RetValue result = visitEqExp(eqExps.get(i), symbolTable);
            if (result.isDigit()) {
                if (result.getValue() == 0) {
                    /* Cond is false, end */
                    module.addInstrBr("<NEXT LOREXP>");
                    module.addCode("");

                    nextLabel = Register.allocReg();
                    if (isLast) {
                        module.replaceInterval(left, module.getLoc(), "<BLOCK2 OR STMT>", "<NEXT LOREXP>");
                    } else {
                        module.replaceInterval(left, module.getLoc(), "%" + nextLabel, "<NEXT LOREXP>");
                    }
                    return new RetValue(nextLabel, 2);
                } else {
                    /* Cond may be false, continue */
                    module.delLastCode();
                }
            } else if (result.isReg()) {
                /* Return Value In Register */
                RetValue temp = new RetValue(Register.allocReg(), 1);
                module.addInstrIcmp(temp, "ne", result, "0");

                nextLabel = Register.allocReg();
                if (i == eqExps.size() - 1) {
                    module.addInstrBrCond(temp, "<BLOCK1>", "<NEXT LOREXP>");
                } else {
                    module.addInstrBrCond(temp, "%" + nextLabel, "<NEXT LOREXP>");
                }
                module.addCode("");
            } else {
                nextLabel = Register.allocReg();
                if (i == eqExps.size() - 1) {
                    module.addInstrBrCond(result, "<BLOCK1>", "<NEXT LOREXP>");
                } else {
                    module.addInstrBrCond(result, "%" + nextLabel, "<NEXT LOREXP>");
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
            if (result.isMany()) {
                /* Value Transfer */
                RetValue value = result;
                result = new RetValue(Register.allocReg(), 1);
                module.addInstrZext(result, "i1", value, "i32");
            }

            String cond = Support.condTransfer(operators.get(i-1).getType());
            RetValue left = result;
            RetValue right = visitRelExp(relExps.get(i), symbolTable);
            result = new RetValue(Register.allocReg(), 1);
            module.addInstrIcmp(result, cond, left, right.irOut());

            /* Value Transfer */
            RetValue value = result;
            result = new RetValue(Register.allocReg(), 1);
            module.addInstrZext(result, "i1", value, "i32");
        }
        Register.cancelAlloc();
        module.delLastCode();
        return new RetValue(Register.getRegNum() - 1, 3);
    }

    private static RetValue visitRelExp(RelExp relExp, SymbolTable symbolTable) {
        ArrayList<AddExp> addExps = relExp.getLowerExps();
        if (addExps.size() == 1) {
            return LocalDecl.visitAddExp(addExps.get(0), symbolTable);
        }

        ArrayList<Token> operators = relExp.getOperators();
        RetValue result = LocalDecl.visitAddExp(addExps.get(0), symbolTable);
        for (int i = 1; i < addExps.size(); i++) {
            String cond = Support.condTransfer(operators.get(i-1).getType());
            RetValue left = result;
            RetValue right = LocalDecl.visitAddExp(addExps.get(i), symbolTable);
            result = new RetValue(Register.allocReg(), 1);
            module.addInstrIcmp(result, cond, left, right.irOut());

            /* Value Transfer */
            RetValue value = result;
            result = new RetValue(Register.allocReg(), 1);
            module.addInstrZext(result, "i1", value, "i32");
        }
        Register.cancelAlloc();
        module.delLastCode();
        return new RetValue(Register.getRegNum() - 1, 3);
    }
}
