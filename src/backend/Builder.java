package backend;

import backend.global.AsmAsciiz;
import backend.global.AsmByte;
import backend.global.AsmWord;
import midend.llvm.IrModule;
import midend.llvm.function.Function;
import midend.llvm.global.GlobalStr;
import midend.llvm.global.GlobalVal;
import midend.llvm.global.initval.InitVal;
import midend.llvm.global.initval.IrArray;
import midend.llvm.global.initval.IrString;
import midend.llvm.global.initval.IrVar;

import java.util.ArrayList;

public class Builder {
    private final ArrayList<GlobalVal> globalVals;
    private final ArrayList<GlobalStr> globalStrs;
    private final ArrayList<Function> functions;

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

    }
}
