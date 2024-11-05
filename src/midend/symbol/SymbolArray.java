package midend.symbol;

import java.util.ArrayList;

public class SymbolArray extends Symbol{
    private int size;
    private ArrayList<Integer> initVal;
    private String initVal2;
    private boolean isConst;

    public SymbolArray(String symbolType, String name, int line, int scope, int size, ArrayList<Integer> initVal) {
        super(symbolType, name, line, scope);
        this.size = size;
        this.initVal = initVal;
    }

    public SymbolArray(String symbolType, String name, int line, int scope, int size, String initVal) {
        super(symbolType, name, line, scope);
        this.size = size;
        this.initVal2 = initVal;
    }
}
