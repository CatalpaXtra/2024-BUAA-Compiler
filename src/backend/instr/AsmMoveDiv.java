package backend.instr;

import backend.Register;

public class AsmMoveDiv extends AsmInstr {
    private final Register to;
    private final Register from;

    public AsmMoveDiv(Register to, Register from) {
        this.to = to;
        this.from = from;
    }

    public String toString() {
        return "mf" + from.toString().substring(1) + " " + to;
    }
}
