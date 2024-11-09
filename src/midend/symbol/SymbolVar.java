package midend.symbol;

import java.util.ArrayList;

public class SymbolVar extends Symbol {
    private int value;
    private ArrayList<Integer> intInitVal;
    private String charInitVal;

    public SymbolVar(String symbolType, String name, int line, int scope) {
        super(symbolType, name, line, scope);
    }

    public SymbolVar(String symbolType, String name, int line, String memory, int value) {
        super(symbolType, name, line, 0, memory, -1);
        this.value = value;
    }

    public SymbolVar(String symbolType, String name, int line, String memory, ArrayList<Integer> values, int arraySize) {
        super(symbolType, name, line, 0, memory, arraySize);
        this.intInitVal = values;
    }

    public SymbolVar(String symbolType, String name, int line, String memory, String values, int arraySize) {
        super(symbolType, name, line, 0, memory, arraySize);
        this.charInitVal = values;
    }

    public SymbolVar(String symbolType, String name, int line, String memory) {
        super(symbolType, name, line, 0, memory, -1);
        this.value = 0;
    }

    public int getValue() {
        return value;
    }
}
