package midend.optimizer;

import backend.Register;
import midend.llvm.Constant;
import midend.llvm.IrModule;
import midend.llvm.Value;
import midend.llvm.function.Function;
import midend.llvm.instr.IrInstr;

import java.util.*;

public class RegAlloc {
    private static ArrayList<Register> regSet = new ArrayList<>();
    private static HashMap<Value, ArrayList<Value>> edgeMap;
    private static  HashMap<Value, ArrayList<Value>> clonedMap;
    private static ArrayList<Value> deletedNodes;
    private static HashMap<Value, Register> var2reg;
    private static final int maxRegNum = 18;

    public static void allocReg() {
        regSet.addAll(Arrays.asList(Register.values()).subList(Register.t0.ordinal(), Register.s7.ordinal() + 1));
        ArrayList<Function> functions = IrModule.getFunctions();
        for (Function func : functions) {
            edgeMap = new HashMap<>();
            clonedMap = new HashMap<>();
            deletedNodes = new ArrayList<>();
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

//    public static void allocReg() {
//        regSet.addAll(Arrays.asList(Register.values()).subList(Register.t0.ordinal(), Register.s7.ordinal() + 1));
//        ArrayList<Function> functions = IrModule.getFunctions();
//        for (Function func : functions) {
//            edgeMap = new HashMap<>();
//            clonedMap = new HashMap<>();
//            deletedNodes = new ArrayList<>();
//            var2reg = new HashMap<>();
//
//            /* Put Edge */
//            ArrayList<IrInstr> instrs = func.getIrBlock().getInstructions();
//            for (int i = 0; i < func.getParams().size(); i++) {
//                IrInstr instr = instrs.get(2*i);
//                ArrayList<Value> conflictInstrs = new ArrayList<>();
//                for (int j = 0; j < func.getParams().size(); j++) {
//                    if (i != j) {
//                        conflictInstrs.add(instrs.get(2*j));
//                    }
//                }
//                putEdgeInMap(instr, conflictInstrs);
//            }
//            for (int i = func.getParams().size() + 1; i < instrs.size(); i++) {
//                handleInstr(instrs.get(i));
//            }
//
//            /* Delete Edge */
//            while (!edgeMap.isEmpty()) {
//                Iterator<Value> iterator = edgeMap.keySet().iterator();
//                boolean cannotDel = true;
//                while (iterator.hasNext()) {
//                    Value instr = iterator.next();
//                    if (edgeMap.get(instr).size() < maxRegNum) {
//                        iterator.remove();
//                        delEdgeFromMap(instr);
//                        deletedNodes.add(instr);
//                        cannotDel = false;
//                    }
//                }
//                if (cannotDel) {
//                    Value instr = edgeMap.keySet().iterator().next();
//                    edgeMap.remove(instr);
//                    delEdgeFromMap(instr);
//                }
//            }
//
//            /* Color Map */
//            edgeMap = clonedMap;
//            for (int i = deletedNodes.size() - 1; i >= 0; i--) {
//                Value instr = deletedNodes.get(i);
//                colorNode(instr);
//            }
//            func.setVar2reg(var2reg);
//            for (Value instr : var2reg.keySet()) {
//                System.out.println(instr.toString() + " -> " + var2reg.get(instr));
//            }
//            System.out.println();
//        }
//    }

    private static void handleInstr(IrInstr instr) {
        ArrayList<Value> operands = new ArrayList<>(instr.getOperands());
        operands.removeIf(value -> value instanceof Constant);
        operands.removeIf(value -> value == null);
        for (Value value : operands) {
            ArrayList<Value> conflictInstrs = new ArrayList<>(operands);
            conflictInstrs.remove(value);
            putEdgeInMap(value, conflictInstrs);
        }
        if (instr.hasLVal()) {
            putEdgeInMap(instr, operands);
        }
    }

    private static void putEdgeInMap(Value instr, ArrayList<Value> conflictInstrs) {
        if (!edgeMap.containsKey(instr)) {
            edgeMap.put(instr, new ArrayList<>());
            clonedMap.put(instr, new ArrayList<>());
        }
        ArrayList<Value> users = edgeMap.get(instr);
        ArrayList<Value> clonedUsers = clonedMap.get(instr);
        for (Value value : conflictInstrs) {
            if (value instanceof Constant) {
                continue;
            }
            users.add(value);
            clonedUsers.add(value);
            if (!edgeMap.containsKey(value)) {
                edgeMap.put(value, new ArrayList<>());
                clonedMap.put(value, new ArrayList<>());
            }
            ArrayList<Value> useds = edgeMap.get(value);
            useds.add(instr);
            ArrayList<Value> clonedUseds = clonedMap.get(value);
            clonedUseds.add(instr);
        }
    }

    private static void delEdgeFromMap(Value instr) {
        for (Value ins : edgeMap.keySet()) {
            ArrayList<Value> useds = edgeMap.get(ins);
            useds.remove(instr);
        }
    }

    private static void colorNode(Value instr) {
        ArrayList<Register> allocatedRegs = new ArrayList<>();
        for (Value node : edgeMap.get(instr)) {
            if (!var2reg.containsKey(node)) {
                continue;
            }
            allocatedRegs.add(var2reg.get(node));
        }
        for (Register reg : regSet) {
            if (!allocatedRegs.contains(reg)) {
                var2reg.put(instr, reg);
                return;
            }
        }
    }

}
