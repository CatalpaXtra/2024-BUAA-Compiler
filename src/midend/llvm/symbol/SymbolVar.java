package midend.llvm.symbol;

import java.util.ArrayList;

public class SymbolVar extends Symbol {
    private int value;
    private ArrayList<Integer> intInitVal;
    private String charInitVal;
    private boolean unknown;

    public SymbolVar(String symbolType, String name, String memory, int value) {
        super(symbolType, name, memory, -1);
        this.value = value;
    }

    public SymbolVar(String symbolType, String name, String memory) {
        super(symbolType, name, memory, -1);
        this.unknown = true;
    }

    public SymbolVar(String symbolType, String name, String memory, ArrayList<Integer> values, int arraySize) {
        super(symbolType, name, memory, arraySize);
        this.intInitVal = values;
    }

    public SymbolVar(String symbolType, String name, String memory, String values, int arraySize) {
        super(symbolType, name, memory, arraySize);
        this.charInitVal = values;
    }

    public int getValue() {
        return value;
    }
}
