package backend.instr;

import backend.Register;

public class AsmAlu extends AsmInstr {
    public enum OP {
        addi, addu, addiu, subu,
        mul, mult, div,
        srl, sra, sll, madd, andi,
    }

    private OP op;
    private Register to;
    private Register operand1;
    private Register operand2;
    private int num;

    public AsmAlu(OP op, Register to, Register operand1, Register operand2, int num) {
        this.op = op;
        this.to = to;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.num = num;
    }

    public Register getOperand1() {
        return operand1;
    }

    public void modifyOperand1(Register reg) {
        operand1 = reg;
    }

    public Register getOperand2() {
        return operand2;
    }

    public void modifyOperand2(Register reg) {
        operand2 = reg;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(op + " ");
        if (op == OP.div || op == OP.mult || op == OP.madd) {
            sb.append(operand1).append(", ").append(operand2);
        } else {
            sb.append(to).append(", ").append(operand1).append(", ");
            if (operand2 == null) {
                sb.append(num);
            } else {
                sb.append(operand2);
            }
        }
        return sb.toString();
    }
}
