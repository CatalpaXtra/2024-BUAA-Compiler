package midend.llvm.instr;

import midend.llvm.RetValue;

public class IrRet extends IrInstr {
    /* ret <type> <value> , ret void */
    private String retType;
    private RetValue value;

    public IrRet(String retType, RetValue value) {
        this.retType = retType;
        this.value = value;
    }

    public String irOut() {
        String instr;
        if (retType.equals("void")) {
            instr = "ret void";
        } else {
            instr = "ret " + retType + " " + value.irOut();
        }
        return instr;
    }
}
