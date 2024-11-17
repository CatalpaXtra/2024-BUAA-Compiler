package midend.llvm.visit;

import frontend.lexer.Token;
import frontend.parser.block.Block;
import frontend.parser.block.BlockItem;
import frontend.parser.block.BlockItemEle;
import frontend.parser.block.statement.StmtEle;
import frontend.parser.block.statement.stmtVariant.*;
import frontend.parser.declaration.Decl;
import frontend.parser.expression.Exp;
import frontend.parser.expression.primary.LVal;
import midend.llvm.Module;
import midend.llvm.Register;
import midend.llvm.RetValue;
import midend.llvm.Support;
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
            VarValue.visitExp(((StmtExp) stmtEle).getExp(), symbolTable);
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
            RetValue result = VarValue.visitExp(stmtReturn.getExp(), symbolTable);
            if (funcType.contains("Char")) {
                RetValue value = result;
                result = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrTrunc(result, "i32", value, "i8");
            }
            irBlock.addInstrRet(Support.varTransfer(funcType), result);
        } else {
            irBlock.addInstrRet("void", null);
        }
    }

    private static void storeLVal(RetValue result, LVal lVal, SymbolTable symbolTable) {
        /* LVal Must Be Var */
        Symbol symbol = symbolTable.getSymbol(lVal.getIdent().getIdenfr());
        String memory = symbol.getMemory();
        String irType = Support.varTransfer(symbol.getSymbolType());
        if (lVal.isArray()) {
            RetValue loc = VarValue.visitExp(lVal.getExp(), symbolTable);
            RetValue temp1 = new RetValue(Register.allocReg(), 1);
            if (symbol.isPointer()) {
                irBlock.addInstrLoad(temp1, irType + "*", memory);
                RetValue temp2 = temp1;
                temp1 = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrGetelementptr(temp1, -1, irType, temp2.irOut(), loc.irOut());
            } else {
                irBlock.addInstrGetelementptr(temp1, symbol.getArraySize(), irType, memory, loc.irOut());
            }
            if (symbol.isChar()) {
                RetValue value = result;
                result = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrTrunc(result, "i32", value, "i8");
            }
            irBlock.addInstrStore(irType, result.irOut(), temp1.irOut());
        } else {
            if (symbol.isChar()) {
                RetValue value = result;
                result = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrTrunc(result, "i32", value, "i8");
            }
            irBlock.addInstrStore(irType, result.irOut(), memory);
        }
    }

    private static void visitStmtAssign(StmtAssign stmtAssign, SymbolTable symbolTable) {
        RetValue result = VarValue.visitExp(stmtAssign.getExp(), symbolTable);
        storeLVal(result, stmtAssign.getlVal(), symbolTable);
    }

    private static void visitStmtGetInt(StmtGetInt stmtGetInt, SymbolTable symbolTable) {
        RetValue result = new RetValue(Register.allocReg(), 1);
        irBlock.addInstrCall(result, "i32", "getint", "");
        storeLVal(result, stmtGetInt.getlVal(), symbolTable);
    }

    private static void visitStmtGetChar(StmtGetChar stmtGetChar, SymbolTable symbolTable) {
        RetValue result = new RetValue(Register.allocReg(), 1);
        irBlock.addInstrCall(result, "i32", "getchar", "");
        storeLVal(result, stmtGetChar.getlVal(), symbolTable);
    }

    private static void visitStmtPrint(StmtPrint stmtPrint, SymbolTable symbolTable) {
        String string = stmtPrint.getStringConst().getToken().getContent();
        ArrayList<String> parts = Support.splitPrintString(string);
        ArrayList<Exp> exps = stmtPrint.getExps();
        int expCount = 0;
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).equals("%d") || parts.get(i).equals("%c")) {
                RetValue result = VarValue.visitExp(exps.get(expCount), symbolTable);
                if (parts.get(i).equals("%d")) {
                    irBlock.addInstrCall(null, "void", "putint", "i32 " + result.irOut());
                } else {
                    irBlock.addInstrCall(null, "void", "putch", "i32 " + result.irOut());
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
        Cond.visitCond(stmtIf.getCond(), symbolTable);
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
            Cond.visitCond(stmtFor.getCond(), symbolTable);
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
        /* Branch to ForStmt2 */
        nextLabel = Register.allocReg();
        if (!stop) {
            irBlock.addInstrBr("%" + nextLabel);
        }
        irBlock.addInstrNull();
        irBlock.addInstrLabel(nextLabel);

        /* Handle ForStmt2 */
        int forstmt2Label = nextLabel;
        if (stmtFor.getForStmt2() != null) {
            visitForStmt(stmtFor.getForStmt2(), symbolTable);
        }

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
        RetValue result = VarValue.visitExp(forStmt.getExp(), symbolTable);
        storeLVal(result, forStmt.getlVal(), symbolTable);
    }

}
