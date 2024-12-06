package midend.optimizer;

import backend.Module;
import backend.instr.AsmInstr;
import backend.instr.AsmJump;

import java.util.ArrayList;
import java.util.Iterator;

public class PeepHole {
    public static void optimize() {
        removeRedundantMove();
        removeRedundantJ();
    }

    private static void removeRedundantMove() {
//        ArrayList<AsmInstr> text = Module.getText();
//        Iterator<AsmInstr> iterator = text.iterator();
//        while (iterator.hasNext()) {
//            AsmInstr instr = iterator.next();
//            if (instr instanceof AsmJump && ((AsmJump) instr).getOp() == AsmJump.OP.j) {
//                if (Module.nearLabel((AsmJump) instr)) {
//                    iterator.remove();
//                }
//            }
//        }
    }

    private static void removeRedundantJ() {
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
