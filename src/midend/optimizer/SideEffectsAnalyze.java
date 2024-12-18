package midend.optimizer;

import midend.llvm.IrModule;
import midend.llvm.Value;
import midend.llvm.function.Function;
import midend.llvm.function.IrBlock;
import midend.llvm.instr.*;

import java.util.ArrayList;
import java.util.HashSet;

public class SideEffectsAnalyze {
    public static void analyzeSideEffects(){
        for (Function func : IrModule.getFunctions()) {
            boolean hasSideEffects = false;
            HashSet<Function> call = new HashSet<>();
            IrBlock block = func.getIrBlock();
            ArrayList<IrInstr> instrs = block.getInstructions();
            for (IrInstr instr : instrs) {
                if (instr instanceof IrCall) {
                    String funcName = ((IrCall)instr).getFuncName();
                    if (funcName.equals("getint") || funcName.equals("getchar") || funcName.equals("putint") || funcName.equals("putch")) {
                        hasSideEffects = true;
                        break;
                    }
                    Function target= ((IrCall) instr).getFunction();
                    call.add(target);
                } else if (instr instanceof IrPutStr) {
                    hasSideEffects = true;
                    break;
                } else if (instr instanceof IrStore) {
                    Value to = instr.getOperands().get(1);
                    if (to.getName().charAt(0) == '@') {
                        // Means GlobalVal
                        hasSideEffects = true;
                        break;
                    } else if (to instanceof IrGetelementptr) {
                        Value basePtr = ((IrGetelementptr) to).getOperands().get(0);
                        if (basePtr.getName().charAt(0) == '@') {
                            hasSideEffects = true;
                            break;
                        } else if (basePtr instanceof IrLoad) {
                            basePtr = ((IrLoad) basePtr).getOperands().get(0);
                            for (int i = 0; i < func.getParams().size(); i++) {
                                if (instrs.get(i * 2).equals(basePtr)) {
                                    hasSideEffects = true;
                                    break;
                                }
                            }
                            if (hasSideEffects) {
                                break;
                            }
                        }
                    }
                }
            }
            func.setCall(call);
            func.setSideEffects(hasSideEffects);
        }
        boolean change = true;
        while (change) {
            change = false;
            for (Function func : IrModule.getFunctions()) {
                for (Function call : func.getCall()) {
                    if (call.hasSideEffects() && !func.hasSideEffects()) {
                        func.setSideEffects(true);
                        change = true;
                        break;
                    }
                }
            }
        }
    }

}
