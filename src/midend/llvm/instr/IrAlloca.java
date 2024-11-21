package midend.llvm.instr;

import midend.llvm.Value;

public class IrAlloca extends IrInstr {
    private Value result;
    private String irType;
    private int size;

    public IrAlloca(Value result, String irType, int size) {
        super(result.irOut(), irType);
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
