package midend.llvm.global;

import midend.llvm.Value;
import midend.llvm.global.initval.IrArray;
import midend.llvm.global.initval.InitVal;
import midend.llvm.global.initval.IrVar;
import midend.llvm.symbol.Symbol;

public class GlobalVal extends Symbol {
    private final boolean isConst;

    public GlobalVal(String name, String irType, InitVal value, int size, boolean isConst) {
        super(name, irType, new Value("@"+name, irType), size, value);
        this.isConst = isConst;
    }

    public InitVal getInitVal() {
        return value;
    }

    public boolean isConst() {
        return isConst;
    }

    public String toString() {
        if (value == null) {
            return "@" + name + " = dso_local global [" + size + " x " + irType + "] zeroinitializer";
        }
        if (value instanceof IrVar) {
            return "@" + name + " = dso_local global " + irType + " " + value.toString();
        }
        if (value instanceof IrArray) {
            return "@" + name + " = dso_local global [" + size + " x " + irType + "] " + value.toString();
        }
        return "@" + name + " = dso_local global [" + size + " x i8] c" + value.toString();
    }
}
