package midend.optimizer;

import midend.llvm.IrModule;
import midend.llvm.User;
import midend.llvm.Value;
import midend.llvm.function.Function;
import midend.llvm.function.IrBlock;
import midend.llvm.instr.*;

import java.util.ArrayList;
import java.util.Iterator;

public class DeadCodeRm {
    private static final int rmCycle = 10;

    public static void removeDeadCode() {
        ArrayList<Function> functions = IrModule.getFunctions();
        for (Function func : functions) {
            for (int i = 0; i < rmCycle; i++) {
                removeOnce(func.getIrBlock());
            }
        }
    }

    private static void removeOnce(IrBlock block) {
        Iterator<IrInstr> it = block.getInstructions().iterator();
        ArrayList<Value> delInstrs = new ArrayList<>();
        while (it.hasNext()) {
            IrInstr instr = it.next();
            ArrayList<User> users = instr.getUsers();
            if (!(instr instanceof IrCall|| instr instanceof IrPutStr) && users.isEmpty() && instr.hasLVal()) {
                instr.removeOperands();
                it.remove();
            } else if (instr instanceof IrAlloca) {
                boolean flag = true;
                for (User user : users) {
                    if (!(user instanceof IrStore)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    delInstrs.addAll(users);
                    instr.removeOperands();
                    it.remove();
                }
            } else if (instr instanceof IrCall) {
                String funcName = ((IrCall)instr).getFuncName();
                if (!(funcName.equals("getint") || funcName.equals("getchar") || funcName.equals("putint") || funcName.equals("putch"))) {
                    Function target = ((IrCall) instr).getFunction();
                    if (instr.getUsers().isEmpty() && !target.hasSideEffects()) {
                        instr.removeOperands();
                        it.remove();
                    }
                }
            }
        }
        it = block.getInstructions().iterator();
        while (it.hasNext()) {
            IrInstr instr = it.next();
            if (delInstrs.contains(instr)) {
                instr.removeOperands();
                it.remove();
            }
        }
    }

}
