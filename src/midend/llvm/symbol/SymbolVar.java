package midend.llvm.symbol;

import midend.llvm.Value;
import midend.llvm.global.constant.IrCon;

public class SymbolVar extends Symbol {
    public SymbolVar(String name, String irType, Value irAlloca, int arraySize, IrCon value) {
        super(name, irType, irAlloca, arraySize, value);
    }
}
