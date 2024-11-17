package midend.llvm.instr;

import midend.llvm.RetValue;

public class IrCall extends IrInstr {
    /* <result> = call [ret attrs] <ty> <name>(<...args>) */
    private final RetValue result;
    private final String funcType;
    private final String funcName;
    private final String rParams;

    public IrCall(RetValue result, String funcType, String funcName, String rParams) {
        this.result = result;
        this.funcType = funcType;
        this.funcName = funcName;
        this.rParams = rParams;
    }

    public String irOut() {
        String instr;
        if (funcType.equals("void")) {
            instr = "call void @" + funcName + "(" + rParams + ")";
        } else {
            instr = result.irOut() + " = call " + funcType + " @" + funcName + "(" + rParams + ")";
        }
        return instr;
    }
}
