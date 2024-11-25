package midend.llvm.function;

public class Param {
    private final String irType;
    private final String memory;

    public Param(String irType, String memory) {
        this.irType = irType;
        this.memory = memory;
    }

    public String getIrType() {
        return irType;
    }

    public String irOut() {
        return irType + " " + memory;
    }
}
