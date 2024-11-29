package backend.instr;

import backend.Register;

public class AsmMove extends AsmInstr {
    private final Register from;
    private final Register to;

    public AsmMove(Register to, Register from) {
        this.from = from;
        this.to = to;
    }

    public String toString(){
        return "move " + to + ", " + from;
    }
}
