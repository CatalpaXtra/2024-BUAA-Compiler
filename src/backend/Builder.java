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
    private HashMap<Value, Integer> offset;
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
        String name = globalStr.getName().replace("@", "");
        String string = globalStr.getString();
        AsmAsciiz asmAsciiz = new AsmAsciiz(name, string);
        Module.addAsmGlobal(asmAsciiz);
    }

    private void buildFunction(Function function) {
        curFunc = function;
        regMap = new HashMap<>();
        offset = new HashMap<>();
        curOffset = 0;
        Module.addAsmLabel(curFunc.getName());
        ArrayList<Param> params = function.getParams();
        for (int i = 0; i < params.size() - 1; i++) {
            curOffset -= 4;
            if (i < 3) {
                regMap.put(params.get(i), Register.getByOffset(Register.a1, i));
            }
            offset.put(params.get(i), curOffset);
        }
        buildIrBlock(function.getIrBlock());
        Module.addAsmNull();
    }

    private void buildIrBlock(IrBlock irBlock) {
        instrs = irBlock.getInstructions();
        for (curInstr = 0; curInstr < instrs.size(); curInstr++) {
            buildIrInstr(instrs.get(curInstr));
        }
    }

    private void buildIrInstr(IrInstr instr) {
        if (instr instanceof IrAlloca) {
            buildAlloca((IrAlloca) instr);
        } else if (instr instanceof IrBinary) {
            buildBinary((IrBinary) instr);
        } else if (instr instanceof IrBr) {
            buildBr((IrBr) instr);
        } else if (instr instanceof IrCall) {
            buildCall((IrCall) instr);
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
            buildStore((IrStore) instr, Register.t2);
        } else if (instr instanceof IrRet) {
            buildRet((IrRet) instr);
        } else if (instr instanceof IrTrunc) {
            buildTrunc((IrTrunc) instr);
        } else if (instr instanceof IrZext) {
            buildZext((IrZext) instr);
        }
    }

    private void buildLabel(IrLabel irLabel) {
        Module.addAsmLabel(irLabel.getLabel());
    }

    private void buildAlloca(IrAlloca irAlloca) {
        offset.put(irAlloca, curOffset);
        curOffset -= 4;
    }

    private void buildLoad(IrLoad irLoad) {
        Value pointer = irLoad.getOperands().get(0);
        Register reg = Register.allocReg();
        if (pointer instanceof IrAlloca) {
            if (regMap.containsKey(pointer)) {
                Module.addAsmMove(regMap.get(pointer), reg);
            } else {
                Module.addAsmMem(AsmMem.Type.lw, reg, offset.get(pointer), Register.sp);
                offset.put(irLoad, offset.get(pointer));
            }
        } else {
            // TODO 全局变量的load
        }

    }

    private void buildStore(IrStore irStore, Register reg) {
        Value value = irStore.getOperands().get(0);
        if (value instanceof Constant) {
            reg = Register.allocReg();
            Module.addAsmLi(reg, ((Constant) value).getValue());
        } else {
            // TODO find reg that store value
        }

        Value pointer = irStore.getOperands().get(1);
        if (regMap.containsKey(pointer)) {
            Module.addAsmMove(regMap.get(pointer), reg);
        } else {
            Module.addAsmMem(AsmMem.Type.sw, reg, offset.get(pointer), Register.sp);
        }
        Register.resetCurReg();
    }

    private void buildRet(IrRet irRet) {
        if (curFunc.getName().equals("main")) {
            Module.addAsmLi(Register.v0, 10);
            Module.addAsmSyscall();
        } else {

        }
    }

    private void buildCall(IrCall irCall) {
        String funcName = irCall.getFuncName();
        if (funcName.equals("getint")) {
            buildGetInt(irCall);
        } else if (funcName.equals("getchar")) {
            buildGetChar(irCall);
        } else if (funcName.equals("putint")) {
            buildPutInt(irCall);
        } else if (funcName.equals("putch")) {
            buildPutCh(irCall);
        } else  {

        }
    }

    private void buildGetInt(IrCall irCall) {
        Module.addAsmLi(Register.v0, 5);
        Module.addAsmSyscall();

        // read next instr and assign
        IrInstr instr = instrs.get(curInstr + 1);
        if (instr instanceof IrStore) {
            buildStore((IrStore) instr, Register.v0);
            curInstr++;
        }
    }

    private void buildGetChar(IrCall irCall) {
        Module.addAsmLi(Register.v0, 12);
        Module.addAsmSyscall();
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
                Module.addAsmMem(AsmMem.Type.lw, Register.a0, offset.get(value), Register.sp);
            }
        }
        Module.addAsmSyscall();
    }

    private void buildPutCh(IrCall irCall) {
        Module.addAsmLi(Register.v0, 11);
        Module.addAsmSyscall();
    }

    private void buildPutStr(IrPutStr irPutStr) {
        Module.addAsmLi(Register.v0, 4);
        Module.addAsmLa(Register.a0, irPutStr.getStrName());
        Module.addAsmSyscall();
    }

    private void buildBinary(IrBinary irBinary) {
        Value operand1 = irBinary.getOperands().get(0);
        Value operand2 = irBinary.getOperands().get(1);
        String operator = irBinary.getOperator();
        AsmAlu.OP op;
        switch (operator) {
            case "add":
                op = AsmAlu.OP.addu;
                break;
            default:
                op = AsmAlu.OP.addu;
        }
        Register resReg = Register.t2;
        if (operand1 instanceof Constant) {
            Module.addAsmAlu(op, resReg, regMap.get(operand2), null, ((Constant) operand1).getValue());
        }
    }

    private void buildIcmp(IrIcmp irIcmp) {

    }

    private void buildBr(IrBr irBr) {

    }

    private void buildGetelementptr(IrGetelementptr irGetelementptr) {
        Value pointer = irGetelementptr.getOperands().get(0);
        Value offset = irGetelementptr.getOperands().get(1);
        Register pointReg = Register.k0;
        Register offsetReg = Register.k1;
        if (pointer instanceof IrAlloca) {
            Module.addAsmMem(AsmMem.Type.lw, pointReg, this.offset.get(pointer), Register.sp);
        } else {
            Module.addAsmLa(pointReg, pointer.getName());
        }

        if (offset instanceof Constant) {
            Module.addAsmMem(AsmMem.Type.lw, Register.allocReg(), ((Constant) offset).getValue(), pointReg);
        } else {
            if (regMap.containsKey(offset)) {
                offsetReg = regMap.get(offset);
            } else {
                Module.addAsmMem(AsmMem.Type.lw, offsetReg, this.offset.get(offset), Register.sp);
            }

            // address + offset
            Module.addAsmAlu(AsmAlu.OP.addu, offsetReg, pointReg, offsetReg, 0);

            Register resReg = Register.k0;
            Module.addAsmMem(AsmMem.Type.lw, resReg, 0, offsetReg);
        }

        IrInstr instr = instrs.get(curInstr + 1);
        if (instr instanceof IrLoad) {
            regMap.put(instr, Register.k0);
            curInstr++;
        }
    }

    private void buildZext(IrZext irZext) {

    }

    private void buildTrunc(IrTrunc irTrunc) {

    }
}
