package backend;

import backend.global.AsmAsciiz;
import backend.global.AsmByte;
import backend.global.AsmWord;
import midend.llvm.IrModule;
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

public class Builder {
    private final ArrayList<GlobalVal> globalVals;
    private final ArrayList<GlobalStr> globalStrs;
    private final ArrayList<Function> functions;
    private Function curFunc;

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
        Module.addAsmLabel(curFunc.getName());
        buildIrBlock(function.getIrBlock());
    }

    private void buildIrBlock(IrBlock irBlock) {
        ArrayList<IrInstr> instrs = irBlock.getInstructions();
        for (IrInstr instr : instrs) {
            buildIrInstr(instr);
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
            buildStore((IrStore) instr);
        } else if (instr instanceof IrRet) {
            buildRet((IrRet) instr);
        } else if (instr instanceof IrTrunc) {
            buildTrunc((IrTrunc) instr);
        } else if (instr instanceof IrZext) {
            buildZext((IrZext) instr);
        }
    }

    private void buildAlloca(IrAlloca irAlloca) {

    }

    private void buildBinary(IrBinary irBinary) {

    }

    private void buildBr(IrBr irBr) {

    }

    private void buildCall(IrCall irCall) {
        String funcName = irCall.getFuncName();
        if (funcName.equals("putint")) {
            Module.addAsmLi(Register.v0, 1);
            Module.addAsmSyscall();
        } else if (funcName.equals("putch")) {
            Module.addAsmLi(Register.v0, 11);
            Module.addAsmSyscall();
        } else if (funcName.equals("getint")) {
            Module.addAsmLi(Register.v0, 5);
            Module.addAsmSyscall();
        } else if (funcName.equals("getchar")) {
            Module.addAsmLi(Register.v0, 12);
            Module.addAsmSyscall();
        } else {

        }
    }

    private void buildPutStr(IrPutStr irPutStr) {
        Module.addAsmLa(Register.a0, irPutStr.getStrName());
        Module.addAsmLi(Register.v0, 4);
        Module.addAsmSyscall();
    }

    private void buildGetelementptr(IrGetelementptr irGetelementptr) {

    }

    private void buildIcmp(IrIcmp irIcmp) {

    }

    private void buildLabel(IrLabel irLabel) {
        Module.addAsmLabel(irLabel.getLabel());
    }

    private void buildLoad(IrLoad irLoad) {

    }

    private void buildStore(IrStore irStore) {

    }

    private void buildRet(IrRet irRet) {
        if (curFunc.getName().equals("main")) {
            Module.addAsmLi(Register.v0, 10);
            Module.addAsmSyscall();
        } else {

        }
    }

    private void buildZext(IrZext irZext) {

    }

    private void buildTrunc(IrTrunc irTrunc) {

    }
}
