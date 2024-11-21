package midend.llvm.symbol;

import midend.llvm.Value;
import midend.llvm.global.constant.IrArray;
import midend.llvm.global.constant.IrConstant;
import midend.llvm.global.constant.IrString;
import midend.llvm.global.constant.IrVar;

public class SymbolCon extends Symbol {
    private final IrConstant irConstant;
    private final int arraySize;

    public SymbolCon(String symbolType, String name, Value memory, IrConstant irConstant, int arraySize) {
        super(symbolType, name, memory, arraySize);
        this.irConstant = irConstant;
        this.arraySize = arraySize;
    }

    public int getValue() {
        return ((IrVar) irConstant).getValue();
    }

    public int getValueAtLoc(int loc) {
        if (irConstant instanceof IrArray) {
            if (loc > arraySize - 1) {
                return 0;
            } else {
                return ((IrArray) irConstant).getNumAt(loc);
            }
        } else {
            if (loc > arraySize - 1) {
                return 0;
            } else {
                return ((IrString) irConstant).getCharAt(loc);
            }
        }
    }
}