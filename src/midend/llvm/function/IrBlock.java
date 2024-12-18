package midend.llvm.function;

import midend.llvm.Value;
import midend.llvm.instr.*;

import java.util.ArrayList;

public class IrBlock {
    private final ArrayList<IrInstr> instructions;

    public IrBlock() {
        this.instructions = new ArrayList<>();
    }

    public void addInstr(IrInstr instr) {
        instructions.add(instr);
    }

    public ArrayList<IrInstr> getInstructions() {
        return instructions;
    }

    public IrInstr addInstrBinary(String name, Value op1, Value op2, String operator) {
        IrBinary irBinary = new IrBinary(name, op1, op2, operator);
        addInstr(irBinary);
        return irBinary;
    }

    public IrInstr addInstrIcmp(String name, String cond, Value op1, Value op2) {
        IrIcmp irIcmp = new IrIcmp(name, cond, op1, op2);
        addInstr(irIcmp);
        return irIcmp;
    }

    public IrInstr addInstrCall(String name, String funcType, String funcName, ArrayList<Param> params, ArrayList<Value> values, Function func) {
        IrCall irCall = new IrCall(name, funcType, funcName, params, values, func);
        addInstr(irCall);
        return irCall;
    }

    public IrInstr addInstrCall(String name, String funcType, String funcName, ArrayList<Param> params, ArrayList<Value> values) {
        IrCall irCall = new IrCall(name, funcType, funcName, params, values, null);
        addInstr(irCall);
        return irCall;
    }

    public void addInstrPutStr(String strName, int len) {
        IrPutStr irPutStr = new IrPutStr(strName, len);
        addInstr(irPutStr);
    }

    public IrInstr addInstrAlloca(String name, String llvmType, int size) {
        IrAlloca irAlloca = new IrAlloca(name, llvmType, size);
        addInstr(irAlloca);
        return irAlloca;
    }

    public IrInstr addInstrLoad(String name, String llvmType, Value pointer) {
        IrLoad irLoad = new IrLoad(name, llvmType, pointer);
        addInstr(irLoad);
        return irLoad;
    }

    public void addInstrStore(String llvmType, Value value, Value pointer) {
        IrStore irStore = new IrStore(llvmType, value, pointer);
        addInstr(irStore);
    }

    public IrInstr addInstrGetelementptr(String name, int size, String llvmType, Value pointer, Value offset) {
        IrGetelementptr irGetelementptr = new IrGetelementptr(name, llvmType, pointer, offset, size);
        addInstr(irGetelementptr);
        return irGetelementptr;
    }

    public void addInstrBr(String dest) {
        IrBr irBr = new IrBr(dest);
        addInstr(irBr);
    }

    public void addInstrBrCond(Value result, String ifTrue, String ifFalse) {
        IrBr irBr = new IrBr(result, ifTrue, ifFalse);
        addInstr(irBr);
    }

    public void addInstrRet(String retType, Value value) {
        IrRet irRet = new IrRet(retType, value);
        addInstr(irRet);
    }

    public IrInstr addInstrZext(String name, String ty1, Value value, String ty2) {
        IrZext irZext = new IrZext(name, ty1, value, ty2);
        addInstr(irZext);
        return irZext;
    }

    public IrInstr addInstrTrunc(String name, String ty1, Value value, String ty2) {
        IrTrunc irTrunc = new IrTrunc(name, ty1, value, ty2);
        addInstr(irTrunc);
        return irTrunc;
    }

    public void addRetIfNotExist() {
        if (instructions.isEmpty()) {
            IrRet irRet = new IrRet("void", null);
            addInstr(irRet);
        } else {
            IrInstr instr = instructions.get(instructions.size() - 1);
            if (!(instr instanceof IrRet)) {
                IrRet irRet = new IrRet("void", null);
                addInstr(irRet);
            }
        }
    }

    public void addInstrLabel(int label) {
        IrLabel irLabel = new IrLabel(label);
        addInstr(irLabel);
    }

    public void addInstrNull() {
        IrInstr irInstr = new IrInstr(null, null);
        addInstr(irInstr);
    }

    public IrInstr getLastInstr() {
        return instructions.get(instructions.size() - 1);
    }

    public void delLastInstr() {
        instructions.remove(instructions.size() - 1);
    }

    public int getLoc() {
        return instructions.size() - 1;
    }

    public void replaceInterval(int left, int right, String replaced, String target) {
        if (left > right) {
            return;
        }
        for (int i = left; i <= right; i++) {
            IrInstr irInstr = instructions.get(i);
            if (irInstr instanceof IrBr) {
                ((IrBr) irInstr).backFill(target, replaced);
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (IrInstr instr : instructions) {
            sb.append(instr.toString()).append('\n');
        }
        return sb.toString();
    }
}
