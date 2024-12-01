package backend;

import backend.global.AsmAsciiz;
import backend.global.AsmByte;
import backend.global.AsmWord;
import backend.instr.AsmAlu;
import backend.instr.AsmMem;
import midend.llvm.Constant;
import midend.llvm.IrModule;
import midend.llvm.Value;
import midend.llvm.function.Function;
import midend.llvm.function.IrBlock;
import midend.llvm.global.GlobalStr;
import midend.llvm.global.GlobalVal;
import midend.llvm.global.initval.InitVal;
import midend.llvm.global.initval.IrArray;
import midend.llvm.global.initval.IrString;
import midend.llvm.global.initval.IrVar;
import midend.llvm.instr.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Builder {
    private final ArrayList<GlobalVal> globalVals;
    private final ArrayList<GlobalStr> globalStrs;
    private final ArrayList<Function> functions;
    private Function curFunc;
    private HashMap<Value, Register> regMap;
    private HashMap<Value, Integer> offsetMap;
    private int curOffset;
    private int curInstr;
    private ArrayList<IrInstr> instrs;

    public Builder() {
        this.globalVals = IrModule.getGlobalVals();
        this.globalStrs = IrModule.getGlobalStrs();
        this.functions = IrModule.getFunctions();
    }

    public void build() {
        for (GlobalVal globalVal : globalVals) {
            buildGlobalVal(globalVal);
        }
        for (GlobalStr globalStr : globalStrs) {
            buildGlobalStr(globalStr);
        }
        for (Function function : functions) {
            buildFunction(function);
        }
    }

    private void buildGlobalVal(GlobalVal globalVal) {
        String name = globalVal.getName();
        int size = globalVal.getArraySize();
        ArrayList<Integer> asmInitVal = new ArrayList<>();
        InitVal irInitVal = globalVal.getInitVal();
        if (irInitVal instanceof IrVar) {
            asmInitVal.add(((IrVar) irInitVal).getValue());
        } else if (irInitVal instanceof IrArray) {
            asmInitVal.addAll(((IrArray) irInitVal).getConstExpSet());
        }

        if (globalVal.getIrType().equals("i32")) {
            AsmWord asmWord = new AsmWord(name, size, asmInitVal);
            Module.addAsmGlobal(asmWord);
        } else {
            if (irInitVal instanceof IrString) {
                String string = ((IrString) irInitVal).getStringConst();
                AsmAsciiz asmAsciiz = new AsmAsciiz(name, string);
                Module.addAsmGlobal(asmAsciiz);
            } else {
                AsmByte asmByte = new AsmByte(name, size, asmInitVal);
                Module.addAsmGlobal(asmByte);
            }
        }
    }

    private void buildGlobalStr(GlobalStr globalStr) {
        String name = globalStr.getName().substring(1);
        String string = globalStr.getString();
        AsmAsciiz asmAsciiz = new AsmAsciiz(name, string);
        Module.addAsmGlobal(asmAsciiz);
    }

    private void buildFunction(Function function) {
        curFunc = function;
        regMap = new HashMap<>();
        offsetMap = new HashMap<>();
        curOffset = 0;
        Module.addAsmLabel(curFunc.getName());

        // TODO pass Param
//        ArrayList<Param> params = function.getParams();
//        for (int i = 0; i < params.size() - 1; i++) {
//            curOffset -= 4;
//            if (i < 3) {
//                regMap.put(params.get(i), Register.getByOffset(Register.a1, i));
//            }
//            offset.put(params.get(i), curOffset);
//        }
        buildIrBlock(function.getIrBlock());
        Module.addAsmNull("");
    }

    private void buildIrBlock(IrBlock irBlock) {
        instrs = irBlock.getInstructions();
        for (IrInstr instr : instrs) {
            curOffset -= 4;
            offsetMap.put(instr, curOffset);
        }
        for (curInstr = 0; curInstr < instrs.size(); curInstr++) {
            buildIrInstr(instrs.get(curInstr));
        }
    }

    private void buildIrInstr(IrInstr instr) {
        Module.addAsmNull(instr.toString());
        if (instr instanceof IrAlloca) {
            buildAlloca((IrAlloca) instr);
        } else if (instr instanceof IrBinary) {
            buildBinary((IrBinary) instr);
        } else if (instr instanceof IrBr) {
            buildBr((IrBr) instr);
        } else if (instr instanceof IrCall) {
            String funcName = ((IrCall) instr).getFuncName();
            if (funcName.equals("getint")) {
                buildGetInt();
            } else if (funcName.equals("getchar")) {
                buildGetChar();
            } else if (funcName.equals("putint")) {
                buildPutInt((IrCall) instr);
            } else if (funcName.equals("putch")) {
                buildPutCh((IrCall) instr);
            } else  {
                buildCall((IrCall) instr);
            }
        } else if (instr instanceof IrPutStr) {
            buildPutStr((IrPutStr) instr);
        } else if (instr instanceof IrGetelementptr) {
            buildGetelementptr((IrGetelementptr) instr);
        } else if (instr instanceof IrIcmp) {
            buildIcmp((IrIcmp) instr);
        } else if (instr instanceof IrLabel) {
            buildLabel((IrLabel) instr);
        } else if (instr instanceof IrLoad) {
            buildLoad((IrLoad) instr);
        } else if (instr instanceof IrStore) {
            buildStore((IrStore) instr);
        } else if (instr instanceof IrRet) {
            buildRet((IrRet) instr);
        } else if (instr instanceof IrTrunc) {
            buildTrunc((IrTrunc) instr);
        } else if (instr instanceof IrZext) {
            buildZext((IrZext) instr);
        }
    }

    private void buildLabel(IrLabel irLabel) {
        String label = curFunc.getName() + "_" + irLabel.getLabel();
        Module.addAsmLabel(label);
    }

    private void buildRet(IrRet irRet) {
        if (curFunc.getName().equals("main")) {
            Module.addAsmLi(Register.v0, 10);
            Module.addAsmSyscall();
        } else {

        }
    }

    private void buildAlloca(IrAlloca irAlloca) {
        int size = irAlloca.getSize();
        if (size != -1) {
            /* Alloc Space For Array */
            curOffset -= 4 * size;
            Module.addAsmAlu(AsmAlu.OP.addu, Register.t0, Register.sp, null, curOffset);
            /* Store Loc Of First Element */
            Module.addAsmMem(AsmMem.Type.sw, Register.t0, offsetMap.get(irAlloca), Register.sp);
        }
    }

    private void buildLoad(IrLoad irLoad) {
        Value pointer = irLoad.getOperands().get(0);
        Register resReg = Register.t0;
        if (pointer instanceof IrAlloca) {
            if (regMap.containsKey(pointer)) {
                Module.addAsmMove(regMap.get(pointer), resReg);
            } else {
//                Module.addAsmMem(AsmMem.Type.lw, resReg, offsetMap.get(pointer), Register.sp);
                offsetMap.put(irLoad, offsetMap.get(pointer));
            }
        } else if (pointer instanceof IrGetelementptr) {
            if (regMap.containsKey(pointer)) {
                Module.addAsmMove(regMap.get(pointer), resReg);
            } else {
                Module.addAsmMem(AsmMem.Type.lw, resReg, offsetMap.get(pointer), Register.sp);
                Module.addAsmMem(AsmMem.Type.lw, resReg, 0, resReg);
                Module.addAsmMem(AsmMem.Type.sw, resReg, offsetMap.get(irLoad), Register.sp);
            }
        } else {
            /* Load Global Val */
            Module.addAsmLa(resReg, pointer.getName().substring(1));
            Module.addAsmMem(AsmMem.Type.lw, resReg, 0, resReg);
            Module.addAsmMem(AsmMem.Type.sw, resReg, offsetMap.get(irLoad), Register.sp);
        }
    }

    private void buildStore(IrStore irStore) {
        Value value = irStore.getOperands().get(0);
        Value pointer = irStore.getOperands().get(1);
        Register resReg = Register.t0;
        Register ptrReg = Register.t1;
        if (value instanceof Constant) {
            Module.addAsmLi(resReg, ((Constant) value).getValue());
        } else {
            Module.addAsmMem(AsmMem.Type.lw, resReg, offsetMap.get(value), Register.sp);
        }

        if (pointer instanceof IrGetelementptr) {
            Module.addAsmMem(AsmMem.Type.lw, ptrReg, offsetMap.get(pointer), Register.sp);
            Module.addAsmMem(AsmMem.Type.sw, resReg, 0, ptrReg);
        } else {
            Module.addAsmMem(AsmMem.Type.sw, resReg, offsetMap.get(pointer), Register.sp);
        }
    }

    private void buildBinary(IrBinary irBinary) {
        Value operand1 = irBinary.getOperands().get(0);
        Value operand2 = irBinary.getOperands().get(1);
        String operator = irBinary.getOperator();
        AsmAlu.OP op = null;
        switch (operator) {
            case "add":
                op = AsmAlu.OP.addu;
                break;
            case "sub":
                op = AsmAlu.OP.subu;
                break;
            case "mul":
                op = AsmAlu.OP.mul;
                break;
            case "sdiv":case "srem":
                op = AsmAlu.OP.div;
                break;
            default:
                break;
        }

        Register resReg = Register.t1;
        Register tmpReg = Register.t1;
        Register tmpReg2 = Register.t2;
        if (op == AsmAlu.OP.div) {
            if (operand1 instanceof Constant) {
                Module.addAsmLi(tmpReg, ((Constant) operand1).getValue());
                int offset2 = offsetMap.get(operand2);
                Module.addAsmMem(AsmMem.Type.lw, tmpReg2, offset2, Register.sp);
            } else if (operand2 instanceof Constant) {
                int offset1 = offsetMap.get(operand1);
                Module.addAsmMem(AsmMem.Type.lw, tmpReg, offset1, Register.sp);
                Module.addAsmLi(tmpReg2, ((Constant) operand2).getValue());
            } else {
                int offset1 = offsetMap.get(operand1);
                Module.addAsmMem(AsmMem.Type.lw, tmpReg, offset1, Register.sp);
                int offset2 = offsetMap.get(operand2);
                Module.addAsmMem(AsmMem.Type.lw, tmpReg2, offset2, Register.sp);
            }
            Module.addAsmAlu(op, tmpReg, tmpReg2);

            if (operator.equals("sdiv")) {
                Module.addAsmMove(resReg, Register.lo);
            } else {
                Module.addAsmMove(resReg, Register.hi);
            }
        } else {
            if (operand1 instanceof Constant) {
                int offset = offsetMap.get(operand2);
                Module.addAsmMem(AsmMem.Type.lw, tmpReg, offset, Register.sp);
                Module.addAsmAlu(op, resReg, tmpReg, null, ((Constant) operand1).getValue());
            } else if (operand2 instanceof Constant) {
                int offset = offsetMap.get(operand1);
                Module.addAsmMem(AsmMem.Type.lw, tmpReg, offset, Register.sp);
                Module.addAsmAlu(op, resReg, tmpReg, null, ((Constant) operand2).getValue());
            } else {
                int offset1 = offsetMap.get(operand1);
                Module.addAsmMem(AsmMem.Type.lw, tmpReg, offset1, Register.sp);
                int offset2 = offsetMap.get(operand2);
                Module.addAsmMem(AsmMem.Type.lw, tmpReg2, offset2, Register.sp);
                Module.addAsmAlu(op, resReg, tmpReg, tmpReg2, 0);
            }
        }

        Module.addAsmMem(AsmMem.Type.sw, resReg, offsetMap.get(irBinary), Register.sp);
    }

    private void buildGetelementptr(IrGetelementptr irGetelementptr) {
        Value pointer = irGetelementptr.getOperands().get(0);
        Value offset = irGetelementptr.getOperands().get(1);
        Register pointReg = Register.k0;
        Register offsetReg = Register.k1;
        Register resReg = Register.k0;
        if (pointer instanceof IrAlloca || pointer instanceof IrGetelementptr) {
            Module.addAsmMem(AsmMem.Type.lw, pointReg, offsetMap.get(pointer), Register.sp);
        } else {
            Module.addAsmLa(pointReg, pointer.getName().substring(1));
        }

        if (offset instanceof Constant) {
            Module.addAsmAlu(AsmAlu.OP.addu, resReg, pointReg, null, ((Constant) offset).getValue() * 4);
        } else {
            if (regMap.containsKey(offset)) {
                offsetReg = regMap.get(offset);
            } else {
                Module.addAsmMem(AsmMem.Type.lw, offsetReg, offsetMap.get(offset) * 4, Register.sp);
            }
            /* Address + Offset, Find Value */
            Module.addAsmAlu(AsmAlu.OP.addu, offsetReg, pointReg, offsetReg, 0);
            Module.addAsmMem(AsmMem.Type.lw, resReg, 0, offsetReg);
        }

        Module.addAsmMem(AsmMem.Type.sw, resReg, offsetMap.get(irGetelementptr), Register.sp);
    }

    private void buildIcmp(IrIcmp irIcmp) {

    }

    private void buildBr(IrBr irBr) {

    }

    private void buildCall(IrCall irCall) {

    }

    private void buildGetInt() {
        Module.addAsmLi(Register.v0, 5);
        Module.addAsmSyscall();

        /* Read Next Store Instr And Assign */
        IrInstr instr = instrs.get(curInstr + 1);
        if (instr instanceof IrStore) {
            Value pointer = instr.getOperands().get(1);
            if (regMap.containsKey(pointer)) {
                Module.addAsmMove(regMap.get(pointer), Register.v0);
            } else {
                Module.addAsmMem(AsmMem.Type.sw, Register.v0, offsetMap.get(pointer), Register.sp);
            }
            curInstr++;
        }
    }

    private void buildPutInt(IrCall irCall) {
        Module.addAsmLi(Register.v0, 1);
        Value value = irCall.getOperands().get(0);
        if (value instanceof Constant) {
            Module.addAsmLi(Register.a0, ((Constant) value).getValue());
        } else {
            if (regMap.containsKey(value)) {
                Module.addAsmMove(regMap.get(value), Register.a0);
            } else {
                Module.addAsmMem(AsmMem.Type.lw, Register.a0, offsetMap.get(value), Register.sp);
            }
        }
        Module.addAsmSyscall();
    }

    private void buildGetChar() {
        Module.addAsmLi(Register.v0, 12);
        Module.addAsmSyscall();

        /* Read Next Store Instr And Assign */
        IrInstr instr = instrs.get(curInstr + 1);
        if (instr instanceof IrStore) {
            Value pointer = instr.getOperands().get(1);
            if (regMap.containsKey(pointer)) {
                Module.addAsmMove(regMap.get(pointer), Register.v0);
            } else {
                Module.addAsmMem(AsmMem.Type.sw, Register.v0, offsetMap.get(pointer), Register.sp);
            }
            curInstr++;
        }
    }

    private void buildPutCh(IrCall irCall) {
        Module.addAsmLi(Register.v0, 11);
        Value value = irCall.getOperands().get(0);
        if (value instanceof Constant) {
            Module.addAsmLi(Register.a0, ((Constant) value).getValue());
        } else {
            if (regMap.containsKey(value)) {
                Module.addAsmMove(regMap.get(value), Register.a0);
            } else {
                Module.addAsmMem(AsmMem.Type.lw, Register.a0, offsetMap.get(value), Register.sp);
            }
        }
        Module.addAsmSyscall();
    }

    private void buildPutStr(IrPutStr irPutStr) {
        Module.addAsmLi(Register.v0, 4);
        Module.addAsmLa(Register.a0, irPutStr.getStrName());
        Module.addAsmSyscall();
    }

    private void buildZext(IrZext irZext) {

    }

    private void buildTrunc(IrTrunc irTrunc) {

    }
}
