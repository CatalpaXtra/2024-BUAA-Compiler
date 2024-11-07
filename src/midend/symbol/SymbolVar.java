package midend.symbol;

public class SymbolVar extends Symbol {
    private int value;

    public SymbolVar(String symbolType, String name, int line, int scope) {
        super(symbolType, name, line, scope);
    }

    public SymbolVar(String symbolType, String name, int line, String memory, int value) {
        super(symbolType, name, line, 0, memory);
        this.value = value;
    }

    public SymbolVar(String symbolType, String name, int line, String memory) {
        super(symbolType, name, line, 0, memory);
        this.value = 0;
    }

    public int getValue() {
        return value;
    }
}
