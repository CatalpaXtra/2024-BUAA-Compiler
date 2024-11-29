package midend.llvm.instr;

import midend.llvm.Value;

public class IrStore extends IrInstr {
    /* store <ty> <value>, ptr <pointer> */
    private Value value;
    private Value pointer;

    public IrStore(String irType, Value value, Value pointer) {
        super(null, irType);
        this.value = value;
        this.pointer = pointer;
        addOperand(value);
        addOperand(pointer);
    }

    public String toString() {
        String instr = "store " + irType + " " + value.getName() + ", " + irType + "* " + pointer.getName();
        return instr;
    }
}
