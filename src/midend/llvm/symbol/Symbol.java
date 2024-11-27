package midend.llvm.symbol;

import midend.llvm.User;
import midend.llvm.Value;
import midend.llvm.global.initval.IrArray;
import midend.llvm.global.initval.InitVal;
import midend.llvm.global.initval.IrString;
import midend.llvm.global.initval.IrVar;

public class Symbol extends User {
    protected final int size;
    protected InitVal value;
    protected final Value irAlloca;

    public Symbol(String name, String irType, Value irAlloca, int size, InitVal value) {
        super(name, irType);
        this.irAlloca = irAlloca;
        this.size = size;
        this.value = value;
    }

    public String getIrType() {
        return irType.replaceAll("\\*$", "");
    }

    public boolean isPointer() {
        return irType.contains("*");
    }

    public boolean isChar() {
        return irType.contains("i8");
    }

    public boolean isArray() {
        return size != -1;
    }

    public int getArraySize() {
        return size;
    }

    public Value getIrAlloca() {
        return irAlloca;
    }

    public boolean isKnown() {
        return value != null;
    }

    public void setUnknown() {
        this.value = null;
    }

    public int getValue() {
        return ((IrVar) value).getValue();
    }

    public int getValueAtLoc(int loc) {
        if (value instanceof IrArray) {
            if (loc > size - 1) {
                return 0;
            } else {
                return ((IrArray) value).getNumAt(loc);
            }
        } else {
            if (loc > size - 1) {
                return 0;
            } else {
                return ((IrString) value).getCharAt(loc);
            }
        }
    }

}
