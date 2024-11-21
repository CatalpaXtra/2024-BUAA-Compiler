package midend.llvm.instr;

import midend.llvm.Value;

public class IrIcmp extends IrInstr {
    /* <result> = icmp <cond> <ty> <op1>, <op2> */
    private Value result;
    private String cond;
    private Value op1;
    private Value op2;

    public IrIcmp(Value result, String cond, Value op1, Value op2) {
        super(result.irOut(), "i1");
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
