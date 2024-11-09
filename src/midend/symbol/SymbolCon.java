package midend.symbol;

import java.util.ArrayList;

public class SymbolCon extends Symbol {
    private int value;
    private int arraySize;
    private ArrayList<Integer> intInitVal;
    private String charInitVal;

    public SymbolCon(String symbolType, String name, int line, int scope) {
        super(symbolType, name, line, scope);
    }

    public SymbolCon(String symbolType, String name, int line, String memory, int value) {
        super(symbolType, name, line, 0, memory, -1);
        this.value = value;
    }

    public SymbolCon(String symbolType, String name, int line, String memory, ArrayList<Integer> intInitVal, int arraySize) {
        super(symbolType, name, line, 0, memory, arraySize);
        this.intInitVal = intInitVal;
    }

    public SymbolCon(String symbolType, String name, int line, String memory, String charInitVal, int arraySize) {
        super(symbolType, name, line, 0, memory, arraySize);
        this.charInitVal = charInitVal;
    }

    public int getValue() {
        return value;
    }

    public int getValueAtLoc(int loc) {
        if (loc > intInitVal.size() - 1) {
            return 0;
        } else {
            return intInitVal.get(loc);
        }
    }
}