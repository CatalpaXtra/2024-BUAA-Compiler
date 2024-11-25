package midend.llvm.instr;

import midend.llvm.Value;

public class IrRet extends IrInstr {
    /* ret <type> <value> , ret void */
    private String retType;
    private Value value;

    public IrRet(String retType, Value value) {
        super(null, null);
        this.retType = retType;
        this.value = value;
    }

    public String toString() {
        String instr;
        if (retType.equals("void")) {
            instr = "ret void";
        } else {
            instr = "ret " + retType + " " + value.getName();
        }
        return instr;
    }
}
