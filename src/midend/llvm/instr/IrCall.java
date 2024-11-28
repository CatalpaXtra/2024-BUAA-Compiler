package midend.llvm.instr;

import midend.llvm.Value;

public class IrCall extends IrInstr {
    /* <result> = call [ret attrs] <ty> <name>(<...args>) */
    private final String funcName;
    private final String rParams;

    public IrCall(String name, String funcType, String funcName, String rParams) {
        super(name, funcType);
        this.funcName = funcName;
        this.rParams = rParams;
    }

    public String getFuncName() {
        return funcName;
    }

    public String toString() {
        String instr;
        if (irType.equals("void")) {
            instr = "call void @" + funcName + "(" + rParams + ")";
        } else {
            instr = name + " = call " + irType + " @" + funcName + "(" + rParams + ")";
        }
        return instr;
    }
}
