package midend.optimizer;

import backend.Register;
import midend.llvm.Constant;
import midend.llvm.IrModule;
import midend.llvm.Value;
import midend.llvm.function.Function;
import midend.llvm.function.Param;
import midend.llvm.instr.IrAlloca;
import midend.llvm.instr.IrInstr;
import midend.llvm.instr.IrLoad;
import midend.llvm.instr.IrStore;

import java.util.*;

public class RegAlloc {
    private static HashMap<Value, Register> var2reg;
    private static final int maxRegNum = 1;

    public static void allocReg() {
        ArrayList<Function> functions = IrModule.getFunctions();
        for (Function func : functions) {
            var2reg = new HashMap<>();

            HashMap<Value, Integer> refCount = new HashMap<>();
            ArrayList<IrInstr> instrs = func.getIrBlock().getInstructions();
            ArrayList<IrInstr> paramRegs = new ArrayList<>();
            for (int i = 0; i < instrs.size(); i++) {
                if (i < 6 && instrs.get(i) instanceof IrAlloca && i + 1 < instrs.size()) {
                    if (instrs.get(i + 1) instanceof IrStore && instrs.get(i + 1).getOperands().get(0) instanceof Param) {
                        paramRegs.add(instrs.get(i));
                        i++;
                        continue;
                    }
                }
                for (Value value : instrs.get(i).getOperands()) {
                    if (value == null || value instanceof Constant || value instanceof Param || value instanceof IrLoad
                            || value.getName().charAt(0) == '@' || paramRegs.contains(value)) {
                        continue;
                    }
                    if (refCount.containsKey(value)) {
                        refCount.put(value, refCount.get(value) + 1);
                    } else {
                        refCount.put(value, 1);
                    }
                }
            }

            int allocNum = 0;
            int callCost = func.getCallList().size() * 2 * 4;
            List<Map.Entry<Value, Integer>> list = new ArrayList<>(refCount.entrySet());
            list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
            for (Map.Entry<Value, Integer> entry : list) {
                if (allocNum >= maxRegNum) {
                    break;
                }
                if (entry.getValue() * 3 > callCost) {
                    var2reg.put(entry.getKey(), Register.getByOffset(Register.s0, allocNum));
                    allocNum++;
                }
                System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
            }

            /* Color Map */
            func.setVar2reg(var2reg);
            for (Value instr : var2reg.keySet()) {
                System.out.println(instr.toString() + " -> " + var2reg.get(instr));
            }
            System.out.println();
        }
    }

    public static void allocReg1() {
        ArrayList<Function> functions = IrModule.getFunctions();
        for (Function func : functions) {
            var2reg = new HashMap<>();

            /* Put Edge */
            ArrayList<IrInstr> instrs = func.getIrBlock().getInstructions();
            int allocNum = 0;
            for (int i = 2 * func.getParams().size(); i < instrs.size() && allocNum < maxRegNum; i++) {
                if (instrs.get(i).hasLVal()) {
                    var2reg.put(instrs.get(i), Register.getByOffset(Register.t0, allocNum));
                    allocNum++;
                }
            }

            /* Color Map */
            func.setVar2reg(var2reg);
            for (Value instr : var2reg.keySet()) {
                System.out.println(instr.toString() + " -> " + var2reg.get(instr));
            }
            System.out.println();
        }
    }

}
