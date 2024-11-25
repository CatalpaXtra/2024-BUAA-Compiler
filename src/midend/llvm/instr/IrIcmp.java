package midend.llvm.instr;

import midend.llvm.Value;

public class IrIcmp extends IrInstr {
    /* <result> = icmp <cond> <ty> <op1>, <op2> */
    private String cond;
    private Value op1;
    private Value op2;

    public IrIcmp(String name, String cond, Value op1, Value op2) {
        super(name, "i1");
        this.cond = cond;
        this.op1 = op1;
        this.op2 = op2;
    }

    public String toString() {
        String instr = name + " = icmp " + cond + " i32 " + op1.getName() + ", " + op2.getName();
        return instr;
    }
}
