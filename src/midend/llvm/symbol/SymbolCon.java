package midend.llvm.symbol;

import midend.llvm.Value;
import midend.llvm.global.initval.InitVal;

public class SymbolCon extends Symbol {
    public SymbolCon(String name, String irType, Value irAlloca, InitVal value, int size) {
        super(name, irType, irAlloca, size, value);
    }
}