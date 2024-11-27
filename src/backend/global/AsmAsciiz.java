package backend.global;

public class AsmAsciiz extends AsmGlobal {
    private final String string;

    public AsmAsciiz(String name, String string) {
        super(name);
        this.string = string.replace("\\0A", "\\n").replace("\\00", "");
    }

    public String toString() {
        return name + ": .asciiz \"" + string + "\"";
    }
}
