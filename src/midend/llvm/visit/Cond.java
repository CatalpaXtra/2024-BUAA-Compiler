package midend.llvm.visit;

import frontend.lexer.Token;
import frontend.parser.expression.add.AddExp;
import frontend.parser.expression.cond.EqExp;
import frontend.parser.expression.cond.LAndExp;
import frontend.parser.expression.cond.LOrExp;
import frontend.parser.expression.cond.RelExp;
import midend.llvm.*;
import midend.llvm.function.IrBlock;
import midend.llvm.symbol.SymbolTable;

import java.util.ArrayList;

public class Cond {
    private static IrBlock irBlock;

    public static void setCondIrBlock(IrBlock irBlock) {
        Cond.irBlock = irBlock;
    }
    
    public static void visitCond(frontend.parser.expression.cond.Cond cond, SymbolTable symbolTable) {
        visitLOrExp(cond.getlOrExp(), symbolTable);
    }

    private static void visitSingleLOrExp(LAndExp lAndExp, SymbolTable symbolTable) {
        irBlock.addCode(Stmt.nextLabel + ":");
        int left = irBlock.getLoc() + 1;
        RetValue result = visitLAndExp(lAndExp, symbolTable, true);
        if (result.isDigit()) {
            if (result.getValue() != 0) {
                /* Cond is true, Jump to Stmt1 */
                irBlock.delLastCode();
                return;
            } else {
                /* Cond is false, Jump to BLOCK2 OR STMT */
                irBlock.addInstrBr("<BLOCK2 OR STMT>");
                irBlock.addCode("");
                Stmt.nextLabel = Register.allocReg();
            }
        } else if (result.isReg() || result.isMany()) {
            if (result.isReg()) {
                RetValue value = result;
                result = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrIcmp(result, "ne", value, "0");
            }
            Stmt.nextLabel = Register.allocReg();
            irBlock.addInstrBrCond(result, "%" + Stmt.nextLabel, "<BLOCK2 OR STMT>");
            irBlock.addCode("");
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
            irBlock.addCode(Stmt.nextLabel + ":");
            RetValue result = visitLAndExp(lAndExps.get(i), symbolTable, i == lAndExps.size() - 1);
            if (result.isDigit()) {
                if (result.getValue() != 0) {
                    /* Cond is true, end */
                    Stmt.nextLabel = Register.allocReg();
                    irBlock.addInstrBr("<BLOCK1>");
                    irBlock.addCode("");
                    irBlock.replaceInterval(left, irBlock.getLoc(), "%" + Stmt.nextLabel, "<BLOCK1>");
                    return;
                } else {
                    /* Cond may be true, continue */
                    if (i == lAndExps.size() - 1) {
                        Stmt.nextLabel = Register.allocReg();
                        irBlock.addInstrBr("<BLOCK2 OR STMT>");
                        irBlock.addCode("");
                        irBlock.replaceInterval(left, irBlock.getLoc(), "%" + Stmt.nextLabel, "<BLOCK1>");
                        return;
                    }
                    irBlock.delLastCode();
                }
            } else if (result.isReg() || result.isMany()) {
                /* Return Value In Register */
                if (result.isReg()) {
                    RetValue temp = result;
                    result = new RetValue(Register.allocReg(), 1);
                    irBlock.addInstrIcmp(result, "ne", temp, "0");
                }
                Stmt.nextLabel = Register.allocReg();
                if (i == lAndExps.size() - 1) {
                    irBlock.addInstrBrCond(result, "<BLOCK1>", "<BLOCK2 OR STMT>");
                } else {
                    irBlock.addInstrBrCond(result, "<BLOCK1>", "%" + Stmt.nextLabel);
                }
                irBlock.addCode("");
            }
        }
        irBlock.replaceInterval(left, irBlock.getLoc(), "%" + Stmt.nextLabel, "<BLOCK1>");
    }

