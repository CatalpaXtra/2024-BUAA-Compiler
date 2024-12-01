package backend.instr;

import backend.Register;

public class AsmCmp extends AsmInstr {
    public enum OP {
        sgt, slt,
        sge, sle,
        seq, sne
    }

    private OP op;
    private Register to;
    private Register operand1;
    private Register operand2;
    private int num;

    public AsmCmp(OP op, Register to, Register operand1, Register operand2, int num) {
        this.op = op;
        this.to = to;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.num = num;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(op + " " + to + ", " + operand1 + ", ");
        if (operand2 == null) {
            sb.append(num);
        } else {
            sb.append(operand2);
        }
        return sb.toString();
    }
}
