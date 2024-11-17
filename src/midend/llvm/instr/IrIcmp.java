package midend.llvm.instr;

import midend.llvm.RetValue;

public class IrIcmp extends IrInstr {
    /* <result> = icmp <cond> <ty> <op1>, <op2> */
    private RetValue result;
    private String cond;
    private RetValue op1;
    private RetValue op2;

    public IrIcmp(RetValue result, String cond, RetValue op1, RetValue op2) {
        this.result = result;
        this.cond = cond;
        this.op1 = op1;
        this.op2 = op2;
    }

    public String irOut() {
        String instr = result.irOut() + " = icmp " + cond + " i32 " + op1.irOut() + ", " + op2.irOut();
        return instr;
    }
}