    private static RetValue visitLAndExp(LAndExp lAndExp, SymbolTable symbolTable, boolean isLast) {
        ArrayList<EqExp> eqExps = lAndExp.getLowerExps();
        if (eqExps.size() == 1) {
            return visitEqExp(eqExps.get(0), symbolTable);
        }

        /* Delete Repeat Label */
        irBlock.delLastCode();
        int left = irBlock.getLoc() + 1;
        for (int i = 0; i < eqExps.size(); i++) {
            irBlock.addCode(Stmt.nextLabel + ":");
            RetValue result = visitEqExp(eqExps.get(i), symbolTable);
            if (result.isDigit()) {
                if (result.getValue() == 0) {
                    /* Cond is false, end */
                    irBlock.addInstrBr("<NEXT LOREXP>");
                    irBlock.addCode("");

                    Stmt.nextLabel = Register.allocReg();
                    if (isLast) {
                        irBlock.replaceInterval(left, irBlock.getLoc(), "<BLOCK2 OR STMT>", "<NEXT LOREXP>");
                    } else {
                        irBlock.replaceInterval(left, irBlock.getLoc(), "%" + Stmt.nextLabel, "<NEXT LOREXP>");
                    }
                    return new RetValue(Stmt.nextLabel, 2);
                } else {
                    /* Cond may be false, continue */
                    if (i == eqExps.size() - 1) {
                        irBlock.addInstrBr("<BLOCK1>");
                        irBlock.addCode("");
                        Stmt.nextLabel = Register.allocReg();
                        if (isLast) {
                            irBlock.replaceInterval(left, irBlock.getLoc(), "<BLOCK2 OR STMT>", "<NEXT LOREXP>");
                        } else {
                            irBlock.replaceInterval(left, irBlock.getLoc(), "%" + Stmt.nextLabel, "<NEXT LOREXP>");
                        }
                        return new RetValue(Stmt.nextLabel, 2);
                    } else {
                        irBlock.delLastCode();
                    }
                }
            } else if (result.isReg()) {
                /* Return Value In Register */
                RetValue temp = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrIcmp(temp, "ne", result, "0");

                Stmt.nextLabel = Register.allocReg();
                if (i == eqExps.size() - 1) {
                    irBlock.addInstrBrCond(temp, "<BLOCK1>", "<NEXT LOREXP>");
                } else {
                    irBlock.addInstrBrCond(temp, "%" + Stmt.nextLabel, "<NEXT LOREXP>");
                }
                irBlock.addCode("");
            } else {
                Stmt.nextLabel = Register.allocReg();
                if (i == eqExps.size() - 1) {
                    irBlock.addInstrBrCond(result, "<BLOCK1>", "<NEXT LOREXP>");
                } else {
                    irBlock.addInstrBrCond(result, "%" + Stmt.nextLabel, "<NEXT LOREXP>");
                }
                irBlock.addCode("");
            }
        }
        if (isLast) {
            irBlock.replaceInterval(left, irBlock.getLoc(), "<BLOCK2 OR STMT>", "<NEXT LOREXP>");
        } else {
            irBlock.replaceInterval(left, irBlock.getLoc(), "%" + Stmt.nextLabel, "<NEXT LOREXP>");
        }
        return new RetValue(Stmt.nextLabel, 2);
    }

    private static RetValue visitEqExp(EqExp eqExp, SymbolTable symbolTable) {
        ArrayList<RelExp> relExps = eqExp.getLowerExps();
        if (relExps.size() == 1) {
            return visitRelExp(relExps.get(0), symbolTable);
        }

        ArrayList<Token> operators = eqExp.getOperators();
        RetValue result = visitRelExp(relExps.get(0), symbolTable);
        if (result.isMany()) {
            /* Value Transfer */
            RetValue value = result;
            result = new RetValue(Register.allocReg(), 1);
            irBlock.addInstrZext(result, "i1", value, "i32");
        }
        for (int i = 1; i < relExps.size(); i++) {
            String cond = Support.condTransfer(operators.get(i-1).getType());
            RetValue left = result;
            RetValue right = visitRelExp(relExps.get(i), symbolTable);
            if (right.isMany()) {
                /* Value Transfer */
                RetValue value = right;
                right = new RetValue(Register.allocReg(), 1);
                irBlock.addInstrZext(right, "i1", value, "i32");
            }
            result = new RetValue(Register.allocReg(), 1);
            irBlock.addInstrIcmp(result, cond, left, right.irOut());

            /* Value Transfer */
            RetValue value = result;
            result = new RetValue(Register.allocReg(), 1);
            irBlock.addInstrZext(result, "i1", value, "i32");
        }
        Register.cancelAlloc();
        irBlock.delLastCode();
        return new RetValue(Register.getRegNum() - 1, 3);
    }

    private static RetValue visitRelExp(RelExp relExp, SymbolTable symbolTable) {
        ArrayList<AddExp> addExps = relExp.getLowerExps();
        if (addExps.size() == 1) {
            return VarValue.visitAddExp(addExps.get(0), symbolTable);
        }

        ArrayList<Token> operators = relExp.getOperators();
        RetValue result = VarValue.visitAddExp(addExps.get(0), symbolTable);
        for (int i = 1; i < addExps.size(); i++) {
            String cond = Support.condTransfer(operators.get(i-1).getType());
            RetValue left = result;
            RetValue right = VarValue.visitAddExp(addExps.get(i), symbolTable);
            result = new RetValue(Register.allocReg(), 1);
            irBlock.addInstrIcmp(result, cond, left, right.irOut());

            /* Value Transfer */
            RetValue value = result;
            result = new RetValue(Register.allocReg(), 1);
            irBlock.addInstrZext(result, "i1", value, "i32");
        }
        Register.cancelAlloc();
        irBlock.delLastCode();
        return new RetValue(Register.getRegNum() - 1, 3);
    }
}
