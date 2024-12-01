package backend.instr;

import backend.Register;

public class AsmAlu extends AsmInstr {
    public enum OP {
        addu, addiu, subu,
        mul, div,
        srl, sra, sll,
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

    public String toString() {
        StringBuilder sb = new StringBuilder(op + " ");
        if (op == OP.div) {
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
