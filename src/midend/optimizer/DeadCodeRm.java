package midend.optimizer;

import midend.llvm.IrModule;
import midend.llvm.function.Function;
import midend.llvm.function.IrBlock;
import midend.llvm.instr.IrCall;
import midend.llvm.instr.IrInstr;
import midend.llvm.instr.IrPutStr;

import java.util.ArrayList;
import java.util.Iterator;

public class DeadCodeRm {
    public static void removeDeadCode() {
        ArrayList<Function> functions = IrModule.getFunctions();
        for (Function func : functions) {
            IrBlock block = func.getIrBlock();
            Iterator<IrInstr> it = block.getInstructions().iterator();
            while (it.hasNext()) {
                IrInstr instr = it.next();
                if (!(instr instanceof IrCall|| instr instanceof IrPutStr) && instr.getUsers().isEmpty() && instr.hasLVal()) {
                    instr.removeOperands();
                    it.remove();
                }
//                if (instr instanceof IrCall) {
//                    Function target = (Function) instr.getOperands().get(0);
//                    if (instr.getUsers().isEmpty() && !target.isHasSideEffects()) {
//                        instr.removeOperands();
//                        it.remove();
//                    }
//                }
            }
        }
    }
}
