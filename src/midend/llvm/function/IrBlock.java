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

    public void addInstrBinary(Value result, Value op1, Value op2, String operand) {
        IrBinary irBinary = new IrBinary(result, op1, op2, operand);
        addInstr(irBinary);
    }

    public void addInstrIcmp(Value result, String cond, Value op1, Value op2) {
        IrIcmp irIcmp = new IrIcmp(result, cond, op1, op2);
        addInstr(irIcmp);
    }

    public void addInstrCall(Value result, String funcType, String funcName, String rParams) {
        IrCall irCall = new IrCall(result, funcType, funcName, rParams);
        addInstr(irCall);
    }

    public void addInstrAlloca(Value result, String llvmType, int size) {
        IrAlloca irAlloca = new IrAlloca(result, llvmType, size);
        addInstr(irAlloca);
    }

    public void addInstrLoad(Value result, String llvmType, Value pointer) {
        IrLoad irLoad = new IrLoad(result, llvmType, pointer);
        addInstr(irLoad);
    }

    public void addInstrStore(String llvmType, Value value, Value pointer) {
        IrStore irStore = new IrStore(llvmType, value, pointer);
        addInstr(irStore);
    }

    public void addInstrGetelementptr(Value result, int size, String llvmType, Value pointer, Value offset) {
        IrGetelementptr irGetelementptr = new IrGetelementptr(result, llvmType, pointer, offset, size);
        addInstr(irGetelementptr);
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

    public void addInstrZext(Value result, String ty1, Value value, String ty2) {
        IrZext irZext = new IrZext(result, ty1, value, ty2);
        addInstr(irZext);
    }

    public void addInstrTrunc(Value result, String ty1, Value value, String ty2) {
        IrTrunc irTrunc = new IrTrunc(result, ty1, value, ty2);
        addInstr(irTrunc);
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

    public String irOut() {
        StringBuilder sb = new StringBuilder();
        for (IrInstr instr : instructions) {
            sb.append(instr.irOut()).append('\n');
        }
        return sb.toString();
    }
}
