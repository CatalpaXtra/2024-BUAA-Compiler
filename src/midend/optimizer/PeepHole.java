package midend.optimizer;

import backend.Module;
import backend.instr.AsmInstr;
import backend.instr.AsmJump;
import backend.instr.AsmMem;
import backend.instr.AsmMove;

import java.util.ArrayList;
import java.util.Iterator;

public class PeepHole {
    public static void optimize() {
        //TODO
        redundantMove();
        redundantMem();
        jumpToNextStmt();
    }

    private static void redundantMove() {
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
                    if (((AsmMove) instr).getTo().equals(((AsmMem) next).getValue())) {
                        ((AsmMem) next).modifyValue(((AsmMove) instr).getFrom());
                        iterator.remove();
                    }
                }
            }
        }
    }

    private static void redundantMem() {
        ArrayList<AsmInstr> text = Module.getText();
        Iterator<AsmInstr> iterator = text.iterator();
        while (iterator.hasNext()) {
            AsmInstr instr = iterator.next();
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
                        }
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
