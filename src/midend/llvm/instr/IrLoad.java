package midend.llvm.instr;

import midend.llvm.RetValue;

public class IrLoad extends IrInstr {
    /* <result> = load <ty>, ptr <pointer> */
    private RetValue result;
    private String irType;
    private String pointer;

    public IrLoad(RetValue result, String irType, String pointer) {
        this.result = result;
        this.irType = irType;
        this.pointer = pointer;
    }

    public String irOut() {
        String instr = result.irOut() + " = load " + irType + ", " + irType + "* " + pointer;
        return instr;
    }
}
