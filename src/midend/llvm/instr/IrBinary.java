package midend.llvm.instr;

import midend.llvm.Value;

public class IrBinary extends IrInstr {
    /* <result> = <operand> i32 <op1>, <op2> */
    private String operator;
    private Value op1;
    private Value op2;

    public IrBinary(String name, Value op1, Value op2, String operator) {
        super(name, "i32");
        this.op1 = op1;
        this.op2 = op2;
        this.operator = operator;
        addOperand(op1);
        addOperand(op2);
    }

    public String getOperator() {
        return operator;
    }

    public String toString() {
        String instr = name + " = " + operator + " i32 " + op1.getName() + ", " + op2.getName();
        return instr;
    }

}
