package backend.instr;

import backend.Register;

public class AsmMem extends AsmInstr {
    public enum Type {
        sw, lw
    }

    private final Type type;
    private Register value;
    private final int offset;
    private final Register base;

    public AsmMem(Type type, Register value, int offset, Register base) {
        this.type = type;
        this.value = value;
        this.offset = offset;
        this.base = base;
    }

    public Register getValue() {
        return value;
    }

    public int getOffset() {
        return offset;
    }

    public Register getBase() {
        return base;
    }

    public Type getType() {
        return type;
    }

    public void modifyValue(Register reg) {
        this.value = reg;
    }

    public String toString() {
        return type + " " + value + ", " + offset + "(" + base + ")";
    }
}
