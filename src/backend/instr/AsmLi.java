package backend.instr;

import backend.Register;

public class AsmLi extends AsmInstr {
    private final Register reg;
    private final int value;

    public AsmLi(Register reg, int value) {
        this.reg = reg;
        this.value = value;
    }

    public String toString(){
        return "li " + reg + ", " + value;
    }
}
