package midend.llvm.symbol;

import midend.llvm.Value;

import java.util.ArrayList;

public class SymbolVar extends Symbol {
    private int value;
    private ArrayList<Integer> intInitVal;
    private String charInitVal;

    public SymbolVar(String symbolType, String name, Value memory) {
        super(symbolType, name, memory, -1);
    }

    public SymbolVar(String symbolType, String name, Value memory, ArrayList<Integer> values, int arraySize) {
        super(symbolType, name, memory, arraySize);
        this.intInitVal = values;
    }

    public SymbolVar(String symbolType, String name, Value memory, String values, int arraySize) {
        super(symbolType, name, memory, arraySize);
        this.charInitVal = values;
    }
}
