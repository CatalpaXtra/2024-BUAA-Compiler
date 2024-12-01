package backend.instr;

import backend.Register;

public class AsmMove extends AsmInstr {
    private final Register to;
    private final Register from;

    public AsmMove(Register to, Register from) {
        this.to = to;
        this.from = from;
    }

    public String toString() {
        if (from.equals(Register.lo) || from.equals(Register.hi)) {
            return "mf" + from.toString().substring(1) + " " + to;
        }
        return "move " + to + ", " + from;
    }
}
