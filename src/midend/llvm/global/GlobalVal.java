package midend.llvm.global;

import midend.llvm.Support;
import midend.llvm.symbol.Symbol;

public class GlobalVal extends Symbol {
    private final String name;
    private final String irType;
    private final Constant value;
    private final int size;

    public GlobalVal(String name, String symbolType, Constant value, int size) {
        super(symbolType, name, "@"+name, size);
        this.name = name;
        this.irType = Support.varTransfer(symbolType);
        this.value = value;
        this.size = size;
    }

    public String irOut() {
        if (value == null) {
            return "@" + name + " = dso_local global [" + size + " x " + irType + "] zeroinitializer";
        }
        if (value instanceof IrVar) {
            return "@" + name + " = dso_local global " + irType + " " + value.irOut();
        }
        if (value instanceof IrArray) {
            return "@" + name + " = dso_local global [" + size + " x " + irType + "] " + value.irOut();
        }
        return "@" + name + " = dso_local global [" + size + " x i8] c" + value.irOut();
    }
}
