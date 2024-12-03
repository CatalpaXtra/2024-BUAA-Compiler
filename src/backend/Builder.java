package backend;

import backend.global.AsmAsciiz;
import backend.global.AsmWord;
import backend.instr.*;
import midend.llvm.Constant;
import midend.llvm.IrModule;
import midend.llvm.Value;
import midend.llvm.function.Function;
import midend.llvm.function.IrBlock;
import midend.llvm.function.Param;
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
        buildFunction(functions.get(functions.size() - 1));
        for (int i = 0; i < functions.size() - 1; i++) {
            buildFunction(functions.get(i));
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
        } else if (irInitVal instanceof IrString) {
            String string = ((IrString) irInitVal).getStringConst();
            for (int i = 0; i < string.length(); i++){
                if (string.charAt(i) == '\\') {
                    asmInitVal.add(10);
                    i++;
                } else {
                    asmInitVal.add((int) string.charAt(i));
                }
            }
        }

        /* All Store By Word */
        AsmWord asmWord = new AsmWord(name, size, asmInitVal);
        Module.addAsmGlobal(asmWord);
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

        /* Pass Param */
        ArrayList<Param> params = function.getParams();
        for (int i = 0; i < params.size(); i++) {
            if (i < 3) {
                regMap.put(params.get(i), Register.getByOffset(Register.a1, i));
            } else {
                curOffset -= 4;
                offsetMap.put(params.get(i), curOffset);
            }
        }
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

    private String labelEncrypt(String label) {
        if (label.charAt(0) == '%') {
            return curFunc.getName() + "_" + label.substring(1);
        }
        return curFunc.getName() + "_" + label;
    }

    private void buildLabel(IrLabel irLabel) {
        Module.addAsmLabel(labelEncrypt(irLabel.getLabel()));
    }

    private void buildRet(IrRet irRet) {
        if (curFunc.getName().equals("main")) {
            Module.addAsmLi(Register.v0, 10);
            Module.addAsmSyscall();
        } else if (curFunc.getIrType().equals("void")) {
            Module.addAsmJump(AsmJump.OP.jr, Register.ra, null);
        } else {
            Value value = irRet.getOperands().get(0);
            if (value instanceof Constant) {
                Module.addAsmLi(Register.v0, ((Constant) value).getValue());
            } else {
                if (regMap.containsKey(value)) {
                    Module.addAsmMove(Register.v0, regMap.get(value));
                } else {
                    Module.addAsmMem(AsmMem.Type.lw, Register.v0, offsetMap.get(value), Register.sp);
                }
            }
            Module.addAsmJump(AsmJump.OP.jr, Register.ra, null);
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
            if (regMap.containsKey(value)) {
                Module.addAsmMove(resReg, regMap.get(value));
            } else {
                Module.addAsmMem(AsmMem.Type.lw, resReg, offsetMap.get(value), Register.sp);
            }
        }

        if (irStore.getIrType().equals("i8")) {
            Module.addAsmAlu(AsmAlu.OP.andi, resReg, resReg, null, 0xFF);
        }

        if (pointer instanceof IrAlloca) {
            if (regMap.containsKey(pointer)) {
                Module.addAsmMove(regMap.get(pointer), resReg);
            } else {
                Module.addAsmMem(AsmMem.Type.sw, resReg, offsetMap.get(pointer), Register.sp);
            }
        } else if (pointer instanceof IrGetelementptr) {
            Module.addAsmMem(AsmMem.Type.lw, ptrReg, offsetMap.get(pointer), Register.sp);
            Module.addAsmMem(AsmMem.Type.sw, resReg, 0, ptrReg);
        } else {
            /* Store Global Val */
            Module.addAsmLa(ptrReg, pointer.getName().substring(1));
            Module.addAsmMem(AsmMem.Type.sw, resReg, 0, ptrReg);
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
            Module.addAsmAlu(op, null, tmpReg, tmpReg2, 0);

            if (operator.equals("sdiv")) {
                Module.addAsmMove(resReg, Register.lo);
            } else {
                Module.addAsmMove(resReg, Register.hi);
            }
        } else {
            if (operand1 instanceof Constant) {
                int offset = offsetMap.get(operand2);
                Module.addAsmMem(AsmMem.Type.lw, tmpReg, offset, Register.sp);
                if (op == AsmAlu.OP.subu) {
                    Module.addAsmLi(tmpReg2, ((Constant) operand1).getValue());
                    Module.addAsmAlu(op, resReg, tmpReg2, tmpReg, 0);
                } else {
                    Module.addAsmAlu(op, resReg, tmpReg, null, ((Constant) operand1).getValue());
                }
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
        if (pointer instanceof IrAlloca || pointer instanceof IrGetelementptr || pointer.getIrType().contains("*")) {
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
                Module.addAsmMem(AsmMem.Type.lw, offsetReg, offsetMap.get(offset), Register.sp);
            }
            Module.addAsmAlu(AsmAlu.OP.mul, offsetReg, offsetReg, null, 4);
            /* Address + Offset, Find Value */
            Module.addAsmAlu(AsmAlu.OP.addu, resReg, pointReg, offsetReg, 0);
        }

        Module.addAsmMem(AsmMem.Type.sw, resReg, offsetMap.get(irGetelementptr), Register.sp);
    }

    private void buildIcmp(IrIcmp irIcmp) {
        Value operand1 = irIcmp.getOperands().get(0);
        Value operand2 = irIcmp.getOperands().get(1);
        String cond = irIcmp.getCond();
        AsmCmp.OP op = null;
        switch (cond) {
            case "sgt":
                op = AsmCmp.OP.sgt;
                break;
            case "sge":
                op = AsmCmp.OP.sge;
                break;
            case "slt":
                op = AsmCmp.OP.slt;
                break;
            case "sle":
                op = AsmCmp.OP.sle;
                break;
            case "ne":
                op = AsmCmp.OP.sne;
                break;
            case "eq":
                op = AsmCmp.OP.seq;
                break;
            default:
                break;
        }

        Register resReg = Register.t1;
        Register tmpReg = Register.t1;
        Register tmpReg2 = Register.t2;
        if (operand1 instanceof Constant) {
            Module.addAsmLi(tmpReg, ((Constant) operand1).getValue());
            if (regMap.containsKey(operand2)) {
                Module.addAsmMove(tmpReg2, regMap.get(operand2));
            } else {
                int offset = offsetMap.get(operand2);
                Module.addAsmMem(AsmMem.Type.lw, tmpReg2, offset, Register.sp);
            }
            Module.addAsmCmp(op, resReg, tmpReg, tmpReg2);
        } else if (operand2 instanceof Constant) {
            if (regMap.containsKey(operand1)) {
                Module.addAsmMove(tmpReg, regMap.get(operand1));
            } else {
                int offset = offsetMap.get(operand1);
                Module.addAsmMem(AsmMem.Type.lw, tmpReg, offset, Register.sp);
            }
            Module.addAsmLi(tmpReg2, ((Constant) operand2).getValue());
            Module.addAsmCmp(op, resReg, tmpReg, tmpReg2);
        } else {
            if (regMap.containsKey(operand1)) {
                Module.addAsmMove(tmpReg, regMap.get(operand1));
            } else {
                int offset = offsetMap.get(operand1);
                Module.addAsmMem(AsmMem.Type.lw, tmpReg, offset, Register.sp);
            }
            if (regMap.containsKey(operand2)) {
                Module.addAsmMove(tmpReg2, regMap.get(operand2));
            } else {
                int offset = offsetMap.get(operand2);
                Module.addAsmMem(AsmMem.Type.lw, tmpReg2, offset, Register.sp);
            }
            Module.addAsmCmp(op, resReg, tmpReg, tmpReg2);
        }
        Module.addAsmMem(AsmMem.Type.sw, resReg, offsetMap.get(irIcmp), Register.sp);
    }

    private void buildBr(IrBr irBr) {
        Value cond = irBr.getCond();
        Register resReg = Register.t1;
        if (cond == null) {
            Module.addAsmJump(AsmJump.OP.j, null, labelEncrypt(irBr.getLabel()));
        } else {
            Module.addAsmMem(AsmMem.Type.lw, resReg, offsetMap.get(cond), Register.sp);
            Module.addAsmBranch(AsmBranch.OP.bne, resReg, null, labelEncrypt(irBr.getLabel1()), 0);
            Module.addAsmJump(AsmJump.OP.j, null, labelEncrypt(irBr.getLabel2()));
        }
    }

    private void buildCall(IrCall irCall) {
        /* Save Allocated Registers */
        ArrayList<Register> allocatedRegs = new ArrayList<>(regMap.values());
        for (int i = 0; i < allocatedRegs.size(); i++) {
            Module.addAsmMem(AsmMem.Type.sw, allocatedRegs.get(i), curOffset - (i + 1) * 4, Register.sp);
        }
        Module.addAsmMem(AsmMem.Type.sw, Register.ra, curOffset - allocatedRegs.size() * 4 - 4, Register.sp);

        /* Pass Param */
        ArrayList<Value> operands = irCall.getOperands();
        for (int i = 0; i < operands.size(); i++) {
            Value operand = operands.get(i);
            if (i < 3) {
                /* Pass Param By Reg */
                Register paramReg = Register.getByOffset(Register.a1, i);
                if (operand instanceof Constant) {
                    Module.addAsmLi(paramReg, ((Constant) operand).getValue());
                } else if (regMap.containsKey(operand)) {
                    Module.addAsmMove(paramReg, regMap.get(operand));
                } else {
                    Module.addAsmMem(AsmMem.Type.lw, paramReg, offsetMap.get(operand), Register.sp);
                }
            } else {
                /* Pass More Param By Mem */
                Register resReg = Register.k0;
                if (operand instanceof Constant) {
                    Module.addAsmLi(resReg, ((Constant) operand).getValue());
                } else if (regMap.containsKey(operand)) {
                    resReg = regMap.get(operand);
                } else {
                    Module.addAsmMem(AsmMem.Type.lw, resReg, offsetMap.get(operand), Register.sp);
                }
                Module.addAsmMem(AsmMem.Type.sw, resReg, curOffset - allocatedRegs.size() * 4 - 4 - (i - 2) * 4, Register.sp);
            }
        }
        Module.addAsmAlu(AsmAlu.OP.addiu, Register.sp, Register.sp, null, curOffset - allocatedRegs.size() * 4 - 4);

        /* Call Func */
        Module.addAsmJump(AsmJump.OP.jal, null, irCall.getFuncName());

        /* Acquire Allocated Registers */
        Module.addAsmMem(AsmMem.Type.lw, Register.ra, 0, Register.sp);
        Module.addAsmAlu(AsmAlu.OP.addiu, Register.sp, Register.sp, null, -(curOffset - allocatedRegs.size() * 4 - 4));
        for (int i = 0; i < allocatedRegs.size(); i++) {
            Module.addAsmMem(AsmMem.Type.lw, allocatedRegs.get(i), curOffset - (i + 1) * 4, Register.sp);
        }

        /* Store Func Return Value */
        if (!curFunc.getIrType().equals("void")) {
            if (regMap.containsKey(irCall)) {
                Module.addAsmAlu(AsmAlu.OP.addiu, Register.v0, regMap.get(irCall), null, 0);
            } else {
                Module.addAsmMem(AsmMem.Type.sw, Register.v0, offsetMap.get(irCall), Register.sp);
            }
        }
    }

    private void buildGetInt() {
        Module.addAsmLi(Register.v0, 5);
        Module.addAsmSyscall();

        /* Read Next Store Instr And Assign */
        IrInstr instr = instrs.get(curInstr + 1);
        if (instr instanceof IrStore) {
            Value pointer = instr.getOperands().get(1);
            if (pointer instanceof IrAlloca) {
                if (regMap.containsKey(pointer)) {
                    Module.addAsmMove(regMap.get(pointer), Register.v0);
                } else {
                    Module.addAsmMem(AsmMem.Type.sw, Register.v0, offsetMap.get(pointer), Register.sp);
                }
            } else {
                Module.addAsmLa(Register.v1, pointer.getName().substring(1));
                Module.addAsmMem(AsmMem.Type.sw, Register.v0, 0, Register.v1);
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
                Module.addAsmMove(Register.a0, regMap.get(value));
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
        IrInstr instr1 = instrs.get(curInstr + 1);
        IrInstr instr2 = instrs.get(curInstr + 2);
        if (instr1 instanceof IrTrunc && instr2 instanceof IrStore) {
            Value pointer = instr2.getOperands().get(1);
            if (regMap.containsKey(pointer)) {
                Module.addAsmMove(regMap.get(pointer), Register.v0);
            } else {
                Module.addAsmMem(AsmMem.Type.sw, Register.v0, offsetMap.get(pointer), Register.sp);
            }
            curInstr += 2;
        }
    }

    private void buildPutCh(IrCall irCall) {
        Module.addAsmLi(Register.v0, 11);
        Value value = irCall.getOperands().get(0);
        if (value instanceof Constant) {
            Module.addAsmLi(Register.a0, ((Constant) value).getValue());
        } else {
            if (regMap.containsKey(value)) {
                Module.addAsmMove(Register.a0, regMap.get(value));
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
        Value operand = irZext.getOperands().get(0);
        offsetMap.put(irZext, offsetMap.get(operand));
    }

    private void buildTrunc(IrTrunc irTrunc) {
        Value operand = irTrunc.getOperands().get(0);
        offsetMap.put(irTrunc, offsetMap.get(operand));
    }
}
