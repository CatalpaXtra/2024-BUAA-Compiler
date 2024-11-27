package midend.llvm.global.initval;

public class IrVar extends InitVal {
    private final int value;

    public IrVar(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String toString() {
        return value + "";
    }
}
