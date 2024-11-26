package midend.llvm.symbol;

import midend.llvm.Value;
import midend.llvm.global.constant.IrCon;

public class SymbolCon extends Symbol {
    public SymbolCon(String name, String irType, Value irAlloca, IrCon value, int size) {
        super(name, irType, irAlloca, size, value);
    }
}