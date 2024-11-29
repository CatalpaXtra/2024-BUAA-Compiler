package midend.llvm.function;

import midend.llvm.Value;

public class Param extends Value {
    public Param(String irType, String name) {
        super(name, irType);
    }

    @Override
    public String toString() {
        return irType + " " + name;
    }
}
