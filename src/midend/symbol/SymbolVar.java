package midend.symbol;

public class SymbolVar extends Symbol {
    private int value;

    public SymbolVar(String symbolType, String name, int line, int scope, String memory, int value) {
        super(symbolType, name, line, scope, memory);
        this.value = value;
    }

    public SymbolVar(String symbolType, String name, int line, int scope, String memory) {
        super(symbolType, name, line, scope, memory);
        this.value = 0;
    }

    public SymbolVar(String symbolType, String name, int line, int scope) {
        super(symbolType, name, line, scope);
    }

    public int getValue() {
        return value;
    }
}
