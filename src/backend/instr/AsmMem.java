package backend.instr;

import backend.Register;

public class AsmMem extends AsmInstr {
    public enum Type{
        sw, lw
    }
    private final Type type;
    private final Register value;
    private final int offset;
    private final Register base;

    public AsmMem(Type type, Register value, int offset, Register base) {
        this.type = type;
        this.value = value;
        this.offset = offset;
        this.base = base;
    }

    public String toString(){
        return type + " " + value + ", " + offset + "(" + base + ")";
    }
}
