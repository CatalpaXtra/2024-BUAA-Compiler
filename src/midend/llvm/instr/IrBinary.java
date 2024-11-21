package midend.llvm.instr;

import midend.llvm.Value;

public class IrBinary extends IrInstr {
    /* <result> = <operand> i32 <op1>, <op2> */
    private Value result;
    private String operand;
    private Value op1;
    private Value op2;

    public IrBinary(Value result, Value op1, Value op2, String operand) {
        super(result.irOut(), "i32");
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
