package backend.instr;

public class AsmLabel extends AsmInstr {
    private final String name;

    public AsmLabel(String name) {
        this.name = name;
    }

    public String getLabel() {
        return name;
    }

    @Override
    public String toString() {
        return name + ":";
    }
}
