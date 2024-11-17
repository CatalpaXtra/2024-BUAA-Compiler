package midend.llvm.instr;

import midend.llvm.RetValue;

public class IrBinary extends IrInstr {
    /* <result> = <operand> i32 <op1>, <op2> */
    private RetValue result;
    private String operand;
    private RetValue op1;
    private RetValue op2;

    public IrBinary(RetValue result, RetValue op1, RetValue op2, String operand) {
        this.result = result;
        this.op1 = op1;
        this.op2 = op2;
        this.operand = operand;
    }

    public String irOut() {
        String instr = result.irOut() + " = " + operand + " i32 " + op1.irOut() + ", " + op2.irOut();
        return instr;
    }

}
