package backend.instr;

import backend.Register;

public class AsmBranch extends AsmInstr {
    public enum OP {
        bgt, blt, bge, ble,
        beq, bne
    }

    private OP op;
    private Register reg1;
    private Register reg2;
    private String label;
    private int num;

    public AsmBranch(OP op, Register reg1, Register reg2, String label, int num) {
        this.reg1 = reg1;
        this.reg2 = reg2;
        this.label = label;
        this.op = op;
        this.num = num;
    }

    public String toString() {
        if (reg2 == null) {
            return op + " " + reg1 + ", " + num + ", " + label;
        } else {
            return op + " " + reg1 + ", " + reg2 + ", " + label;
        }
    }
}
