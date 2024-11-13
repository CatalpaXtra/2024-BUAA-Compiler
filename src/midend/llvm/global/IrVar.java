package midend.llvm.global;

public class IrVar extends Constant{
    private final int value;

    public IrVar(int value) {
        this.value = value;
    }

    public String irOut() {
        return value + "";
    }
}
