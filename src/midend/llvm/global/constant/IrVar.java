package midend.llvm.global.constant;

public class IrVar extends IrCon {
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
