package midend.llvm.symbol;

import midend.llvm.Value;
import midend.llvm.global.initval.InitVal;

public class SymbolVar extends Symbol {
    public SymbolVar(String name, String irType, Value irAlloca, int arraySize, InitVal value) {
        super(name, irType, irAlloca, arraySize, value);
    }
}
