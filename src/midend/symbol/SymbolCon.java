package midend.symbol;

public class SymbolCon extends Symbol {
    private int value;

    public SymbolCon(String symbolType, String name, int line, int scope, String memory, int value) {
        super(symbolType, name, line, scope, memory);
        this.value = value;
    }

    public SymbolCon(String symbolType, String name, int line, int scope) {
        super(symbolType, name, line, scope);
        this.value = 0;
    }

    public int getValue() {
        return value;
    }
}