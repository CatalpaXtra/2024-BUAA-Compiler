package backend.instr;

import backend.Register;

public class AsmJump extends AsmInstr {
    public enum OP {
        j, jal, jr,
    }

    private OP op;
    private Register to;
    private String label;

    public AsmJump(OP op, Register to, String label) {
        this.op = op;
        this.to = to;
        this.label = label;
    }

    public String toString() {
        if (op == OP.jr) {
            return op + " " + to;
        } else {
            return op + " " + label;
        }
    }
}
