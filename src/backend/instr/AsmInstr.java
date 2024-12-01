package backend.instr;

public class AsmInstr {
    private final String comment;

    public AsmInstr() {
        comment = "";
    }

    public AsmInstr(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return comment;
    }
}
