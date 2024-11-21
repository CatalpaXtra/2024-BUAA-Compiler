package midend.llvm.function;

import midend.llvm.Value;

public class Param {
    private final String irType;
    private final Value memory;

    public Param(String irType, Value memory) {
        this.irType = irType;
        this.memory = memory;
    }

    public String getIrType() {
        return irType;
    }

    public String irOut() {
        return irType + " " + memory.irOut();
    }
}
