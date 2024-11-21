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
                Value value = result;
                result = new Value(Register.allocReg(), "i32");
                Constant zero = new Constant(0);
                irBlock.addInstrIcmp(result, "ne", value, zero);
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
                    Value temp = result;
                    result = new Value(Register.allocReg(), "i32");
                    Constant zero = new Constant(0);
                    irBlock.addInstrIcmp(result, "ne", temp, zero);
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
                    result = new Value(Register.allocReg(), "i32");
                    Constant zero = new Constant(0);
                    irBlock.addInstrIcmp(result, "ne", temp, zero);
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
            Value value = result;
            result = new Value(Register.allocReg(), "i32");
            irBlock.addInstrZext(result, "i1", value, "i32");
        }
        for (int i = 1; i < relExps.size(); i++) {
            String cond = Support.condTransfer(operators.get(i-1).getType());
            Value left = result;
            Value right = visitRelExp(relExps.get(i), symbolTable);
            if (right.isCondValue()) {
                /* Value Transfer */
                Value value = right;
                right = new Value(Register.allocReg(), "i32");
                irBlock.addInstrZext(right, "i1", value, "i32");
            }
            result = new Value(Register.allocReg(), "i32");
            irBlock.addInstrIcmp(result, cond, left, right);

            /* Value Transfer */
            Value value = result;
            result = new Value(Register.allocReg(), "i32");
            irBlock.addInstrZext(result, "i1", value, "i32");
        }
        Register.cancelAlloc();
        irBlock.delLastInstr();
        return new Value(Register.getRegNum() - 1, "i1");
    }

    private static Value visitRelExp(RelExp relExp, SymbolTable symbolTable) {
        ArrayList<AddExp> addExps = relExp.getLowerExps();
        if (addExps.size() == 1) {
            return VarValue.visitAddExp(addExps.get(0), symbolTable);
        }

        ArrayList<Token> operators = relExp.getOperators();
        Value result = VarValue.visitAddExp(addExps.get(0), symbolTable);
        for (int i = 1; i < addExps.size(); i++) {
            String cond = Support.condTransfer(operators.get(i-1).getType());
            Value left = result;
            Value right = VarValue.visitAddExp(addExps.get(i), symbolTable);
            result = new Value(Register.allocReg(), "i32");
            irBlock.addInstrIcmp(result, cond, left, right);

            /* Value Transfer */
            Value value = result;
            result = new Value(Register.allocReg(), "i32");
            irBlock.addInstrZext(result, "i1", value, "i32");
        }
        Register.cancelAlloc();
        irBlock.delLastInstr();
        return new Value(Register.getRegNum() - 1, "i1");
    }
}
