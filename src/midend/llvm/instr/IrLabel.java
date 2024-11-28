package midend.llvm.instr;

public class IrLabel extends IrInstr {
    private int label;

    public IrLabel(int label) {
        super(null, null);
        this.label = label;
    }

    public String getLabel() {
        return label + "";
    }

    public String toString() {
        return label + ":";
    }
}
