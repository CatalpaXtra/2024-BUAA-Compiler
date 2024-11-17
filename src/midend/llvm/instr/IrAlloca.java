package midend.llvm.instr;

import midend.llvm.RetValue;

public class IrAlloca extends IrInstr {
    private RetValue result;
    private String irType;
    private int size;

    public IrAlloca(RetValue result, String irType, int size) {
        this.result = result;
        this.irType = irType;
        this.size = size;
    }

    public String irOut() {
        String instr;
        if (size == -1) {
            instr = result.irOut() + " = alloca " + irType;
        } else {
            instr = result.irOut() + " = alloca [" + size + " x " + irType + "]";
        }
        return instr;
    }
}
