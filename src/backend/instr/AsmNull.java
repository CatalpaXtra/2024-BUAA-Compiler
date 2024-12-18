package backend.instr;

public class AsmNull extends AsmInstr {
    private final String comment;

    public AsmNull(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return comment;
    }
}
