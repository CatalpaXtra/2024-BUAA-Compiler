package midend.llvm.instr;

import midend.llvm.Value;

public class IrStore extends IrInstr {
    /* store <ty> <value>, ptr <pointer> */
    private String irType;
    private Value value;
    private Value pointer;

    public IrStore(String irType, Value value, Value pointer) {
        super(null, null);
        this.irType = irType;
        this.value = value;
        this.pointer = pointer;
    }

    public String irOut() {
        String instr = "store " + irType + " " + value.irOut() + ", " + irType + "* " + pointer.irOut();
        return instr;
    }
}
