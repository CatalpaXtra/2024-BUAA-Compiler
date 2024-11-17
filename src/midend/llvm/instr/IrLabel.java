package midend.llvm.instr;

public class IrLabel extends IrInstr {
    private int label;

    public IrLabel(int label) {
        this.label = label;
    }

    public String irOut() {
        return label + ":";
    }
}
