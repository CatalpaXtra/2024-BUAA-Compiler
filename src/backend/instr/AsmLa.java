package backend.instr;

import backend.Register;

public class AsmLa extends AsmInstr {
    private final Register reg;
    private final String name;

    public AsmLa(Register reg, String name) {
        this.reg = reg;
        this.name = name;
    }

    public String toString(){
        return "la " + reg + ", " + name;
    }
}
