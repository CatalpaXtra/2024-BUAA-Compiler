package midend.llvm.symbol;

import java.util.ArrayList;

public class SymbolCon extends Symbol {
    private int value;
    private ArrayList<Integer> intInitVal;
    private String charInitVal;

    public SymbolCon(String symbolType, String name, String memory, int value) {
        super(symbolType, name, memory, -1);
        this.value = value;
    }

    public SymbolCon(String symbolType, String name, String memory, ArrayList<Integer> intInitVal, int arraySize) {
        super(symbolType, name, memory, arraySize);
        this.intInitVal = intInitVal;
        this.charInitVal = null;
    }

    public SymbolCon(String symbolType, String name, String memory, String charInitVal, int arraySize) {
        super(symbolType, name, memory, arraySize);
        this.intInitVal = null;
        this.charInitVal = charInitVal;
    }

    public int getValue() {
        return value;
    }

    public int getValueAtLoc(int loc) {
        if (intInitVal != null) {
            if (loc > intInitVal.size() - 1) {
                return 0;
            } else {
                return intInitVal.get(loc);
            }
        } else {
            if (loc > charInitVal.length() - 1) {
                return 0;
            } else {
                return charInitVal.charAt(loc);
            }
        }
    }
}