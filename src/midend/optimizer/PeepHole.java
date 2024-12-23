package midend.optimizer;

import backend.Module;
import backend.instr.*;

import java.util.ArrayList;
import java.util.Iterator;

public class PeepHole {
//    private static final int cycle = 1; // save
    private static final int cycle = 1;

    public static void optimize() {
        if (Optimizer.optimize) {
            for (int i = 0 ; i < cycle; i++) {
                redundantInstr();
            }
            jumpToNextStmt();
        }
    }

    private static void redundantInstr() {
        ArrayList<AsmInstr> text = Module.getText();
        Iterator<AsmInstr> iterator = text.iterator();
        while (iterator.hasNext()) {
            AsmInstr instr = iterator.next();
            if (instr instanceof AsmMove) {
                AsmInstr next = Module.getNextAsm(instr);
                if (next instanceof AsmMove) {
                    if (((AsmMove) instr).getTo().equals(((AsmMove) next).getFrom())) {
                        ((AsmMove) next).modifyFrom(((AsmMove) instr).getFrom());
                        iterator.remove();
                    }
                } else if (next instanceof AsmMem) {
                    // WARN might bring risk
                    if (((AsmMove) instr).getTo().equals(((AsmMem) next).getValue())) {
                        ((AsmMem) next).modifyValue(((AsmMove) instr).getFrom());
                        iterator.remove();
                    }
                } else if (next instanceof AsmAlu) {
                    if (((AsmMove) instr).getTo().equals(((AsmAlu) next).getOperand1())) {
                        ((AsmAlu) next).modifyOperand1(((AsmMove) instr).getFrom());
                        iterator.remove();
                    } else if (((AsmMove) instr).getTo().equals(((AsmAlu) next).getOperand2())) {
                        ((AsmAlu) next).modifyOperand2(((AsmMove) instr).getFrom());
                        iterator.remove();
                    }
                }
            }
            if (instr instanceof AsmMem) {
                AsmInstr next = Module.getNextAsm(instr);
                if (next instanceof AsmMem && ((AsmMem) instr).getType() == AsmMem.Type.sw && ((AsmMem) next).getType() == AsmMem.Type.lw) {
                    if (((AsmMem) instr).getOffset() == ((AsmMem) next).getOffset() && ((AsmMem) instr).getBase() == ((AsmMem) next).getBase()) {
                        if (((AsmMem) instr).getValue().equals(((AsmMem) next).getValue())) {
                            instr = iterator.next();
                            while (!instr.equals(next)) {
                                instr = iterator.next();
                            }
                            iterator.remove();
                        } else {
                            AsmMem instrBefore = (AsmMem) instr;
                            instr = iterator.next();
                            while (!instr.equals(next)) {
                                instr = iterator.next();
                            }
                            Module.changeInstr(next, ((AsmMem) next).getValue(), instrBefore.getValue());
                        }
                    }
                } else if (next instanceof AsmMove && ((AsmMem) instr).getType() == AsmMem.Type.lw) {
                    if (((AsmMove) next).getFrom().equals(((AsmMem) instr).getValue())) {
                        ((AsmMem) instr).modifyValue(((AsmMove) next).getTo());
                        instr = iterator.next();
                        while (!instr.equals(next)) {
                            instr = iterator.next();
                        }
                        iterator.remove();
                    }
                }
            }
        }
    }

    private static void jumpToNextStmt() {
        ArrayList<AsmInstr> text = Module.getText();
        Iterator<AsmInstr> iterator = text.iterator();
        while (iterator.hasNext()) {
            AsmInstr instr = iterator.next();
            if (instr instanceof AsmJump && ((AsmJump) instr).getOp() == AsmJump.OP.j) {
                if (Module.nearLabel((AsmJump) instr)) {
                    iterator.remove();
                }
            }
        }
    }
}
