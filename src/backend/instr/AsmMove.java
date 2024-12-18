package backend.instr;

import backend.Register;

public class AsmMove extends AsmInstr {
    private final Register to;
    private Register from;

    public AsmMove(Register to, Register from) {
        this.to = to;
        this.from = from;
    }

    public Register getTo() {
        return to;
    }

    public Register getFrom() {
        return from;
    }

    public void modifyFrom(Register reg) {
        this.from = reg;
    }

    public String toString() {
        if (from.equals(Register.lo) || from.equals(Register.hi)) {
            return "mf" + from.toString().substring(1) + " " + to;
        }
        return "move " + to + ", " + from;
    }
}
