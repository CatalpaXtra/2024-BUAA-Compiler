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

    public AsmCmp(OP op, Register to, Register operand1, Register operand2) {
        this.op = op;
        this.to = to;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    public String toString() {
        return op + " " + to + ", " + operand1 + ", " + operand2;
    }
}
