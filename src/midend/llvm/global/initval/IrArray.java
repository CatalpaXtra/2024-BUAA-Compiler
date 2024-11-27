package midend.llvm.global.initval;

import java.util.ArrayList;

public class IrArray extends InitVal {
    private final String irType;
    private final ArrayList<Integer> constExpSet;
    private final int size;

    public IrArray(String irType, ArrayList<Integer> constExpSet, int size) {
        this.irType = irType;
        this.constExpSet = constExpSet;
        this.size = size;
    }

    public int getNumAt(int loc) {
        return constExpSet.get(loc);
    }

    public ArrayList<Integer> getConstExpSet() {
        return constExpSet;
    }

    public String toString() {
        boolean zeroinitializer = true;
        String arrayFormat = "[";
        for (Integer val : constExpSet) {
            if (val != 0) {
                zeroinitializer = false;
            }
            arrayFormat += irType + " " + val + ", ";
        }
        if (zeroinitializer) {
            return "zeroinitializer";
        }
        for (int i = constExpSet.size(); i < size; i++) {
            arrayFormat += irType + " 0, ";
        }
        return arrayFormat.substring(0, arrayFormat.length() - 2) + "]";
    }
}
