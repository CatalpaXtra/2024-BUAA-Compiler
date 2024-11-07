package midend.symbol;

public class SymbolCon extends Symbol {
    private int value;

    public SymbolCon(String symbolType, String name, int line, int scope) {
        super(symbolType, name, line, scope);
    }

    public SymbolCon(String symbolType, String name, int line, String memory, int value) {
        super(symbolType, name, line, 0, memory);
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}