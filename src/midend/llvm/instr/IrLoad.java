package midend.llvm.instr;

import midend.llvm.Value;

public class IrLoad extends IrInstr {
    /* <result> = load <ty>, ptr <pointer> */
    private Value result;
    private String irType;
    private Value pointer;

    public IrLoad(Value result, String irType, Value pointer) {
        super(result.irOut(), irType);
        this.result = result;
        this.irType = irType;
        this.pointer = pointer;
    }

    public String irOut() {
        String instr = result.irOut() + " = load " + irType + ", " + irType + "* " + pointer.irOut();
        return instr;
    }
}
