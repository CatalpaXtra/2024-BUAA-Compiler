package midend.llvm.instr;

public class IrStore extends IrInstr {
    /* store <ty> <value>, ptr <pointer> */
    private String irType;
    private String value;
    private String pointer;

    public IrStore(String irType, String value, String pointer) {
        this.irType = irType;
        this.value = value;
        this.pointer = pointer;
    }

    public String irOut() {
        String instr = "store " + irType + " " + value + ", " + irType + "* " + pointer;
        return instr;
    }
}
