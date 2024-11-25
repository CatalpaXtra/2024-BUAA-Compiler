package midend.llvm.instr;

import midend.llvm.Value;

public class IrAlloca extends IrInstr {
    private int size;

    public IrAlloca(String name, String irType, int size) {
        super(name, irType);
        this.size = size;
    }

    public String toString() {
        String instr;
        if (size == -1) {
            instr = name + " = alloca " + irType;
        } else {
            instr = name + " = alloca [" + size + " x " + irType + "]";
        }
        return instr;
    }
}
