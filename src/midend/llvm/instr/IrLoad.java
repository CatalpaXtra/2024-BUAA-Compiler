package midend.llvm.instr;

import midend.llvm.Value;

public class IrLoad extends IrInstr {
    /* <result> = load <ty>, ptr <pointer> */
    private final Value pointer;

    public IrLoad(String name, String irType, Value pointer) {
        super(name, irType);
        this.pointer = pointer;
        addOperand(pointer);
    }

    public String toString() {
        String instr = name + " = load " + irType + ", " + irType + "* " + pointer.getName();
        return instr;
    }
}
